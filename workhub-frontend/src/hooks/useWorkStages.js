import { useState, useCallback } from 'react';
import api from '../api/axios';

export const useWorkStages = (projectId) => {
  const [stages, setStages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchStages = useCallback(async () => {
    if (!projectId) return;
    setIsLoading(true);
    setError(null);
    try {
      const res = await api.get(`/projects/${projectId}/work-stages`);
      setStages(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load stages');
    } finally {
      setIsLoading(false);
    }
  }, [projectId]);

  const createStage = async (name) => {
    try {
      const res = await api.post(`/projects/${projectId}/work-stages`, { name });
      setStages(prev => [...prev, res.data]);
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to create stage' };
    }
  };

  const renameStage = async (stageId, name) => {
    try {
      const res = await api.patch(`/projects/${projectId}/work-stages/${stageId}`, { name });
      setStages(prev => prev.map(s => s.id === stageId ? res.data : s));
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to rename stage' };
    }
  };

  const deleteStage = async (stageId) => {
    try {
      await api.delete(`/projects/${projectId}/work-stages/${stageId}`);
      setStages(prev => prev.filter(s => s.id !== stageId));
      return { success: true };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || 'Failed to delete stage' };
    }
  };

  const reorderStages = async (stageIds) => {
    try {
      await api.put(`/projects/${projectId}/work-stages/reorder`, { stageIds });
      return { success: true };
    } catch (err) {
      return { success: false, error: 'Failed to reorder stages' };
    }
  };

  return { stages, isLoading, error, fetchStages, createStage, renameStage, deleteStage, reorderStages };
};
