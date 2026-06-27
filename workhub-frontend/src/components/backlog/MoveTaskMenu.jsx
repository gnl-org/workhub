import React from 'react';
import { ArrowRight } from 'lucide-react';

export default function MoveTaskMenu({ taskId, currentStageId, stages, onMove, onClose }) {
  const availableStages = stages.filter(s => s.id !== currentStageId);

  return (
    <>
      <div className="fixed inset-0 z-10" onClick={onClose} />
      <div className="absolute right-0 top-8 z-20 bg-white border border-slate-200 rounded-xl shadow-lg py-1 w-44">
        <p className="px-3 py-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider">Move to stage</p>
        {availableStages.map(stage => (
          <button
            key={stage.id}
            onClick={() => { onClose(); onMove(taskId, stage.id); }}
            className="w-full flex items-center gap-2 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 transition"
          >
            <ArrowRight size={14} /> {stage.name}
          </button>
        ))}
        {availableStages.length === 0 && (
          <p className="px-3 py-2 text-xs text-slate-400 italic">No other stages</p>
        )}
      </div>
    </>
  );
}
