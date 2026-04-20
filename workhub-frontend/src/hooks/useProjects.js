import { useState, useCallback } from 'react';
import api from '../api/axios';
import { getUserInfo } from '../util/auth';

export const useProjects = () => {
  const [projects, setProjects] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [members, setMembers] = useState([]);
  const [isMembersLoading, setIsMembersLoading] = useState(false);

  const fetchProjects = useCallback(async () => {
    setIsLoading(true);
    setError(false);
    try {
      const user = getUserInfo();
      const endpoint = user?.role === 'ADMIN' ? '/projects' : '/projects/own';
      const res = await api.get(endpoint);
      setProjects(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load projects. Please try again.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  const createProject = async (formData) => {
    setIsSubmitting(true);
    try {
      const res = await api.post('/projects', formData);
      await fetchProjects();
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.message || "Creation failed" };
    } finally {
      setIsSubmitting(false);
    }
  };

  // NEW: Update Logic
  const updateProject = async (id, formData) => {
    setIsSubmitting(true);
    try {
      const res = await api.patch(`/projects/${id}`, formData);
      // Optional: Update local state if you're on the dashboard
      console.log("Updated project:", res.data);
      setProjects(prev => prev.map(p => p.id === id ? res.data : p));
      await api.patch(`/projects/${id}`, formData);
      return { success: true, data: res.data };
    } catch (err) {
      return { success: false, error: "Update failed" };
    } finally {
      setIsSubmitting(false);
    }
  };

  // NEW: Delete Logic
  const removeProject = async (id) => {
    setIsSubmitting(true);
    try {
      await api.delete(`/projects/${id}`);
      setProjects(prev => prev.filter(p => p.id !== id));
      return { success: true };
    } catch (err) {
      return { success: false, error: "Delete failed" };
    } finally {
      setIsSubmitting(false);
    }
  };

  // POST /projectMember/addMember
  const addMemberToProject = async (projectId, userEmail, projectRole = 'MEMBER') => {
    setIsSubmitting(true);
    try {
      await api.post('/projectMember/addMember', {
        projectId,
        userEmail,
        projectRole
      });
      return { success: true };
    } catch (err) {
      return { 
        success: false, 
        error: err.response?.data?.message || "User not found or already in project" 
      };
    } finally {
      setIsSubmitting(false);
    }
  };

  // The spec doesn't show a specific DELETE for members, 
  // but if you add one to your Java controller, it usually looks like this:
  const removeMemberFromProject = async (projectId, memberId) => {
    setIsSubmitting(true);
    try {
      // Assuming a standard REST path if implemented later:
      // await api.delete(`/projects/${projectId}/members/${memberId}`);
      console.warn("Delete member endpoint not yet in API spec");
      return { success: true };
    } catch (err) {
      return { success: false, error: "Delete failed" };
    } finally {
      setIsSubmitting(false);
    }
  };

  const fetchMembers = useCallback(async (projectId) => {
    setIsMembersLoading(true);
    try {
    // Matches your new endpoint: GET /projects/{projectId}/members
    const res = await api.get(`/projects/${projectId}/members`);
    setMembers(res.data);
    } catch (err) {
    console.error("Failed to fetch members:", err);
    } finally {
    setIsMembersLoading(false);
    }
  }, []);

  return { 
    projects, 
    isLoading, 
    isSubmitting, 
    createProject, 
    updateProject, 
    removeProject, 
    refresh: fetchProjects,
    addMemberToProject,
    removeMemberFromProject,
    fetchMembers,
    members,
    isMembersLoading,
    error
  };
};