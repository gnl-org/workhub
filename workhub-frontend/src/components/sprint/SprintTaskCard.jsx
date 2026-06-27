import React, { useState } from 'react';
import { MoreHorizontal, ArrowRight } from 'lucide-react';

const priorityColors = {
  HIGH: 'text-red-600',
  MEDIUM: 'text-amber-600',
  LOW: 'text-blue-600',
};

const NEXT_STATUS = {
  OPEN: 'IN_PROGRESS',
  IN_PROGRESS: 'IN_REVIEW',
  IN_REVIEW: 'COMPLETED',
};

export default function SprintTaskCard({ task, onStatusChange }) {
  const [menuOpen, setMenuOpen] = useState(false);

  const initials = task.assigneeName
    ? task.assigneeName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
    : '??';

  const nextStatus = NEXT_STATUS[task.status];

  return (
    <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200 hover:border-indigo-300 transition-colors group relative">
      <div className="flex justify-between items-start mb-2">
        <span className="text-[10px] font-mono text-slate-400 group-hover:text-indigo-500 font-bold transition">
          {task.title?.slice(0, 2).toUpperCase() || 'WH'}-{task.id?.toString().slice(0, 4)}
        </span>
        <div className="relative">
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            className="opacity-0 group-hover:opacity-100 transition-opacity"
          >
            <MoreHorizontal size={14} className="text-slate-300 hover:text-slate-600" />
          </button>
          {menuOpen && (
            <div className="absolute right-0 top-6 bg-white border border-slate-200 rounded-xl shadow-lg py-1 min-w-[160px] z-20">
              {nextStatus && (
                <button
                  onClick={() => { onStatusChange(task.id, nextStatus); setMenuOpen(false); }}
                  className="flex items-center gap-2 w-full px-4 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50 transition"
                >
                  <ArrowRight size={12} /> Move to {nextStatus.replace('_', ' ')}
                </button>
              )}
            </div>
          )}
        </div>
      </div>
      <p className="text-sm font-semibold text-slate-800 leading-tight mb-4">
        {task.title}
      </p>
      <div className="flex justify-between items-center">
        <div className={`text-[10px] font-black uppercase ${priorityColors[task.priority] || 'text-slate-400'}`}>
          {task.priority || 'NONE'}
        </div>
        <div className="w-6 h-6 rounded-full bg-slate-100 border border-white flex items-center justify-center text-[10px] font-bold text-slate-500" title={task.assigneeName}>
          {initials}
        </div>
      </div>
    </div>
  );
}
