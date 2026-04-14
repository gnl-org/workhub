# Workhub Security & Authorization Model

## 1. Role Overview
| Role | Level | Description |
| :--- | :--- | :--- |
| **ADMIN** | Global | Full system access. Can view/edit all projects and tasks. |
| **USER** | Global | Base role. Can create own projects and participate in others. |
| **OWNER** | Project | The creator of a project. Has management rights over members. |
| **MEMBER** | Project | Added by Owner/Admin. Has collaborative rights over tasks. |

## 2. Permissions Matrix
| Action | ADMIN | OWNER | MEMBER | USER (Other) |
| :--- | :---: | :---: | :---: | :---: |
| **View All Projects** | ✅ | ❌ | ❌ | ❌ |
| **View Project Details** | ✅ | ✅ | ✅ | ❌ |
| **Add/Remove Members** | ✅ | ✅ | ❌ | ❌ |
| **Update/Delete Project** | ✅ | ✅ | ❌ | ❌ |
| **Create Task** | ✅ | ✅ | ✅ | ❌ |
| **Edit/Delete Task** | ✅ | ✅ | ✅ | ❌ |

## 3. Data Integrity Rules
- **Access Isolation:** Standard users can only "see" projects they own or have been invited to.
- **Cascading Deletion:** Deletion of a Project (via `CascadeType.ALL`) automatically removes all associated Tasks and Member relations.
- **Implicit Identity:** The `SecurityContext` provides the identity for all creation tasks; `ownerId` is never accepted as a request parameter.

## 4. Implementation Details
The security layer is enforced via:
- **Spring Security Method Security:** `@PreAuthorize("hasRole('ADMIN')")` for global overrides.
- **Service Layer Gatekeepers:** `validateProjectAccess(Project, User)` for contextual authorization.
- **JPA Composite Keys:** `ProjectMemberId` ensures a user cannot be added twice to the same project.