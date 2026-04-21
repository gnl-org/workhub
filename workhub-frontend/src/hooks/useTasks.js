import { useState, useCallback } from 'react';
import api from '../api/axios';

export const useTasks = (projectId) => {
  const [tasks, setTasks] = useState([]);
  const [pageInfo, setPageInfo] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const fetchTasks = useCallback(async (filters = {}, page = 0, size = 10) => {
    if (!projectId) return;
    setIsLoading(true);
    setError(null);
    try {
      const res = await api.get(`/projects/${projectId}/tasks`, {
        params: {
          ...filters,
          page,
          size,
          sort: 'createdAt,desc'
        }
      });

      setTasks(res.data.content || []);
      setPageInfo({
        totalElements: res.data.totalElements,
        totalPages: res.data.totalPages,
        last: res.data.last,
        number: res.data.number
      });
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load tasks");
    } finally {
      setIsLoading(false);
    }
  }, [projectId]);

  const createTask = async (taskData) => {
    setIsSubmitting(true);
    try {
      const res = await api.post(`/projects/${projectId}/tasks`, taskData);
      setTasks(prev => [res.data, ...prev]);
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || "Task creation failed" };
    } finally {
      setIsSubmitting(false);
    }
  };

  const updateTask = async (taskId, updateData) => {
    setIsSubmitting(true);
    try {
      const res = await api.patch(`/projects/${projectId}/tasks/${taskId}`, updateData);
      setTasks(prev => prev.map(t => t.id === taskId ? res.data : t));
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: "Update failed" };
    } finally {
      setIsSubmitting(false);
    }
  };

  const removeTask = async (taskId) => {
    try {
      await api.delete(`/projects/${projectId}/tasks/${taskId}`);
      setTasks(prev => prev.filter(t => t.id !== taskId));
      return { success: true };
    } catch (err) {
      return { success: false, error: "Delete failed" };
    }
  };

  return {
    tasks,
    pageInfo,
    isLoading,
    isSubmitting,
    error,
    fetchTasks,
    createTask,
    updateTask,
    removeTask
  };
};