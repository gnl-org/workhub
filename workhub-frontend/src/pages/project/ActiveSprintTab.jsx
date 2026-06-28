import React, { useEffect, useState, useCallback } from 'react';
import api from '../../api/axios';
import { useSprints } from '../../hooks/useSprints';
import SprintToolbar from '../../components/sprint/SprintToolbar';
import SprintEmptyState from '../../components/sprint/SprintEmptyState';
import SprintManageDrawer from '../../components/sprint/SprintManageDrawer';
import CloseSprintModal from '../../components/sprint/CloseSprintModal';
import KanbanBoard from '../../components/sprint/KanbanBoard';
import TaskDetailModal from '../../components/task/TaskDetailModal';

export default function ActiveSprintTab({ projectId }) {
  const {
    activeSprint, sprints, isLoading, error,
    fetchActiveSprint, fetchSprints,
    createSprint, startSprint, closeSprint,
    updateTaskStatus
  } = useSprints(projectId);

  const [tasks, setTasks] = useState([]);
  const [showManageDrawer, setShowManageDrawer] = useState(false);
  const [showCloseModal, setShowCloseModal] = useState(false);
  const [isClosing, setIsClosing] = useState(false);
  const [detailTaskId, setDetailTaskId] = useState(null);
  const [members, setMembers] = useState([]);

  const loadData = useCallback(async () => {
    const as = await fetchActiveSprint();
    await fetchSprints();
    if (as) {
      try {
        const res = await api.get(`/projects/${projectId}/sprints/${as.id}`);
        setTasks(res.data.tasks || []);
      } catch (e) {
        setTasks([]);
      }
    }
  }, [projectId, fetchActiveSprint, fetchSprints]);

  useEffect(() => {
    loadData();
    api.get(`/projects/${projectId}/members`).then(res => setMembers(res.data)).catch(() => {});
  }, [projectId, loadData]);

  const handleStartSprint = async (sprintId) => {
    const result = await startSprint(sprintId);
    if (result.success) {
      await loadData();
    }
    return result;
  };

  const handleCloseSprint = async () => {
    if (!activeSprint) return;
    setIsClosing(true);
    const result = await closeSprint(activeSprint.id);
    setIsClosing(false);
    setShowCloseModal(false);
    if (result.success) {
      setTasks([]);
      await fetchSprints();
    }
  };

  const handleStatusChange = async (taskId, newStatus) => {
    const prevTasks = [...tasks];
    setTasks(prev => prev.map(t => t.id === taskId ? { ...t, status: newStatus } : t));
    const result = await updateTaskStatus(taskId, newStatus);
    if (!result.success) {
      setTasks(prevTasks);
      const as = await fetchActiveSprint();
      if (as) {
        try {
          const res = await api.get(`/projects/${projectId}/sprints/${as.id}`);
          setTasks(res.data.tasks || []);
        } catch (e) {
          setTasks([]);
        }
      }
    }
  };

  if (isLoading && !activeSprint) {
    return (
      <div className="h-full flex items-center justify-center">
        <div className="text-slate-400 font-bold text-sm">Loading sprint...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="h-full flex items-center justify-center p-6">
        <div className="bg-red-50 text-red-700 px-6 py-4 rounded-2xl text-sm font-bold">
          {error}
        </div>
      </div>
    );
  }

  if (!activeSprint) {
    return (
      <div className="h-full flex flex-col p-6">
        <SprintEmptyState onCreateSprint={() => setShowManageDrawer(true)} />
        {showManageDrawer && (
          <SprintManageDrawer
            projectId={projectId}
            sprints={sprints}
            onClose={() => setShowManageDrawer(false)}
            onStart={handleStartSprint}
            onCreated={() => { fetchSprints(); loadData(); }}
          />
        )}
      </div>
    );
  }

  const incompleteCount = tasks.filter(t =>
    !['COMPLETED', 'CANCELLED'].includes(t.status)
  ).length;

  return (
    <div className="h-full flex flex-col p-6 overflow-hidden">
      <SprintToolbar
        sprint={activeSprint}
        onStart={() => { }}
        onComplete={() => setShowCloseModal(true)}
        onManage={() => setShowManageDrawer(true)}
      />

      <KanbanBoard
        tasks={tasks}
        onStatusChange={handleStatusChange}
        onTaskClick={setDetailTaskId}
      />

      {showManageDrawer && (
        <SprintManageDrawer
          projectId={projectId}
          sprints={sprints}
          onClose={() => setShowManageDrawer(false)}
          onStart={handleStartSprint}
          onCreated={() => { fetchSprints(); loadData(); }}
        />
      )}

      {showCloseModal && (
        <CloseSprintModal
          sprint={activeSprint}
          incompleteCount={incompleteCount}
          onConfirm={handleCloseSprint}
          onCancel={() => setShowCloseModal(false)}
          isClosing={isClosing}
        />
      )}

      <TaskDetailModal
        projectId={projectId}
        taskId={detailTaskId}
        isOpen={!!detailTaskId}
        onClose={() => setDetailTaskId(null)}
        onUpdated={loadData}
        members={members}
      />
    </div>
  );
}
