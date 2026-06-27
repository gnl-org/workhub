# WorkHub: Work Stages + Sprint Management — Complete Implementation Guide

Save this document. It captures the full spec and step-by-step implementation plan from planning through polish.

---

## 1. Product summary

### What you're building

| Tab / view | Purpose |
|------------|---------|
| **Backlog** | All backlog work organized into **custom work stages** (user-created). Drag/move tasks between stages. |
| **Active Sprint** | **One active sprint** shown as a **kanban board** (columns = `TaskStatus`). |
| **Sprint history** | Read-only list of **closed** sprints and their tasks. |

### Two concepts (do not conflate)

| Concept | Examples | Drives |
|---------|----------|--------|
| **Work stage** | Backlog, Ready for Refinement, Ready for Sprint, Sprint 24 | Backlog tab grouping |
| **Sprint** | Sprint 24 (PLANNED → ACTIVE → CLOSED) | Active kanban + sprint-linked backlog stage |
| **Task status** | OPEN, IN_PROGRESS, IN_REVIEW, COMPLETED | Kanban columns in Active Sprint tab |

### Current codebase baseline

- **Backend:** Spring Boot 4, Java 21, JPA, Flyway, PostgreSQL
- **Frontend:** React 19, Vite, Tailwind, Axios
- **Exists today:** `Task` CRUD, flat read-only `BacklogTab`, mock `ActiveSprintTab`
- **Missing today:** work stages, sprints, ordering, DnD, sprint lifecycle, history

**Key existing files:**

```
backend/src/main/java/com/gnl/workhub/backend/entity/Task.java
backend/src/main/java/com/gnl/workhub/backend/service/TaskService.java
backend/src/main/java/com/gnl/workhub/backend/controller/TaskController.java
backend/src/main/resources/db/migration/V1__init_schema.sql … V7
workhub-frontend/src/pages/project/BacklogTab.jsx
workhub-frontend/src/pages/project/ActiveSprintTab.jsx
workhub-frontend/src/pages/project/ProjectDetails.jsx
workhub-frontend/src/hooks/useTasks.js
```

---

## 2. Confirmed business rules (all decisions locked)

### Backlog visibility

| Item | Rule |
|------|------|
| Default work stages | Always visible (Backlog, Ready for Refinement, Ready for Sprint + any user-created stage with no sprint link) |
| Unassigned tasks (`sprint_id IS NULL`) | Always visible in whatever work stage they belong to |
| Sprint-linked stage, sprint `PLANNED` | Visible in Backlog |
| Sprint-linked stage, sprint `ACTIVE` | Visible in Backlog; tasks show **"In Active Sprint"** badge |
| Sprint-linked stage, sprint `CLOSED` | **Hidden** from Backlog |
| Tasks on closed sprint only | **Hidden** from Backlog |

### Work stages

| Rule | Behavior |
|------|----------|
| Create | User can create any name from UI |
| Delete | Allowed; tasks reassigned to default **Backlog** stage |
| Reorder | Drag stage headers |
| On sprint create | Auto-create linked work stage (same name, e.g. "Sprint 24") |

### Sprint lifecycle

| Rule | Behavior |
|------|----------|
| Statuses | `PLANNED` → `ACTIVE` → `CLOSED` |
| Active constraint | **Only one `ACTIVE` sprint per project** |
| Start guard | Cannot start a planned sprint if another sprint is already `ACTIVE` — must close it first |
| Dates | Optional on create; `start_date` set on **Start** if missing |
| Kanban columns | Fixed `TaskStatus` enum (keep simple for v1) |

### Close sprint — incomplete task carry-over

**Incomplete** = status NOT IN (`COMPLETED`, `CANCELLED`)
→ OPEN, IN_PROGRESS, IN_REVIEW, BLOCKED

| Scenario | Action |
|----------|--------|
| ≥1 `PLANNED` sprint exists | Move incomplete tasks to planned sprint with **lowest sprint number** |
| No `PLANNED` sprint exists | Auto-create new `PLANNED` sprint (next auto-increment number) + work stage; move incomplete tasks there |
| Completed / cancelled tasks | Stay on closed sprint; hidden from Backlog |

**Sprint number parsing:** Extract numeric suffix from name (`"Sprint 24"` → 24). Lowest number wins. Tie-break: earliest `created_at`.

**Auto-increment:** `max(existing parsed numbers) + 1`, or `1` if none.

