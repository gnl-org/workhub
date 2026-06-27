import React from 'react';
import { Play, CheckCircle } from 'lucide-react';

export default function SprintToolbar({ sprint, onStart, onComplete, onManage }) {
  if (!sprint) return null;

  const isActive = sprint.status === 'ACTIVE';
  const isPlanned = sprint.status === 'PLANNED';

  return (
    <div className="flex justify-between items-center mb-6">
      <div className="flex items-center gap-4">
        <h3 className="font-bold text-slate-800">{sprint.name}</h3>
        <span className="text-xs px-2 py-0.5 rounded-full font-bold uppercase tracking-wider bg-indigo-100 text-indigo-700">
          {sprint.status.replace('_', ' ')}
        </span>
        {sprint.endDate && (
          <span className="text-xs text-slate-400 font-medium italic">
            Ends {new Date(sprint.endDate).toLocaleDateString()}
          </span>
        )}
      </div>
      <div className="flex items-center gap-2">
        {isPlanned && (
          <button
            onClick={onStart}
            className="flex items-center gap-1.5 bg-indigo-600 text-white px-4 py-2 rounded-lg text-xs font-bold hover:bg-indigo-700 transition"
          >
            <Play size={14} /> Start Sprint
          </button>
        )}
        {isActive && (
          <button
            onClick={onComplete}
            className="flex items-center gap-1.5 bg-white border border-slate-200 px-4 py-2 rounded-lg text-xs font-bold text-slate-700 hover:bg-slate-50 transition"
          >
            <CheckCircle size={14} /> Complete Sprint
          </button>
        )}
        <button
          onClick={onManage}
          className="flex items-center gap-1.5 bg-white border border-slate-200 px-4 py-2 rounded-lg text-xs font-bold text-slate-700 hover:bg-slate-50 transition"
        >
          Manage Sprints
        </button>
      </div>
    </div>
  );
}
