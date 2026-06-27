import React from 'react';
import { Boxes, Plus } from 'lucide-react';

export default function SprintEmptyState({ onCreateSprint }) {
  return (
    <div className="h-full flex items-center justify-center">
      <div className="text-center max-w-sm">
        <div className="w-16 h-16 bg-slate-50 text-slate-300 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <Boxes size={32} />
        </div>
        <p className="text-slate-500 font-bold mb-2">No active sprint</p>
        <p className="text-slate-400 text-sm mb-6">
          Create a sprint to start organizing your work into iterations.
        </p>
        <button
          onClick={onCreateSprint}
          className="flex items-center gap-2 mx-auto bg-indigo-600 text-white px-5 py-2.5 rounded-xl text-sm font-bold hover:bg-indigo-700 transition"
        >
          <Plus size={16} /> Create Sprint
        </button>
      </div>
    </div>
  );
}