### Sprint history

- Required: view closed sprints (name, goal, dates, task counts, read-only task list)
- Not in v1: reopen closed sprint, edit closed sprint

---

## 3. Data model

### ER diagram

```
Project
  ├── WorkStage (many)     ← default stages + sprint-linked stages
  ├── Sprint (many)        ← PLANNED | ACTIVE | CLOSED
  └── Task (many)
        ├── work_stage_id  (required after migration)
        ├── sprint_id      (nullable)
        ├── sort_order     (int, within stage)
        └── status         (TaskStatus — kanban columns)
```

### Table: `work_stages`

```sql
CREATE TABLE work_stages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sprint_id   UUID REFERENCES sprints(id) ON DELETE SET NULL,  -- NULL = default stage
    name        VARCHAR(100) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, name)
);
CREATE INDEX idx_work_stages_project ON work_stages(project_id);
CREATE INDEX idx_work_stages_sprint ON work_stages(sprint_id);
```

### Table: `sprints`

```sql
CREATE TYPE sprint_status AS ENUM ('PLANNED', 'ACTIVE', 'CLOSED');

CREATE TABLE sprints (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    goal        TEXT,
    status      sprint_status NOT NULL DEFAULT 'PLANNED',
    start_date  DATE,
    end_date    DATE,
    closed_at   TIMESTAMPTZ,           -- set on close
    created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_one_active_sprint_per_project
    ON sprints(project_id) WHERE status = 'ACTIVE';

CREATE INDEX idx_sprints_project_status ON sprints(project_id, status);
```

### Extend: `tasks`

```sql
ALTER TABLE tasks
    ADD COLUMN work_stage_id UUID REFERENCES work_stages(id) ON DELETE SET NULL,
    ADD COLUMN sprint_id     UUID REFERENCES sprints(id) ON DELETE SET NULL,
    ADD COLUMN sort_order    INT NOT NULL DEFAULT 0;

CREATE INDEX idx_tasks_work_stage ON tasks(work_stage_id, sort_order);
CREATE INDEX idx_tasks_sprint ON tasks(sprint_id);
```

### Fixups (same or separate migration)

```sql
-- Align DB default with Java enum
ALTER TABLE tasks ALTER COLUMN status SET DEFAULT 'OPEN';
UPDATE tasks SET status = 'OPEN' WHERE status = 'TODO';
```

Fix `TaskRepository.countOverdueTasks`: use `COMPLETED` not `DONE`.

### Default stages (seed per project)

| name | sort_order | sprint_id |
|------|------------|-----------|
| Backlog | 0 | NULL |
| Ready for Refinement | 1 | NULL |
| Ready for Sprint | 2 | NULL |

Hook seeding into `ProjectService.createProject()` and backfill migration for existing projects.

---

## 4. Backend implementation steps

### Step 4.1 — Migrations

**Files to create:**

```
backend/src/main/resources/db/migration/V8__work_stages_and_sprints.sql
backend/src/main/resources/db/migration/V9__backfill_work_stages.sql
```

**V8:** Create `work_stages`, `sprints`, alter `tasks`, partial unique index, status default fix.

**V9:** For each existing project → insert 3 default stages; set all tasks `work_stage_id` = Backlog stage, `sort_order` = row number by `created_at`.

Run locally: `./mvnw flyway:migrate` or start app.

---

### Step 4.2 — Enums

**Create:**

```java
// enums/SprintStatus.java
public enum SprintStatus { PLANNED, ACTIVE, CLOSED }
```

**Existing:** `TaskStatus` — OPEN, IN_PROGRESS, IN_REVIEW, COMPLETED, CANCELLED, BLOCKED

---

### Step 4.3 — Entities

**Create `WorkStage.java`:**

```java
@Entity @Table(name = "work_stages")
public class WorkStage extends BaseEntity {
    @ManyToOne Project project;
    @ManyToOne Sprint sprint;      // nullable
    String name;
    int sortOrder;
}
```

**Create `Sprint.java`:**

```java
@Entity @Table(name = "sprints")
public class Sprint extends BaseEntity {
    @ManyToOne Project project;
    String name;
    String goal;
    @Enumerated(EnumType.STRING) SprintStatus status;
    LocalDate startDate;
    LocalDate endDate;
    Instant closedAt;
}
```

