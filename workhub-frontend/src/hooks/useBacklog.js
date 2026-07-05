import { useState, useCallback } from 'react';
import api from '../api/axios';

export const useBacklog = (projectId) => {
  const [backlog, setBacklog] = useState({ stages: [] });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchBacklog = useCallback(async () => {
    if (!projectId) return;
    setIsLoading(true);
    setError(null);
    try {
      const res = await api.get(`/api/v1/projects/${projectId}/backlog`);
      setBacklog(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load backlog');
    } finally {
      setIsLoading(false);
    }
  }, [projectId]);

  const moveTask = async (taskId, workStageId, sortOrder) => {
    try {
      const res = await api.patch(`/api/v1/projects/${projectId}/tasks/${taskId}/move`, {
        workStageId,
        sortOrder
      });
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to move task' };
    }
  };

  const moveTaskOptimistic = async (taskId, workStageId) => {
    const prevStages = backlog.stages;

    setBacklog(prev => {
      const movedTask = prev.stages.flatMap(s => s.tasks || []).find(t => t.id === taskId);
      if (!movedTask) return prev;

      const targetStage = prev.stages.find(s => s.id === workStageId);

      const removeFrom = (stages) => stages.map(s => ({
        ...s,
        tasks: (s.tasks || []).filter(t => t.id !== taskId)
      }));

      const addTo = (stages) => stages.map(s =>
        s.id === workStageId
          ? {
              ...s,
              tasks: [...(s.tasks || []), {
                ...movedTask,
                workStageId,
                sprintId: targetStage?.sprintId || null,
                inActiveSprint: targetStage?.sprintStatus === 'ACTIVE'
              }]
            }
          : s
      );

      return { ...prev, stages: addTo(removeFrom(prev.stages)) };
    });

    try {
      const res = await api.patch(`/api/v1/projects/${projectId}/tasks/${taskId}/move`, { workStageId });
      setBacklog(prev => {
        const updated = res.data;
        return {
          ...prev,
          stages: prev.stages.map(s => ({
            ...s,
            tasks: (s.tasks || []).map(t => t.id === taskId ? { ...t, ...updated } : t)
          }))
        };
      });
      return { success: true };
    } catch (err) {
      setBacklog(prev => ({ ...prev, stages: prevStages }));
      return { success: false, error: err.response?.data?.message || 'Failed to move task' };
    }
  };

  const reorderTasks = async (stageId, taskIds) => {
    try {
      await api.put(`/api/v1/projects/${projectId}/work-stages/${stageId}/tasks/reorder`, { taskIds });
      return { success: true };
    } catch (err) {
      return { success: false, error: 'Failed to reorder tasks' };
    }
  };

  return { backlog, isLoading, error, fetchBacklog, moveTask, moveTaskOptimistic, reorderTasks };
};
