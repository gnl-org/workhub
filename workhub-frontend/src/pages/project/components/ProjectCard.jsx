import React from 'react';
import { Briefcase, ArrowRight } from 'lucide-react';

export default function ProjectCard({ project, onClick }) {
  return (
    <div 
      onClick={onClick}
      className="bg-white p-8 rounded-[2.5rem] border border-slate-100 shadow-sm hover:shadow-2xl hover:shadow-indigo-100/40 transition-all cursor-pointer group relative overflow-hidden"
    >
      <div className="absolute top-0 right-0 w-24 h-24 bg-indigo-50 rounded-bl-[4rem] -mr-8 -mt-8 transition-colors group-hover:bg-indigo-600/10" />
      
      <div className="relative z-10">
        <div className="p-4 bg-slate-50 text-slate-400 rounded-2xl inline-block mb-6 group-hover:bg-indigo-600 group-hover:text-white transition-colors shadow-sm">
          <Briefcase size={28} />
        </div>
        
        <div className="mb-2">
          <span className="text-[10px] font-black px-2.5 py-1 bg-indigo-50 text-indigo-600 rounded-lg uppercase tracking-wider">
            {project.status || 'ACTIVE'}
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
          <div className="w-10 h-10 rounded-full bg-slate-900 flex items-center justify-center text-white transition-all group-hover:bg-indigo-600 group-hover:translate-x-1">
            <ArrowRight size={18} />
          </div>
        </div>
      </div>
    </div>
  );
}