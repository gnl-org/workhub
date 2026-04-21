import React from 'react';
import { Briefcase, ArrowRight } from 'lucide-react';

export default function ProjectCard({ project, onClick }) {
  return (
    <div 
      onClick={onClick}
      className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm hover:shadow-xl hover:shadow-indigo-100/30 transition-all cursor-pointer group relative overflow-hidden h-full flex flex-col"
    >
      <div className="absolute top-0 right-0 w-16 h-16 bg-indigo-50 rounded-bl-[2.5rem] -mr-4 -mt-4 transition-colors group-hover:bg-indigo-600/10" />
      
      <div className="relative z-10 flex flex-col h-full">
        <div className="w-fit self-start p-3 bg-slate-50 text-slate-400 rounded-xl mb-4 group-hover:bg-indigo-600 group-hover:text-white transition-colors shadow-sm flex items-center justify-center">
            <Briefcase size={20} />
        </div>
        
        <div className="mb-2">
          <span className="text-[9px] font-black px-2 py-0.5 bg-indigo-50 text-indigo-600 rounded-md uppercase tracking-wider">
            {project.status || 'ACTIVE'}
          </span>
        </div>
        
        <h3 className="text-lg font-bold text-slate-900 mb-2 group-hover:text-indigo-600 transition-colors line-clamp-1">
          {project.title}
        </h3>
        
        <p className="text-slate-500 text-xs leading-relaxed line-clamp-2 mb-6 flex-grow">
          {project.description}
        </p>
        
        <div className="flex items-center justify-between pt-4 border-t border-slate-50">
          <div className="flex flex-col">
            <span className="text-[10px] text-slate-400 font-bold uppercase tracking-tighter">Owner</span>
            <span className="text-xs font-bold text-slate-700">{project.ownerName}</span>
          </div>
          
          <div className="w-8 h-8 rounded-full bg-slate-900 flex items-center justify-center text-white transition-all group-hover:bg-indigo-600 group-hover:translate-x-1">
            <ArrowRight size={14} />
          </div>
        </div>
      </div>
    </div>
  );
}