import React from 'react';
import { Edit3, Trash2, Calendar, User } from 'lucide-react';

export default function ProjectDetailsCard({ project, onEditClick, onDeleteClick }) {
  return (
    <div className="bg-white rounded-[2.5rem] p-10 border border-slate-100 shadow-sm">
      <div className="flex justify-between items-start mb-8">
        <h2 className="text-2xl font-black text-slate-900">Project Details</h2>
        <div className="flex gap-2">
          <button 
            onClick={onEditClick}
            className="p-3 bg-slate-50 text-slate-600 rounded-xl hover:bg-indigo-50 hover:text-indigo-600 transition-all"
          >
            <Edit3 size={20} />
          </button>
          <button 
            onClick={onDeleteClick}
            className="p-3 bg-slate-50 text-red-400 rounded-xl hover:bg-red-50 hover:text-red-500 transition-all"
          >
            <Trash2 size={20} />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-10">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 bg-slate-50 rounded-2xl flex items-center justify-center text-slate-400">
            <User size={20} />
          </div>
          <div>
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Project Owner</p>
            <p className="font-bold text-slate-900">
              {project.ownerName || project.creator?.fullName || 'N/A'}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 bg-slate-50 rounded-2xl flex items-center justify-center text-slate-400">
            <Calendar size={20} />
          </div>
          <div>
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Status</p>
            <span className="text-xs font-black px-2 py-1 bg-indigo-50 text-indigo-600 rounded-lg uppercase">
              {project.status}
            </span>
          </div>
        </div>
      </div>

      <div className="space-y-4">
        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Description</label>
        <p className="text-slate-600 leading-relaxed font-medium">
          {project.description || "No description provided."}
        </p>
      </div>
    </div>
  );
}