**Modify `Task.java`:** add `workStage`, `sprint`, `sortOrder`.

---

### Step 4.4 — Repositories

**Create:**

```java
WorkStageRepository
  - findByProjectIdOrderBySortOrderAsc(UUID projectId)
  - findBySprintId(UUID sprintId)
  - existsByProjectIdAndName(UUID, String)
  - findDefaultBacklogStage(UUID projectId)  // name='Backlog' AND sprint IS NULL

SprintRepository
  - findByProjectIdAndStatus(UUID, SprintStatus)
  - findByProjectIdAndStatusOrderByCreatedAtDesc(UUID, SprintStatus)
  - findByProjectIdOrderByCreatedAtDesc(UUID)
  - existsByProjectIdAndStatus(UUID, ACTIVE)
```

**Extend `TaskRepository`:**

```java
  - findByWorkStageIdAndDeletedFalseOrderBySortOrderAsc(UUID)
  - findBySprintIdAndDeletedFalse(UUID)
  - countBySprintIdAndStatusNotIn(UUID, List<TaskStatus>)
```

---

### Step 4.5 — DTOs

**Work stages:**

```
WorkStageResponse       { id, name, sortOrder, sprintId, sprintStatus }
CreateWorkStageRequest  { name }
UpdateWorkStageRequest  { name }
ReorderWorkStagesRequest { stageIds: UUID[] }
BacklogResponse         { stages: BacklogStageDto[] }
BacklogStageDto         { stage: WorkStageResponse, tasks: TaskResponse[] }
MoveTaskRequest         { workStageId, sortOrder }
ReorderTasksRequest     { taskIds: UUID[] }
```

**Sprints:**

```
SprintResponse          { id, name, goal, status, startDate, endDate, closedAt, taskCounts }
CreateSprintRequest     { name?, goal?, startDate?, endDate? }
UpdateSprintRequest     { name?, goal?, startDate?, endDate? }
SprintHistoryResponse     { sprints: SprintResponse[] }
SprintDetailResponse    { sprint, tasks: TaskResponse[] }
AssignTasksRequest      { taskIds: UUID[] }
```

---

### Step 4.6 — Services

#### `WorkStageService`

| Method | Logic |
|--------|-------|
| `listVisibleStages(projectId)` | Default stages + stages linked to PLANNED/ACTIVE sprints only |
| `createStage(projectId, name)` | Append at max sortOrder + 1; unique name check |
| `renameStage(id, name)` | Update name |
| `deleteStage(id)` | Move tasks → default Backlog stage; delete stage (block delete of default Backlog?) — recommend: block deleting seeded defaults, allow custom |
| `reorderStages(projectId, ids[])` | Rewrite sortOrder 0..n |
| `seedDefaultStages(project)` | Called from ProjectService on create |

#### `SprintService`

| Method | Logic |
|--------|-------|
| `listSprints(projectId)` | All sprints |
| `listClosedSprints(projectId)` | status = CLOSED, newest first |
| `getActiveSprint(projectId)` | Optional single ACTIVE |
| `getSprintDetail(id)` | Sprint + tasks (for history) |
| `createSprint(projectId, req)` | Parse/auto name "Sprint N"; status PLANNED; create linked WorkStage |
| `updateSprint(id, req)` | Only if PLANNED |
| `startSprint(id)` | Throw 409 if another ACTIVE; set ACTIVE; set startDate if null |
| `closeSprint(id)` | See algorithm below |
| `assignTasks(sprintId, taskIds[])` | Set task.sprint_id; optionally move to sprint's linked work stage |
| `removeTasks(taskIds[])` | Clear sprint_id |
| `parseSprintNumber(name)` | Regex extract int |
| `nextSprintNumber(projectId)` | max + 1 |
| `findTargetPlannedSprint(projectId)` | PLANNED with lowest sprint number |

#### Close sprint algorithm

```
1. Validate sprint is ACTIVE
2. Set status = CLOSED, closed_at = now(), end_date = today if null
3. Find incomplete tasks on this sprint
4. target = findTargetPlannedSprint(projectId)
   IF target is null:
     target = createSprint(projectId, auto name "Sprint N")
5. For each incomplete task:
     task.sprint_id = target.id
     task.workStage = target's linked work stage
     recompute sort_order in target stage
6. Return CloseSprintResponse with counts + target sprint info
```

#### Extend `TaskService`

