import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, ArrowRight, Briefcase } from 'lucide-react';
import api from '../api/axios';
import { getUserInfo } from '../util/auth';

export default function Dashboard() {
  const [projects, setProjects] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const user = getUserInfo();
    const userRole = user?.role || 'USER'; 
    const endpoint = userRole === 'ADMIN' ? '/projects' : '/projects/own';

    api.get(endpoint)
        .then(res => setProjects(res.data))
        .catch(err => console.error("API Error:", err));
    }, []);

  return (
    <div className="p-8 lg:p-12">
      <header className="max-w-7xl mx-auto flex justify-between items-end mb-12">
        <div>
          <h1 className="text-4xl font-black text-slate-900 tracking-tight">Workspace</h1>
          <p className="text-slate-500 font-medium mt-1">Manage your active projects and team progress.</p>
        </div>
        <button className="bg-indigo-600 text-white px-6 py-3 rounded-2xl font-bold flex items-center gap-2 hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-100 active:scale-95">
          <Plus size={20} /> New Project
        </button>
      </header>

      <main className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
        {projects.map((project) => (
          <div 
            key={project.id}
            onClick={() => navigate(`/projects/${project.id}`)}
            className="bg-white p-8 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-2xl hover:shadow-indigo-100/50 transition-all cursor-pointer group relative overflow-hidden"
          >
            {/* Decorative accent */}
            <div className="absolute top-0 right-0 w-24 h-24 bg-indigo-50 rounded-bl-[4rem] -mr-8 -mt-8 transition-colors group-hover:bg-indigo-600/10" />
            
            <div className="relative z-10">
              <div className="p-4 bg-slate-50 text-slate-400 rounded-2xl inline-block mb-6 group-hover:bg-indigo-600 group-hover:text-white transition-colors shadow-sm">
                <Briefcase size={28} />
              </div>
              
              <div className="mb-2">
                <span className="text-[10px] font-black px-2.5 py-1 bg-indigo-50 text-indigo-600 rounded-lg uppercase tracking-wider">
                  {project.status}
                </span>
              </div>
              
              <h3 className="text-2xl font-bold text-slate-900 mb-3 group-hover:text-indigo-600 transition-colors">
                {project.title}
              </h3>
              
              <p className="text-slate-500 text-sm leading-relaxed line-clamp-2 mb-8">
                {project.description}
              </p>
              
              <div className="flex items-center justify-between pt-6 border-t border-slate-50">
                <span className="text-xs font-bold text-slate-400">Owner: {project.ownerName}</span>
                <div className="w-8 h-8 rounded-full bg-slate-900 flex items-center justify-center text-white transition-transform group-hover:translate-x-1">
                  <ArrowRight size={16} />
                </div>
              </div>
            </div>
          </div>
        ))}
      </main>
    </div>
  );
}