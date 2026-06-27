import React, { useState } from 'react';
import { CheckCircle2, ChevronRight, MoreHorizontal, ArrowRight, GripVertical } from 'lucide-react';
import { useDraggable } from '@dnd-kit/core';

export default function BacklogTaskRow({ task, stages, onMoveTask }) {
  const [menuOpen, setMenuOpen] = useState(false);

  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: task.id,
    data: { type: 'task', workStageId: task.workStageId },
  });

  const getPriorityStyle = (priority) => {
    switch (priority) {
      case 'CRITICAL': return 'bg-red-100 text-red-700 border-red-200';
      case 'HIGH': return 'bg-orange-100 text-orange-700 border-orange-200';
      case 'MEDIUM': return 'bg-blue-100 text-blue-700 border-blue-200';
      default: return 'bg-slate-100 text-slate-600 border-slate-200';
    }
  };

  const style = transform ? {
    transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
  } : undefined;

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`p-3 transition-all flex items-center justify-between group border-b border-slate-50 last:border-b-0 ${
        isDragging ? 'opacity-40 bg-indigo-50' : 'hover:bg-slate-50/50'
      }`}
    >
      <button
        {...listeners}
        {...attributes}
        className="p-1 mr-1 rounded text-slate-300 hover:text-slate-500 hover:bg-slate-100 transition opacity-0 group-hover:opacity-100 cursor-grab active:cursor-grabbing flex-shrink-0"
      >
        <GripVertical size={16} />
      </button>
      <div className="flex items-center gap-3 flex-1">
        <CheckCircle2 className={task.status === 'COMPLETED' ? 'text-emerald-500' : 'text-slate-200'} size={18} />

        <div className="flex flex-col">
          <h4 className="text-sm font-bold text-slate-700 group-hover:text-indigo-600 transition-colors">
            {task.title}
          </h4>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tighter">
                      {(task.status || 'OPEN').replace('_', ' ')}
                    </span>
            <span className="w-1 h-1 rounded-full bg-slate-200" />
            <span className="text-[10px] font-medium text-slate-400">
              {task.assigneeName || 'Unassigned'}
            </span>
            {task.inActiveSprint && (
              <>
                <span className="w-1 h-1 rounded-full bg-slate-200" />
                <span className="text-[10px] font-bold text-amber-600">In Sprint</span>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <span className={`text-[9px] font-black px-2 py-0.5 rounded-lg border uppercase tracking-wider ${getPriorityStyle(task.priority)}`}>
          {task.priority}
        </span>

        <div className="w-7 h-7 rounded-full bg-slate-100 border-2 border-white flex items-center justify-center text-[9px] font-bold text-slate-600" title={task.assigneeName}>
          {task.assigneeName ? task.assigneeName.substring(0, 2).toUpperCase() : '--'}
        </div>

        <div className="relative">
          <button
            onClick={(e) => { e.stopPropagation(); setMenuOpen(!menuOpen); }}
            className="p-1 rounded-lg text-slate-300 hover:text-slate-600 hover:bg-slate-100 transition opacity-0 group-hover:opacity-100"
          >
            <MoreHorizontal size={14} />
          </button>

          {menuOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setMenuOpen(false)} />
              <div className="absolute right-0 top-8 z-20 bg-white border border-slate-200 rounded-xl shadow-lg py-1 w-44">
                <p className="px-3 py-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider">Move to stage</p>
                {stages.filter(s => s.id !== task.workStageId).map(stage => (
                  <button
                    key={stage.id}
                    onClick={() => { setMenuOpen(false); onMoveTask(task.id, stage.id); }}
                    className="w-full flex items-center gap-2 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 transition"
                  >
                    <ArrowRight size={14} /> {stage.name}
                  </button>
                ))}
              </div>
            </>
          )}
        </div>

        <ChevronRight size={16} className="text-slate-300 group-hover:text-indigo-400 transition-colors" />
      </div>
    </div>
  );
}
