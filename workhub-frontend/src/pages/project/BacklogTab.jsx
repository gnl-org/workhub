import React, { useEffect, useState, useCallback } from 'react';
import { Plus, AlertCircle } from 'lucide-react';
import { DndContext, DragOverlay, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { useBacklog } from '../../hooks/useBacklog';
import { useWorkStages } from '../../hooks/useWorkStages';
import { useTasks } from '../../hooks/useTasks';
import StageSection from '../../components/backlog/StageSection';
import CreateStageModal from '../../components/backlog/CreateStageModal';
import CreateTaskModal from '../../components/backlog/CreateTaskModal';

export default function BacklogTab({ projectId }) {
  const { backlog, isLoading, error, fetchBacklog, moveTask } = useBacklog(projectId);
  const { stages, fetchStages, createStage, renameStage, deleteStage } = useWorkStages(projectId);
  const { createTask } = useTasks(projectId);

  const [showCreateStage, setShowCreateStage] = useState(false);
  const [showCreateTask, setShowCreateTask] = useState(false);
  const [activeTask, setActiveTask] = useState(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );

  const loadData = useCallback(() => {
    fetchBacklog();
    fetchStages();
  }, [fetchBacklog, fetchStages]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleMoveTask = async (taskId, stageId) => {
    const result = await moveTask(taskId, stageId);
    if (result.success) {
      fetchBacklog();
    }
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

  const handleDragStart = (event) => {
    const allStages = backlog.stages || [];
    for (const stage of allStages) {
      const task = (stage.tasks || []).find(t => t.id === event.active.id);
      if (task) { setActiveTask(task); return; }
    }
  };

  const handleDragEnd = (event) => {
    setActiveTask(null);
    const { active, over } = event;
    if (!over) return;

    const task = (backlog.stages || []).flatMap(s => s.tasks || []).find(t => t.id === active.id);
    if (!task) return;

    let targetStageId = over.data?.current?.type === 'stage' ? over.id : null;

    if (!targetStageId) {
      // Dropped on another task — find its stage
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
        <div className="space-y-4">
          {backlog.stages.length === 0 ? (
            <div className="bg-white rounded-[2rem] border border-slate-100 shadow-sm py-24 text-center">
              <div className="w-16 h-16 bg-slate-50 text-slate-300 rounded-2xl flex items-center justify-center mx-auto mb-4">
                <AlertCircle size={32} />
              </div>
              <p className="text-slate-500 font-bold">No stages yet</p>
              <p className="text-slate-400 text-sm mt-1">Create a stage to organize your work.</p>
            </div>
          ) : (
            [...backlog.stages].sort((a, b) => {
              const rank = (s) => {
                if (s.sprintStatus === 'ACTIVE') return 0;
                if (s.sprintStatus === 'PLANNED') return 1;
                return 2 + s.sortOrder;
              };
              return rank(a) - rank(b);
            }).map(stage => (
              <StageSection
                key={stage.id}
                stage={stage}
                tasks={stage.tasks || []}
                stages={backlog.stages}
                onRename={() => handleRenameStage(stage)}
                onDelete={() => handleDeleteStage(stage)}
                onMoveTask={handleMoveTask}
              />
            ))
          )}
        </div>

        <DragOverlay>
          {activeTask ? (
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
      />
    </div>
  );
}
