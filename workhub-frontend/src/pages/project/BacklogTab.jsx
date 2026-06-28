import React, { useEffect, useState, useCallback } from 'react';
import { Plus, AlertCircle, GripVertical } from 'lucide-react';
import { DndContext, DragOverlay, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { useBacklog } from '../../hooks/useBacklog';
import { useWorkStages } from '../../hooks/useWorkStages';
import { useTasks } from '../../hooks/useTasks';
import api from '../../api/axios';
import StageSection from '../../components/backlog/StageSection';
import StageInsertionPoint from '../../components/backlog/StageInsertionPoint';
import CreateStageModal from '../../components/backlog/CreateStageModal';
import CreateTaskModal from '../../components/backlog/CreateTaskModal';
import TaskDetailModal from '../../components/task/TaskDetailModal';

const sortStages = (stages) =>
  [...stages].sort((a, b) => {
    const rank = (s) => {
      if (s.sprintStatus === 'ACTIVE') return 0;
      if (s.sprintStatus === 'PLANNED') return 1;
      return 2 + (s.sortOrder ?? 0);
    };
    return rank(a) - rank(b);
  });

export default function BacklogTab({ projectId }) {
  const { backlog, isLoading, error, fetchBacklog, moveTaskOptimistic } = useBacklog(projectId);
  const { createStage, renameStage, deleteStage, reorderStages } = useWorkStages(projectId);
  const { createTask } = useTasks(projectId);

  const [showCreateStage, setShowCreateStage] = useState(false);
  const [showCreateTask, setShowCreateTask] = useState(false);
  const [activeTask, setActiveTask] = useState(null);
  const [activeStage, setActiveStage] = useState(null);
  const [activeType, setActiveType] = useState(null);
  const [detailTaskId, setDetailTaskId] = useState(null);
  const [members, setMembers] = useState([]);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );

  const loadData = useCallback(() => {
    fetchBacklog();
  }, [fetchBacklog]);

  useEffect(() => {
    loadData();
    api.get(`/projects/${projectId}/members`).then(res => setMembers(res.data)).catch(() => {});
  }, [projectId, loadData]);

  const handleMoveTask = async (taskId, stageId) => {
    await moveTaskOptimistic(taskId, stageId);
  };

  const handleCreateStage = async (name) => {
    const result = await createStage(name);
    if (result.success) {
      fetchBacklog();
    }
    return result;
  };

  const handleRenameStage = (stage) => {
    const name = prompt('Rename stage:', stage.name);
    if (name && name.trim() && name.trim() !== stage.name) {
      renameStage(stage.id, name.trim()).then(r => {
        if (r.success) fetchBacklog();
      });
    }
  };

  const handleDeleteStage = (stage) => {
    if (confirm(`Delete "${stage.name}"? Tasks will be moved to Backlog.`)) {
      deleteStage(stage.id).then(r => {
        if (r.success) fetchBacklog();
      });
    }
  };

  const handleCreateTask = async (taskData) => {
    const result = await createTask(taskData);
    if (result.success) {
      fetchBacklog();
    }
    return result;
  };

  const handleTaskUpdated = () => {
    fetchBacklog();
  };

  const handleDragStart = (event) => {
    const data = event.active.data?.current;
    setActiveType(data?.type);
    if (data?.type === 'stage-drag') {
      const stage = (backlog.stages || []).find(s => s.id === data.stageId);
      if (stage) { setActiveStage(stage); return; }
    }
    const allStages = backlog.stages || [];
    for (const stage of allStages) {
      const task = (stage.tasks || []).find(t => t.id === event.active.id);
      if (task) { setActiveTask(task); return; }
    }
  };

  const handleDragEnd = async (event) => {
    const data = event.active.data?.current;
    setActiveTask(null);
    setActiveStage(null);
    setActiveType(null);
    const { active, over } = event;
    if (!over) return;

    if (data?.type === 'stage-drag') {
      const draggedStageId = data.stageId;
      const overType = over.data?.current?.type;

      if (overType === 'insertion') {
        const insertIdx = over.data?.current?.index;
        const sortedStages = sortStages(backlog.stages || []);
        const fromIdx = sortedStages.findIndex(s => s.id === draggedStageId);
        if (fromIdx === -1) return;

        const newOrder = [...sortedStages];
        const [moved] = newOrder.splice(fromIdx, 1);
        newOrder.splice(insertIdx, 0, moved);

        const result = await reorderStages(newOrder.map(s => s.id));
        if (result.success) {
          fetchBacklog();
        }
        return;
      }

      let targetStageId = overType === 'stage' ? over.id : null;
      if (!targetStageId || targetStageId === draggedStageId) return;

      const sortedStages = sortStages(backlog.stages || []);
      const fromIdx = sortedStages.findIndex(s => s.id === draggedStageId);
      const toIdx = sortedStages.findIndex(s => s.id === targetStageId);
      if (fromIdx === -1 || toIdx === -1) return;

      const newOrder = [...sortedStages];
      const [moved] = newOrder.splice(fromIdx, 1);
      newOrder.splice(toIdx, 0, moved);

      const result = await reorderStages(newOrder.map(s => s.id));
      if (result.success) {
        fetchBacklog();
      }
      return;
    }

    const task = (backlog.stages || []).flatMap(s => s.tasks || []).find(t => t.id === active.id);
    if (!task) return;

    let targetStageId = over.data?.current?.type === 'stage' ? over.id : null;

    if (!targetStageId) {
      for (const stage of backlog.stages) {
        if ((stage.tasks || []).some(t => t.id === over.id)) {
          targetStageId = stage.id;
          break;
        }
      }
    }

    if (targetStageId && targetStageId !== task.workStageId) {
      handleMoveTask(task.id, targetStageId);
    }
  };

  const allTasks = (backlog.stages || []).flatMap(s => s.tasks || []);
  const sortedStages = sortStages(backlog.stages || []);
  const isStageDragging = activeType === 'stage-drag';

  if (isLoading && backlog.stages.length === 0) {
    return (
      <div className="max-w-5xl mx-auto py-12 px-4 space-y-4">
        {[1, 2, 3].map(i => (
          <div key={i} className="h-24 bg-slate-100 rounded-2xl animate-pulse" />
        ))}
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto py-8 px-4 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-xl font-bold text-slate-900">Backlog</h3>
          <p className="text-sm text-slate-500 font-medium">Manage project work stages and tasks</p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowCreateStage(true)}
            className="flex items-center gap-2 bg-white border border-slate-200 text-slate-700 px-4 py-2.5 rounded-xl font-bold text-sm hover:bg-slate-50 transition-all shadow-sm"
          >
            <Plus size={18} /> New Stage
          </button>
          <button
            onClick={() => setShowCreateTask(true)}
            className="flex items-center gap-2 bg-indigo-600 text-white px-5 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100"
          >
            <Plus size={18} /> New Task
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-100 rounded-2xl flex items-center gap-3 text-red-600 text-sm font-medium">
          <AlertCircle size={18} /> {error}
        </div>
      )}

      <DndContext
        sensors={sensors}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <div className="space-y-0">
          {sortedStages.length === 0 ? (
            <div className="bg-white rounded-[2rem] border border-slate-100 shadow-sm py-24 text-center">
              <div className="w-16 h-16 bg-slate-50 text-slate-300 rounded-2xl flex items-center justify-center mx-auto mb-4">
                <AlertCircle size={32} />
              </div>
              <p className="text-slate-500 font-bold">No stages yet</p>
              <p className="text-slate-400 text-sm mt-1">Create a stage to organize your work.</p>
            </div>
          ) : (
            <>
              <StageInsertionPoint index={0} show={isStageDragging} />
              {sortedStages.map((stage, i) => (
                <React.Fragment key={stage.id}>
                  <StageSection
                    stage={stage}
                    tasks={stage.tasks || []}
                    stages={backlog.stages}
                    onRename={() => handleRenameStage(stage)}
                    onDelete={() => handleDeleteStage(stage)}
                    onMoveTask={handleMoveTask}
                    onTaskClick={setDetailTaskId}
                  />
                  <StageInsertionPoint index={i + 1} show={isStageDragging} />
                </React.Fragment>
              ))}
            </>
          )}
        </div>

        <DragOverlay>
          {activeStage ? (
            <div className="flex items-center gap-3 px-4 py-3 bg-white rounded-xl shadow-lg border border-indigo-300 rotate-1 opacity-90">
              <GripVertical size={16} className="text-slate-400" />
              <p className="text-sm font-bold text-slate-700">{activeStage.name}</p>
              <span className="text-[10px] font-bold text-slate-400 bg-slate-200 px-2 py-0.5 rounded-full">
                {(backlog.stages || []).find(s => s.id === activeStage.id)?.tasks?.length || 0}
              </span>
            </div>
          ) : activeTask ? (
            <div className="bg-white p-3 rounded-xl shadow-lg border border-indigo-300 rotate-2 opacity-90 flex items-center gap-3">
              <p className="text-sm font-bold text-slate-700">{activeTask.title}</p>
            </div>
          ) : null}
        </DragOverlay>
      </DndContext>

      <CreateStageModal
        isOpen={showCreateStage}
        onClose={() => setShowCreateStage(false)}
        onCreate={handleCreateStage}
      />

      <CreateTaskModal
        isOpen={showCreateTask}
        onClose={() => setShowCreateTask(false)}
        onCreate={handleCreateTask}
        stages={backlog.stages}
        members={members}
      />

      <TaskDetailModal
        projectId={projectId}
        taskId={detailTaskId}
        isOpen={!!detailTaskId}
        onClose={() => setDetailTaskId(null)}
        onUpdated={handleTaskUpdated}
        members={members}
      />
    </div>
  );
}