| Method | Logic |
|--------|-------|
| `getBacklog(projectId)` | Visible stages + tasks per stage, ordered by sortOrder |
| `createTask(...)` | Default work_stage = Backlog; sortOrder = max+1 in stage |
| `moveTask(taskId, stageId, sortOrder)` | Update work_stage + sort_order; log activity |
| `reorderTasksInStage(stageId, taskIds[])` | Bulk update sort_order |
| `updateTaskStatus(taskId, status)` | For kanban DnD |

#### Extend `ProjectService.createProject()`

After project save → `workStageService.seedDefaultStages(project)`.

#### Extend `ActivityLogService`

Log: stage move, sprint assign, sprint start, sprint close.

---

### Step 4.7 — Controllers & API contract

Base path pattern matches existing: `/projects/{projectId}/...`

#### Work stages

```
GET    /projects/{projectId}/work-stages
POST   /projects/{projectId}/work-stages              body: { name }
PATCH  /projects/{projectId}/work-stages/{stageId}    body: { name }
DELETE /projects/{projectId}/work-stages/{stageId}
PUT    /projects/{projectId}/work-stages/reorder      body: { stageIds: UUID[] }
```

#### Backlog (grouped)

```
GET    /projects/{projectId}/backlog
       → { stages: [{ id, name, sortOrder, sprintId, sprintStatus, tasks: TaskResponse[] }] }

PATCH  /projects/{projectId}/tasks/{taskId}/move
       body: { workStageId, sortOrder? }

PUT    /projects/{projectId}/work-stages/{stageId}/tasks/reorder
       body: { taskIds: UUID[] }
```

#### Sprints

```
GET    /projects/{projectId}/sprints
GET    /projects/{projectId}/sprints/active
GET    /projects/{projectId}/sprints/history          → closed sprints
GET    /projects/{projectId}/sprints/{sprintId}       → detail + tasks
POST   /projects/{projectId}/sprints                  body: CreateSprintRequest
PATCH  /projects/{projectId}/sprints/{sprintId}       body: UpdateSprintRequest (PLANNED only)
POST   /projects/{projectId}/sprints/{sprintId}/start
POST   /projects/{projectId}/sprints/{sprintId}/close
POST   /projects/{projectId}/sprints/{sprintId}/tasks body: { taskIds }
DELETE /projects/{projectId}/sprints/{sprintId}/tasks body: { taskIds }
```

#### Active sprint kanban

```
GET    /projects/{projectId}/sprints/active/tasks
       → tasks where sprint.status=ACTIVE AND deleted=false

PATCH  /projects/{projectId}/tasks/{taskId}
       body: { status }   (existing endpoint — reuse for kanban)
```

#### Error codes

| Code | When |
|------|------|
| 409 Conflict | Start sprint while another ACTIVE exists |
| 400 Bad Request | Close non-ACTIVE sprint; update CLOSED sprint |
| 404 | Resource not found |
| 403 | Not project member |
```

---

### Step 4.8 — Mappers

```
WorkStageMapper, SprintMapper
Extend TaskMapper → include workStageId, sprintId, sortOrder, inActiveSprint flag
Extend TaskResponse
```

---

### Step 4.9 — Backlog query filter (critical)

In `getBacklog()`:

```sql
-- Include stage if:
--   sprint_id IS NULL  (default/custom stage)
--   OR linked sprint.status IN ('PLANNED', 'ACTIVE')

