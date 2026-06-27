import React from 'react';
import { X, AlertTriangle } from 'lucide-react';

export default function CloseSprintModal({ sprint, incompleteCount, onConfirm, onCancel, isClosing }) {
  if (!sprint) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/20" onClick={onCancel} />
      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 mx-4">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-amber-100 rounded-xl flex items-center justify-center">
              <AlertTriangle size={20} className="text-amber-600" />
            </div>
            <h3 className="font-bold text-slate-900">Complete Sprint</h3>
          </div>
          <button onClick={onCancel} className="p-1 hover:bg-slate-100 rounded-lg text-slate-400">
            <X size={18} />
          </button>
        </div>

        <p className="text-sm text-slate-600 mb-1">
          You are about to close <strong className="text-slate-800">{sprint.name}</strong>.
        </p>

        <div className="bg-amber-50 rounded-xl p-4 my-4">
          <p className="text-sm text-amber-800 font-bold">
            {incompleteCount} incomplete task{incompleteCount !== 1 ? 's' : ''}
          </p>
          <p className="text-xs text-amber-700 mt-1">
            Incomplete tasks will be moved to the next planned sprint. If none exists, a new one will be created automatically.
          </p>
        </div>

        <div className="flex gap-3">
          <button
            onClick={onCancel}
            className="flex-1 px-4 py-2.5 rounded-xl text-sm font-bold text-slate-600 border border-slate-200 hover:bg-slate-50 transition"
            disabled={isClosing}
          >
            Cancel
          </button>
          <button
            onClick={onConfirm}
            disabled={isClosing}
            className="flex-1 px-4 py-2.5 rounded-xl text-sm font-bold text-white bg-amber-600 hover:bg-amber-700 disabled:opacity-50 transition"
          >
            {isClosing ? 'Closing...' : 'Complete Sprint'}
          </button>
        </div>
      </div>
    </div>
  );
}
