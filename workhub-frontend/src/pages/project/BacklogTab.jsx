import React, { useEffect, useState, useCallback } from 'react';
import { Plus, AlertCircle } from 'lucide-react';
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
  const [renamingStage, setRenamingStage] = useState(null);

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
          backlog.stages.map(stage => (
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
