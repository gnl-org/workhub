import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useModal } from '../../hooks/useModal';
import { useProjects } from '../../hooks/useProjects';
import Modal from '../../components/Modal';
import ProjectDetailsCard from './components/ProjectDetailsCard';
import ProjectMembersCard from './components/ProjectMembersCard';
import ProjectForm from './components/ProjectForm';
import ConfirmModal from '../../components/ConfirmModal';

export default function InfoTab({ project, updateProject: updateProjectLocal }) {
  const navigate = useNavigate();
  const addMemberModal = useModal();
  const editModal = useModal();
  const deleteModal = useModal();
  
  const [email, setEmail] = useState('');
  const { 
    addMemberToProject, 
    isSubmitting, 
    fetchMembers, 
    members, 
    updateProject, 
    removeProject 
  } = useProjects();

  useEffect(() => {
    if (project?.id) {
      fetchMembers(project.id);
    }
  }, [project?.id, fetchMembers]);

  const handleUpdate = async (formData) => {
    const res = await updateProject(project.id, formData);
    if (res.success) {
      updateProjectLocal({ ...project, ...formData });
      editModal.close();
    } else {
      alert(res.error);
    }
  };

  const handleAddMember = async (e) => {
    e.preventDefault();
    const res = await addMemberToProject(project.id, email);
    if (res.success) {
      setEmail('');
      addMemberModal.close();
      fetchMembers(project.id);
    } else {
      alert(res.error);
    }
  };

  const handleDelete = async () => {
    const res = await removeProject(project.id);
    if (res.success) {
      deleteModal.close();
      navigate('/'); 
    } else {
      alert(res.error);
    }
  };

  if (!project) return null;

  console.log("Project InfoTab Rendered with project:", project);

  return (
    <div className="max-w-4xl mx-auto py-8 px-4 space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      
      <ProjectDetailsCard 
        project={project} 
        onEditClick={editModal.toggle} 
        onDeleteClick={deleteModal.toggle} 
      />

      <ProjectMembersCard 
        members={members || []} 
        onAddClick={addMemberModal.open}
        isAdmin={true} 
      />

      <Modal isOpen={editModal.isOpen} onClose={editModal.close} title="Edit Project Details">
        <div className="p-1">
          <ProjectForm 
            onSubmit={handleUpdate}
            onCancel={editModal.close} 
            isSubmitting={isSubmitting} 
            initialData={project} 
          />
        </div>
      </Modal>

      <ConfirmModal 
        isOpen={deleteModal.isOpen}
        onClose={deleteModal.close}
        onConfirm={handleDelete}
        isSubmitting={isSubmitting}
        title="Delete Project"
        message={`Are you sure you want to delete "${project.title}"? This will permanently remove all associated tasks and members.`}
        confirmText="Yes, Delete Project"
      />

      <Modal isOpen={addMemberModal.isOpen} onClose={addMemberModal.close} title="Invite Member">
        <form onSubmit={handleAddMember} className="space-y-6">
          <div className="p-1">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Email Address</label>
            <input 
              type="email" 
              required
              className="w-full mt-2 px-6 py-4 bg-slate-50 border border-slate-100 rounded-2xl outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 transition-all"
              placeholder="user@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <button 
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-indigo-600 text-white py-4 rounded-2xl font-bold hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 disabled:opacity-50 active:scale-[0.98]"
          >
            {isSubmitting ? "Inviting..." : "Add to Project"}
          </button>
        </form>
      </Modal>
    </div>
  );
}