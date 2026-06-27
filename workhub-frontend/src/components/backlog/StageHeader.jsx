import React, { useState } from 'react';
import { ChevronDown, ChevronRight, GripVertical, MoreHorizontal, Pencil, Trash2 } from 'lucide-react';
import { useDraggable } from '@dnd-kit/core';

export default function StageHeader({ stage, taskCount, onRename, onDelete, isCollapsed, onToggleCollapse }) {
  const [menuOpen, setMenuOpen] = useState(false);

  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: `stage:${stage.id}`,
    data: { type: 'stage-drag', stageId: stage.id },
  });

  const isDefault = ['Backlog', 'Ready for Refinement', 'Ready for Sprint'].includes(stage.name);

  return (
    <div className={`flex items-center justify-between px-4 py-3 bg-slate-50 border-b border-slate-100 rounded-t-xl group ${isDragging ? 'opacity-50' : ''}`}>
      <div className="flex items-center gap-1 flex-1">
        <button ref={setNodeRef} {...listeners} {...attributes} className="p-1 rounded text-slate-400 hover:text-slate-600 hover:bg-slate-200 transition cursor-grab active:cursor-grabbing touch-none">
          <GripVertical size={16} />
        </button>
        <button onClick={onToggleCollapse} className="text-slate-400 hover:text-slate-600 transition">
          {isCollapsed ? <ChevronRight size={18} /> : <ChevronDown size={18} />}
        </button>
        <h4 className="text-sm font-bold text-slate-700">{stage.name}</h4>
        <span className="text-[10px] font-bold text-slate-400 bg-slate-200 px-2 py-0.5 rounded-full">
          {taskCount}
        </span>
        {stage.sprintStatus === 'ACTIVE' && (
          <span className="text-[10px] font-bold text-amber-600 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-full">
            In Active Sprint
          </span>
        )}
      </div>

      <div className="relative">
        <button
          onClick={() => setMenuOpen(!menuOpen)}
          className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-200 transition opacity-0 group-hover:opacity-100"
        >
          <MoreHorizontal size={16} />
        </button>

        {menuOpen && (
          <>
            <div className="fixed inset-0 z-10" onClick={() => setMenuOpen(false)} />
            <div className="absolute right-0 top-8 z-20 bg-white border border-slate-200 rounded-xl shadow-lg py-1 w-40">
              {!isDefault && (
                <>
                  <button
                    onClick={() => { setMenuOpen(false); onRename(); }}
                    className="w-full flex items-center gap-2 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 transition"
                  >
                    <Pencil size={14} /> Rename
                  </button>
                  <button
                    onClick={() => { setMenuOpen(false); onDelete(); }}
                    className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50 transition"
                  >
                    <Trash2 size={14} /> Delete
                  </button>
                </>
              )}
              {isDefault && (
                <p className="px-3 py-2 text-xs text-slate-400 italic">Default stage</p>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