-- Include task if:
--   task.work_stage is visible
--   AND (task.sprint_id IS NULL
--        OR task.sprint.status IN ('PLANNED', 'ACTIVE'))
```

Tasks on closed sprints must never leak into backlog response even if `work_stage_id` is stale.

---

### Step 4.10 — Backend tests (recommended)

```
WorkStageServiceTest       — create, delete reassigns tasks, visibility filter
SprintServiceTest          — start guard (409), close carry-over, auto-create target sprint
SprintServiceTest          — lowest sprint number selection
BacklogIntegrationTest     — closed sprint tasks excluded
```

---

## 5. Frontend implementation steps

### Step 5.1 — Dependencies

```bash
cd workhub-frontend
npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities
```

---

### Step 5.2 — API hooks

**Create `src/hooks/useWorkStages.js`:**

```
fetchStages(), createStage(name), renameStage(id, name),
deleteStage(id), reorderStages(stageIds)
```

**Create `src/hooks/useSprints.js`:**

```
fetchSprints(), fetchActiveSprint(), fetchSprintHistory(), fetchSprintDetail(id),
createSprint(data), updateSprint(id, data), startSprint(id), closeSprint(id),
assignTasks(sprintId, taskIds), removeTasks(sprintId, taskIds),
fetchActiveSprintTasks()
```

**Create `src/hooks/useBacklog.js`:**

```
fetchBacklog(), moveTask(taskId, stageId, sortOrder), reorderTasks(stageId, taskIds)
```

**Extend `src/hooks/useTasks.js`:**

Wire `createTask` with `workStageId`; keep `updateTask` for status changes.

---

### Step 5.3 — Backlog tab restructure

**Replace `BacklogTab.jsx` layout:**

```
BacklogTab
├── Header: title, [+ New Stage], [+ New Task]
├── Error / loading states
└── DndContext
    └── SortableContext (stages)
        └── StageSection (for each visible stage)
            ├── StageHeader (name, count, collapse, ⋮ menu: rename/delete)
            └── SortableContext (tasks)
                └── BacklogTaskRow (each task)
                    ├── title, priority, assignee, status
                    ├── "In Active Sprint" badge if sprint.status === ACTIVE
                    └── ⋮ menu: move to stage, assign to sprint
```

**New components:**

```
src/components/backlog/StageSection.jsx
src/components/backlog/StageHeader.jsx
src/components/backlog/BacklogTaskRow.jsx
src/components/backlog/CreateStageModal.jsx
src/components/backlog/CreateTaskModal.jsx
src/components/backlog/MoveTaskMenu.jsx
```

**DnD behavior:**

| Drag | API call |
|------|----------|
| Task within stage | `PUT .../work-stages/{id}/tasks/reorder` |
| Task to another stage | `PATCH .../tasks/{id}/move` |
| Stage header | `PUT .../work-stages/reorder` |

Start with **menu-based moves** in Phase 1 if DnD is slow; add DnD in Phase 4.

---

### Step 5.4 — Active Sprint tab

**Rewrite `ActiveSprintTab.jsx`:**

```
ActiveSprintTab
├── if no active sprint → EmptyState (link to create/start)
├── SprintToolbar
│     name, goal, end date countdown
│     [Manage Sprints] [Complete Sprint]
├── KanbanBoard (DndContext)
│     columns from TaskStatus: OPEN, IN_PROGRESS, IN_REVIEW, COMPLETED
│     └── KanbanColumn
│           └── SprintTaskCard (draggable)
└── on drag end → updateTask({ status: newColumn })
```

**New components:**

```
src/components/sprint/SprintToolbar.jsx
src/components/sprint/KanbanBoard.jsx
src/components/sprint/KanbanColumn.jsx
src/components/sprint/SprintTaskCard.jsx
src/components/sprint/SprintEmptyState.jsx
src/components/sprint/CloseSprintModal.jsx      (confirm only — no carry-over choice)
src/components/sprint/SprintManageDrawer.jsx    (create, edit planned, start)
```

**Modify `ProjectDetails.jsx`:**

```jsx
{activeTab === 'sprint' && <ActiveSprintTab projectId={projectId} />}
{activeTab === 'history' && <SprintHistoryTab projectId={projectId} />}
```

Add 4th tab: **Sprint History** (or sub-view inside Active Sprint — tab is cleaner).

---

### Step 5.5 — Sprint history tab

**Create `SprintHistoryTab.jsx`:**

```
SprintHistoryTab
├── list of closed sprints (newest first)
│     card: name, dates, goal snippet, completed/incomplete counts
└── click → SprintHistoryDetail (drawer or inline expand)
      read-only task list grouped by final status
