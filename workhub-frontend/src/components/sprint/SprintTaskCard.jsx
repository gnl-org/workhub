import React, { useState } from 'react';
import { MoreHorizontal, ArrowRight, ArrowLeft, GripVertical } from 'lucide-react';
import { useDraggable } from '@dnd-kit/core';

const priorityColors = {
  HIGH: 'text-red-600',
  MEDIUM: 'text-amber-600',
  LOW: 'text-blue-600',
};

const NEXT_STATUS = {
  OPEN: 'BLOCKED',
  BLOCKED: 'IN_PROGRESS',
  IN_PROGRESS: 'IN_REVIEW',
  IN_REVIEW: 'COMPLETED',
};

const PREV_STATUS = {
  BLOCKED: 'OPEN',
  IN_PROGRESS: 'BLOCKED',
  IN_REVIEW: 'IN_PROGRESS',
  COMPLETED: 'IN_REVIEW',
};

export default function SprintTaskCard({ task, onStatusChange, onClick }) {
  const [menuOpen, setMenuOpen] = useState(false);

  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: task.id,
    data: { status: task.status },
  });

  const initials = task.assigneeName
    ? task.assigneeName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
    : '??';

  const nextStatus = NEXT_STATUS[task.status];
  const prevStatus = PREV_STATUS[task.status];

  const style = transform ? {
    transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
  } : undefined;

  return (
    <div
      ref={setNodeRef}
      style={style}
      onClick={onClick}
      className={`bg-white p-4 rounded-xl shadow-sm border transition-colors group relative cursor-pointer ${
        isDragging ? 'border-indigo-400 shadow-lg opacity-50' : 'border-slate-200 hover:border-indigo-300'
      }`}
    >
      <div className="flex justify-between items-start mb-2">
        <button
          {...listeners}
          {...attributes}
          className="p-0.5 rounded text-slate-300 hover:text-slate-500 hover:bg-slate-100 transition cursor-grab active:cursor-grabbing -ml-1"
        >
          <GripVertical size={14} />
        </button>
        <span className="text-[10px] font-mono text-slate-400 group-hover:text-indigo-500 font-bold transition">
          {task.title?.slice(0, 2).toUpperCase() || 'WH'}-{task.id?.toString().slice(0, 4)}
        </span>
        <div className="relative">
          <button
            onClick={(e) => { e.stopPropagation(); setMenuOpen(!menuOpen); }}
            className="opacity-0 group-hover:opacity-100 transition-opacity p-0.5 rounded hover:bg-slate-100"
          >
            <MoreHorizontal size={14} className="text-slate-300 hover:text-slate-600" />
          </button>
          {menuOpen && (
            <div className="absolute right-0 top-6 bg-white border border-slate-200 rounded-xl shadow-lg py-1 min-w-[160px] z-20">
              {prevStatus && (
                <button
                  onClick={() => { onStatusChange?.(task.id, prevStatus); setMenuOpen(false); }}
                  className="flex items-center gap-2 w-full px-4 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50 transition"
                >
                  <ArrowLeft size={12} /> Move to {prevStatus.replace('_', ' ')}
                </button>
              )}
              {nextStatus && (
                <button
                  onClick={() => { onStatusChange?.(task.id, nextStatus); setMenuOpen(false); }}
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
