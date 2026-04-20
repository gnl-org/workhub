import React, { use, useEffect } from 'react';
import { Plus } from 'lucide-react';
import { useModal } from '../hooks/useModal';
import { useProjects } from '../hooks/useProjects';

// Components
import ProjectForm from './project/components/ProjectForm';
import ProjectGrid from './project/components/ProjectGrid';
import Modal from '../components/Modal';

export default function Dashboard() {
  const { isOpen, open, close } = useModal();
  const { projects, isSubmitting, createProject, isLoading, refresh, error } = useProjects();

  useEffect(() => {
    refresh();
  }, []);

  const handleCreateSubmit = async (formData) => {
    const result = await createProject(formData);
    if (result.success) {
      close();
    } else {
      alert(result.error);
    }
  };

  return (
    <div className="p-8 lg:p-12 min-h-screen bg-[#fcfdfe]">
      <header className="max-w-7xl mx-auto flex justify-between items-end mb-12">
        <div>
          <h1 className="text-4xl font-black text-slate-900 tracking-tight">Workspace</h1>
          <p className="text-slate-500 font-medium mt-1">Manage active projects and team progress.</p>
        </div>
        <button 
          onClick={open}
          className="bg-indigo-600 text-white px-8 py-4 rounded-2xl font-bold flex items-center gap-2 hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-100"
        >
          <Plus size={20} /> New Project
        </button>
      </header>

      {/* Grid handles its own loading state internally now if you pass isLoading */}
      <ProjectGrid projects={projects} isLoading={isLoading} error={error} refresh={refresh} />

      <Modal 
        isOpen={isOpen} 
        onClose={close}
        title="New Project"
        isPreventClose={isSubmitting}
      >
        <ProjectForm 
          onSubmit={handleCreateSubmit}
          onCancel={close}
          isSubmitting={isSubmitting}
        />
      </Modal>
    </div>
  );
}