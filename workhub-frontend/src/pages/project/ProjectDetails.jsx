import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Info, ListTodo, Boxes, ChevronLeft } from 'lucide-react';
import api from '../../api/axios';

// Tab Components
import InfoTab from './InfoTab';
import BacklogTab from './BacklogTab';
import ActiveSprintTab from './ActiveSprintTab';

export default function ProjectDetails() {
  const { projectId } = useParams();
  const [activeTab, setActiveTab] = useState('backlog');
  const [project, setProject] = useState(null);

  useEffect(() => {
    api.get(`/projects/${projectId}`).then(res => setProject(res.data));
  }, [projectId]);

  const tabs = [
    { id: 'backlog', label: 'Backlog', icon: ListTodo },
    { id: 'sprint', label: 'Active Sprint', icon: Boxes },
    { id: 'info', label: 'Project Info', icon: Info },
  ];

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Sub-Header / Navigation */}
      <nav className="border-b border-slate-200 px-8 py-4 flex items-center justify-between bg-white sticky top-0 z-10">
        <div className="flex items-center gap-4">
          <Link to="/" className="p-2 hover:bg-slate-100 rounded-lg text-slate-400 hover:text-slate-900 transition">
            <ChevronLeft size={20} />
          </Link>
          <div>
            <h2 className="font-bold text-slate-900">{project?.title}</h2>
            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest">Project Workspace</p>
          </div>
        </div>
        
        <div className="flex bg-slate-100 p-1 rounded-xl">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-bold transition-all ${
                activeTab === tab.id ? 'bg-white shadow-sm text-indigo-600' : 'text-slate-500 hover:text-slate-700'
              }`}
            >
              <tab.icon size={16} /> {tab.label}
            </button>
          ))}
        </div>
      </nav>

      {/* Dynamic Content */}
      <main className="flex-1 bg-slate-50/50">
        {activeTab === 'info' && <InfoTab projectId={projectId} project={project} updateProject={setProject} />}
        {activeTab === 'backlog' && <BacklogTab projectId={projectId} />}
        {activeTab === 'sprint' && <ActiveSprintTab />}
      </main>
    </div>
  );
}