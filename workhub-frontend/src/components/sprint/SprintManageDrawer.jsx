import React, { useState, useEffect } from 'react';
import { X, Play, Plus, ChevronRight } from 'lucide-react';
import api from '../../api/axios';

export default function SprintManageDrawer({ projectId, sprints, onClose, onStart, onCreated }) {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [name, setName] = useState('');
  const [goal, setGoal] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [error, setError] = useState(null);

  const plannedSprints = (sprints || []).filter(s => s.status === 'PLANNED');
  const activeSprint = (sprints || []).find(s => s.status === 'ACTIVE');
  const hasActiveSprint = !!activeSprint;

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const data = {};
      if (name.trim()) data.name = name.trim();
      if (goal.trim()) data.goal = goal.trim();
      if (startDate) data.startDate = startDate;
      if (endDate) data.endDate = endDate;
      const res = await api.post(`/api/v1/projects/${projectId}/sprints`, data);
      onCreated?.(res.data);
      setShowCreateForm(false);
      setName('');
      setGoal('');
      setStartDate('');
      setEndDate('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create sprint');
    }
  };

  const handleStart = async (sprintId) => {
    setError(null);
    if (hasActiveSprint) {
      setError(`Cannot start: "${activeSprint.name}" is already active. Close it first.`);
      return;
    }
    const result = await onStart(sprintId);
    if (result.success) onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <div className="absolute inset-0 bg-black/20" onClick={onClose} />
      <div className="relative w-full max-w-lg bg-white shadow-2xl flex flex-col">
        <div className="flex items-center justify-between p-6 border-b border-slate-200">
          <h3 className="font-bold text-slate-900">Manage Sprints</h3>
          <button onClick={onClose} className="p-2 hover:bg-slate-100 rounded-lg text-slate-400">
            <X size={18} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6 space-y-4">
          {error && (
            <div className="bg-red-50 text-red-700 text-xs font-bold px-4 py-3 rounded-xl">
              {error}
            </div>
          )}

          {!showCreateForm ? (
            <button
              onClick={() => setShowCreateForm(true)}
              className="flex items-center gap-2 w-full bg-indigo-600 text-white px-4 py-3 rounded-xl text-sm font-bold hover:bg-indigo-700 transition"
            >
              <Plus size={16} /> New Sprint
            </button>
          ) : (
            <form onSubmit={handleCreate} className="bg-slate-50 rounded-2xl p-4 space-y-3">
              <div>
                <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Name (optional)</label>
                <input
                  type="text"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  placeholder="Sprint N (auto)"
                  className="w-full mt-1 px-3 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                />
              </div>
              <div>
                <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Goal (optional)</label>
                <input
                  type="text"
                  value={goal}
                  onChange={e => setGoal(e.target.value)}
                  placeholder="What's the focus?"
                  className="w-full mt-1 px-3 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                />
              </div>
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Start</label>
                  <input
                    type="date"
                    value={startDate}
                    onChange={e => setStartDate(e.target.value)}
                    className="w-full mt-1 px-3 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  />
                </div>
                <div className="flex-1">
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">End</label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={e => setEndDate(e.target.value)}
                    className="w-full mt-1 px-3 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  />
                </div>
              </div>
              <div className="flex gap-2 pt-1">
                <button
                  type="submit"
                  className="flex-1 bg-indigo-600 text-white rounded-xl py-2 text-xs font-bold hover:bg-indigo-700 transition"
                >
                  Create Sprint
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreateForm(false)}
                  className="px-4 py-2 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-200 transition"
                >
                  Cancel
                </button>
              </div>
            </form>
          )}

          {hasActiveSprint && (
            <div className="bg-green-50 rounded-2xl p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-bold text-green-800">{activeSprint.name}</p>
                  <p className="text-xs text-green-600 font-medium">{activeSprint.totalTasks} tasks</p>
                </div>
                <span className="text-[10px] px-2 py-0.5 rounded-full font-bold bg-green-200 text-green-800">
                  ACTIVE
                </span>
              </div>
            </div>
          )}

          {plannedSprints.length > 0 && (
            <div>
              <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-3">Planned Sprints</h4>
              <div className="space-y-2">
                {plannedSprints.map(s => (
                  <div key={s.id} className="flex items-center justify-between bg-white border border-slate-200 rounded-xl p-4">
                    <div>
                      <p className="text-sm font-bold text-slate-800">{s.name}</p>
                      {s.goal && <p className="text-xs text-slate-500 mt-0.5">{s.goal}</p>}
                    </div>
                    <button
                      onClick={() => handleStart(s.id)}
                      className="flex items-center gap-1.5 text-indigo-600 hover:text-indigo-800 text-xs font-bold transition"
                    >
                      <Play size={14} /> Start
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {plannedSprints.length === 0 && !showCreateForm && (
            <p className="text-center text-slate-400 text-sm py-8">No planned sprints. Create one to get started.</p>
          )}
        </div>
      </div>
    </div>
  );
}
