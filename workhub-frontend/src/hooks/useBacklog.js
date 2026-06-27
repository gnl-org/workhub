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
      const res = await api.get(`/projects/${projectId}/backlog`);
      setBacklog(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load backlog');
    } finally {
      setIsLoading(false);
    }
  }, [projectId]);

  const moveTask = async (taskId, workStageId, sortOrder) => {
    try {
      const res = await api.patch(`/projects/${projectId}/tasks/${taskId}/move`, {
        workStageId,
        sortOrder
      });
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to move task' };
    }
  };

  const reorderTasks = async (stageId, taskIds) => {
    try {
      await api.put(`/projects/${projectId}/work-stages/${stageId}/tasks/reorder`, { taskIds });
      return { success: true };
    } catch (err) {
      return { success: false, error: 'Failed to reorder tasks' };
    }
  };

  return { backlog, isLoading, error, fetchBacklog, moveTask, reorderTasks };
};