```

**New components:**

```
src/pages/project/SprintHistoryTab.jsx
src/components/sprint/SprintHistoryCard.jsx
src/components/sprint/SprintHistoryDetail.jsx
```

---

### Step 5.6 — Sprint management flows

#### Create sprint

1. User opens SprintManageDrawer → "New Sprint"
2. Optional: name (default auto "Sprint N"), goal, dates
3. `POST /sprints` → backend creates sprint + work stage
4. Refresh backlog + sprint list

#### Start sprint

1. User picks PLANNED sprint → "Start"
2. If ACTIVE exists → show error: "Close Sprint X first"
3. `POST /sprints/{id}/start`
4. Switch to Active Sprint tab

#### Assign tasks to sprint

From BacklogTaskRow menu → pick PLANNED or ACTIVE sprint  
`POST /sprints/{id}/tasks`  
Move task to sprint's linked work stage (backend handles)

#### Close sprint

1. Active Sprint tab → "Complete Sprint"
2. Confirm modal (show incomplete count; explain they'll move to Sprint N)
3. `POST /sprints/{id}/close`
4. Refresh backlog (closed stage gone), history, active sprint tab

---

### Step 5.7 — UI states checklist

| State | Handling |
|-------|----------|
| Empty backlog | Existing empty state per stage or global |
| No active sprint | SprintEmptyState with CTA |
| No closed sprints | History empty state |
| Loading | Skeleton rows / columns |
| 409 on start | Toast: close active sprint first |
| Error | Red banner (match existing BacklogTab pattern) |

---

## 6. Phased delivery (recommended order)

After each phase: update `docs/erd.md` with new tables, columns, and relationships.

### Phase 1 — Work stages + grouped backlog

- [ ] V8/V9 migrations
- [ ] WorkStage entity, repo, service, controller
- [ ] Seed default stages on project create + backfill
- [ ] `GET /backlog` with visibility filter
- [ ] Task move + reorder APIs
- [ ] BacklogTab with stage sections (menu moves OK; DnD optional)
- [ ] CreateStageModal, CreateTaskModal wired

**Done when:** User can create stages, create tasks, move tasks between stages.

---

### Phase 2 — Sprint entity + lifecycle

- [ ] Sprint entity, repo, service, controller
- [ ] Auto-create work stage on sprint create
- [ ] Start sprint (with 409 guard)
- [ ] Close sprint (carry-over algorithm)
- [ ] Assign/remove tasks from sprint
- [ ] SprintManageDrawer + CloseSprintModal

**Done when:** Full sprint CRUD, start, close, task assignment works via API + basic UI.

---

### Phase 3 — Active sprint kanban

- [ ] `GET /sprints/active/tasks`
- [ ] Wire ActiveSprintTab to real data
- [ ] Kanban columns from TaskStatus
- [ ] Drag card → PATCH status
- [ ] "In Active Sprint" badge on BacklogTab

**Done when:** Active sprint shows real tasks; status changes via drag or click.

---

### Phase 4 — Sprint history

- [ ] `GET /sprints/history` + detail endpoint
- [ ] SprintHistoryTab + detail view
- [ ] Add tab to ProjectDetails

**Done when:** Closed sprints browsable read-only.

---

### Phase 5 — Polish

- [ ] Full DnD (backlog stages + tasks + kanban)
- [ ] Activity logging for moves/start/close
- [ ] Fix TaskSpecifications priority/assignee filters
- [ ] Fix overdue query (`COMPLETED` not `DONE`)
- [ ] Block delete of seeded default stages (optional UX)
- [ ] Optimistic UI updates + refresh on error rollback

---

## 7. Complete file checklist

### Database

```
V8__work_stages_and_sprints.sql
V9__backfill_work_stages.sql
```

### Backend — new files

```
entity/WorkStage.java
entity/Sprint.java
enums/SprintStatus.java
repository/WorkStageRepository.java
repository/SprintRepository.java
service/WorkStageService.java
service/SprintService.java
controller/WorkStageController.java
controller/SprintController.java
controller/BacklogController.java          (or extend TaskController)
dto/WorkStageResponse.java, CreateWorkStageRequest.java, ...
dto/SprintResponse.java, CreateSprintRequest.java, ...
dto/BacklogResponse.java, MoveTaskRequest.java, ...
mapper/WorkStageMapper.java
mapper/SprintMapper.java
```

### Backend — modify

```
entity/Task.java
repository/TaskRepository.java
service/TaskService.java
service/ProjectService.java
service/ActivityLogService.java
controller/TaskController.java
dto/TaskResponse.java, TaskRequest.java
mapper/TaskMapper.java
specification/TaskSpecifications.java     (fix filters)
repository/TaskRepository.java              (fix overdue)
```

### Frontend — new

```
hooks/useWorkStages.js
hooks/useSprints.js
hooks/useBacklog.js
components/backlog/StageSection.jsx
components/backlog/StageHeader.jsx
components/backlog/BacklogTaskRow.jsx
components/backlog/CreateStageModal.jsx
components/backlog/CreateTaskModal.jsx
components/backlog/MoveTaskMenu.jsx
components/sprint/SprintToolbar.jsx
components/sprint/KanbanBoard.jsx
components/sprint/KanbanColumn.jsx
components/sprint/SprintTaskCard.jsx
components/sprint/SprintEmptyState.jsx
components/sprint/SprintManageDrawer.jsx
components/sprint/CloseSprintModal.jsx
components/sprint/SprintHistoryCard.jsx
components/sprint/SprintHistoryDetail.jsx
pages/project/SprintHistoryTab.jsx
```

### Frontend — modify

```
pages/project/BacklogTab.jsx
pages/project/ActiveSprintTab.jsx
pages/project/ProjectDetails.jsx
hooks/useTasks.js
package.json
```

---

## 8. Key algorithms (copy-paste reference)

### Parse sprint number

```java
public OptionalInt parseSprintNumber(String name) {
    Matcher m = Pattern.compile("(\\d+)").matcher(name);
    if (m.find()) return OptionalInt.of(Integer.parseInt(m.group(1)));
    return OptionalInt.empty();
}
```

### Next sprint name

```java
public String nextSprintName(UUID projectId) {
    int max = sprintRepository.findByProjectId(projectId).stream()
        .map(Sprint::getName)
        .mapToInt(n -> parseSprintNumber(n).orElse(0))
        .max().orElse(0);
    return "Sprint " + (max + 1);
}
```

### Target planned sprint on close

```java
public Sprint findLowestNumberPlannedSprint(UUID projectId) {
    return sprintRepository.findByProjectIdAndStatus(projectId, PLANNED).stream()
        .min(Comparator
            .comparingInt(s -> parseSprintNumber(s.getName()).orElse(Integer.MAX_VALUE))
            .thenComparing(Sprint::getCreatedAt))
        .orElse(null);
}
```

### Incomplete task filter

```java
private static final Set<TaskStatus> COMPLETE = Set.of(COMPLETED, CANCELLED);
boolean isIncomplete(Task t) { return !COMPLETE.contains(t.getStatus()); }
```

### Backlog stage visibility

```java
boolean isStageVisible(WorkStage stage) {
    if (stage.getSprint() == null) return true;
    SprintStatus s = stage.getSprint().getStatus();
    return s == PLANNED || s == ACTIVE;
}
```

---

## 9. Manual test script (QA checklist)

### Work stages

1. Create project → verify 3 default stages appear
2. Create custom stage "Icebox" → appears at bottom
3. Create task in Backlog stage → appears in correct section
4. Move task to Ready for Refinement → disappears from Backlog section, appears there
5. Delete custom stage with tasks → tasks land in Backlog
6. Reorder stages → order persists on refresh

### Sprints

7. Create Sprint 1 → work stage "Sprint 1" appears in backlog
8. Assign tasks to Sprint 1 (PLANNED) → tasks show under Sprint 1 stage
9. Start Sprint 1 → Active Sprint tab shows kanban with those tasks
10. Tasks still visible in Backlog with "In Active Sprint" badge
11. Create Sprint 2 (PLANNED) while Sprint 1 active → OK
12. Try start Sprint 2 → error until Sprint 1 closed
13. Move kanban card to In Progress → status updates, backlog badge reflects status
14. Close Sprint 1 with incomplete tasks + Sprint 2 planned → incomplete go to Sprint 2
15. Close Sprint 1 with incomplete tasks + no planned → auto Sprint 3 created, tasks moved
16. Sprint 1 stage gone from backlog; completed tasks not in backlog
17. Sprint history shows closed Sprint 1 with read-only tasks

---

## 10. Out of scope for v1 (defer)

- Custom kanban columns per project
- Reopen closed sprint
- Edit closed sprint metadata
- Bulk select / bulk move UI
- Sprint burndown / velocity charts
- Notifications on sprint start/close
- Carry-over user choice modal (fixed rule: lowest planned or auto-create)

---

## 11. How to resume after lost context

1. Read **Section 2** (business rules) — source of truth for behavior
2. Run migrations V8/V9 if not applied
3. Check phase checklist in **Section 6** — continue from first unchecked phase
4. Use **Section 7** file checklist to see what's missing
5. Run **Section 9** manual tests to validate current state

When ready to build, say **go ahead** and start at **Phase 1, Step 4.1 (migrations)**.
