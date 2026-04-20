import React, { useState, useEffect } from 'react';
import { useModal } from '../../hooks/useModal';
import { useProjects } from '../../hooks/useProjects';
import Modal from '../../components/Modal';
import ProjectDetailsCard from './components/ProjectDetailsCard';
import ProjectMembersCard from './components/ProjectMembersCard';

export default function InfoTab({ project, onUpdate }) {
  const addMemberModal = useModal();
  const [email, setEmail] = useState('');
  const { addMemberToProject, isSubmitting, fetchMembers, members } = useProjects();

  useEffect(() => {
    if (project?.id) {
      fetchMembers(project.id);
    }
  }, [project?.id, fetchMembers]);

  const handleAddMember = async (e) => {
    e.preventDefault();
    const res = await addMemberToProject(project.id, email);
    if (res.success) {
      setEmail('');
      addMemberModal.close();
      onUpdate(); 
    } else {
      alert(res.error);
    }
  };

  if (!project) return null;

  return (
    /* Change 1: Max width and margin auto to center. Vertical space between cards. */
    <div className="max-w-4xl mx-auto py-8 px-4 space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
      
      {/* Detail Card on top */}
      <ProjectDetailsCard 
        project={project} 
        onEditClick={() => {/* handle edit */}} 
        onDeleteClick={() => {/* handle delete */}} 
      />

      {/* Members Card below */}
      <ProjectMembersCard 
        members={members || []} 
        onAddClick={addMemberModal.open}
        isAdmin={true} 
      />

      <Modal isOpen={addMemberModal.isOpen} onClose={addMemberModal.close} title="Invite Member">
        <form onSubmit={handleAddMember} className="space-y-6">
          <div className="p-1"> {/* Tiny padding to prevent input ring clipping */}
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