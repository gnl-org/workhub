import React from 'react';
import { Clock } from 'lucide-react';

export default function SprintHistoryTab({ projectId }) {
  return (
    <div className="max-w-5xl mx-auto py-8 px-4">
      <div className="bg-white rounded-[2rem] border border-slate-100 shadow-sm py-24 text-center">
        <div className="w-16 h-16 bg-slate-50 text-slate-300 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <Clock size={32} />
        </div>
        <p className="text-slate-500 font-bold">No sprint history yet</p>
        <p className="text-slate-400 text-sm mt-1">Closed sprints will appear here.</p>
      </div>
    </div>
  );
}
