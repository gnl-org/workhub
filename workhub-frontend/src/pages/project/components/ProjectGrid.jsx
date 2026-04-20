import React from 'react';
import ProjectCard from './ProjectCard';
import { useNavigate } from 'react-router-dom';
import { LayoutGrid } from 'lucide-react';

export default function ProjectGrid({ projects, isLoading }) {
  const navigate = useNavigate();

  if (!isLoading && projects.length === 0) {
    return (
      <div className="max-w-7xl mx-auto py-20 text-center bg-slate-50/50 rounded-[3rem] border-2 border-dashed border-slate-100">
        <div className="inline-flex p-4 bg-white rounded-2xl shadow-sm text-slate-300 mb-4">
          <LayoutGrid size={40} />
        </div>
        <h3 className="text-xl font-bold text-slate-900">No projects found</h3>
        <p className="text-slate-500 mt-2">Get started by creating your first WorkHub project.</p>
      </div>
    );
  }

  return (
    <main className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
      {projects.map((project) => (
        <ProjectCard 
          key={project.id} 
          project={project} 
          onClick={() => navigate(`/projects/${project.id}`)} 
        />
      ))}
    </main>
  );
}