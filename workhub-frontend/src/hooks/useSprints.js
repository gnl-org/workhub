import { useState, useCallback } from 'react';
import api from '../api/axios';

export const useSprints = (projectId) => {
  const [activeSprint, setActiveSprint] = useState(null);
  const [sprints, setSprints] = useState([]);
  const [history, setHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchActiveSprint = useCallback(async () => {
    if (!projectId) return;
    setIsLoading(true);
    setError(null);
    try {
      const res = await api.get(`/api/v1/projects/${projectId}/sprints/active`);
      setActiveSprint(res.data);
      return res.data;
    } catch (err) {
      if (err.response?.status === 404) {
        setActiveSprint(null);
      } else {
        setError(err.response?.data?.message || 'Failed to load active sprint');
      }
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [projectId]);

  const fetchSprints = useCallback(async () => {
    if (!projectId) return;
    setIsLoading(true);
    setError(null);
    try {
      const res = await api.get(`/api/v1/projects/${projectId}/sprints`);
      setSprints(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load sprints');
    } finally {
      setIsLoading(false);
    }
  }, [projectId]);

  const fetchHistory = useCallback(async () => {
    if (!projectId) return;
    setIsLoading(true);
    setError(null);
    try {
      const res = await api.get(`/api/v1/projects/${projectId}/sprints/history`);
      setHistory(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load sprint history');
    } finally {
      setIsLoading(false);
    }
  }, [projectId]);

  const createSprint = async (data = {}) => {
    try {
      const res = await api.post(`/api/v1/projects/${projectId}/sprints`, data);
      setSprints(prev => [res.data, ...prev]);
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to create sprint' };
    }
  };

  const startSprint = async (sprintId) => {
    try {
      const res = await api.post(`/api/v1/projects/${projectId}/sprints/${sprintId}/start`);
      setActiveSprint(res.data);
      return { success: true, data: res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Failed to start sprint';
      return { success: false, error: msg };
    }
  };

  const closeSprint = async (sprintId) => {
    try {
      const res = await api.post(`/api/v1/projects/${projectId}/sprints/${sprintId}/close`);
      setActiveSprint(null);
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to close sprint' };
    }
  };

  const assignTasks = async (sprintId, taskIds) => {
    try {
      await api.post(`/api/v1/projects/${projectId}/sprints/${sprintId}/tasks`, { taskIds });
      return { success: true };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to assign tasks' };
    }
  };

  const removeTasks = async (sprintId, taskIds) => {
    try {
      await api.delete(`/api/v1/projects/${projectId}/sprints/${sprintId}/tasks`, { data: { taskIds } });
      return { success: true };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to remove tasks' };
    }
  };

  const getSprintDetail = async (sprintId) => {
    try {
      const res = await api.get(`/api/v1/projects/${projectId}/sprints/${sprintId}`);
      return res.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load sprint detail');
      return null;
    }
  };

  const fetchActiveSprintTasks = useCallback(async () => {
    if (!activeSprint?.id) return [];
    const detail = await getSprintDetail(activeSprint.id);
    return detail?.tasks || [];
  }, [projectId, activeSprint?.id]);

  const updateTaskStatus = async (taskId, status) => {
    try {
      const res = await api.patch(`/api/v1/projects/${projectId}/tasks/${taskId}`, { status });
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to update task' };
    }
  };

  return {
    activeSprint, sprints, history, isLoading, error,
    fetchActiveSprint, fetchSprints, fetchHistory,
    createSprint, startSprint, closeSprint,
    assignTasks, removeTasks,
    fetchActiveSprintTasks, updateTaskStatus, getSprintDetail
  };
};
