import React, { useEffect } from 'react';
import { useTasks } from '../../hooks/useTasks';
import { Plus, CheckCircle2, AlertCircle, Clock, ChevronRight } from 'lucide-react';

export default function BacklogTab({ projectId }) {
  const { tasks, isLoading, error, fetchTasks } = useTasks(projectId);

  useEffect(() => {
    fetchTasks({}, 0, 50);
  }, [fetchTasks]);

  const getPriorityStyle = (priority) => {
    switch (priority) {
      case 'CRITICAL': return 'bg-red-100 text-red-700 border-red-200';
      case 'HIGH': return 'bg-orange-100 text-orange-700 border-orange-200';
      case 'MEDIUM': return 'bg-blue-100 text-blue-700 border-blue-200';
      default: return 'bg-slate-100 text-slate-600 border-slate-200';
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-5xl mx-auto py-12 px-4 space-y-4">
        {[1, 2, 3].map(i => (
          <div key={i} className="h-20 bg-slate-100 rounded-2xl animate-pulse" />
        ))}
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto py-8 px-4 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-xl font-bold text-slate-900">Backlog</h3>
          <p className="text-sm text-slate-500 font-medium">Manage project tasks and requirements</p>
        </div>
        <button className="flex items-center gap-2 bg-indigo-600 text-white px-5 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100">
          <Plus size={18} /> New Task
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-100 rounded-2xl flex items-center gap-3 text-red-600 text-sm font-medium">
          <AlertCircle size={18} /> {error}
        </div>
      )}

      <div className="bg-white rounded-[2rem] border border-slate-100 shadow-sm overflow-hidden">
        {tasks.length === 0 ? (
          <div className="py-24 text-center">
            <div className="w-16 h-16 bg-slate-50 text-slate-300 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <Clock size={32} />
            </div>
            <p className="text-slate-500 font-bold">Your backlog is empty</p>
            <p className="text-slate-400 text-sm mt-1">Create a task to get the engine running.</p>
          </div>
        ) : (
          <div className="divide-y divide-slate-50">
            {tasks.map((task) => (
              <div key={task.id} className="p-4 hover:bg-slate-50/50 transition-all flex items-center justify-between group cursor-pointer">
                <div className="flex items-center gap-4 flex-1">
                  <CheckCircle2 className={task.status === 'COMPLETED' ? 'text-emerald-500' : 'text-slate-200'} size={20} />
                  
                  <div className="flex flex-col">
                    <h4 className="text-sm font-bold text-slate-700 group-hover:text-indigo-600 transition-colors">
                      {task.title}
                    </h4>
                    <div className="flex items-center gap-3 mt-1">
                      <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tighter">
                        {task.status.replace('_', ' ')}
                      </span>
                      <span className="w-1 h-1 rounded-full bg-slate-200" />
                      <span className="text-[10px] font-medium text-slate-400">
                        Updated {new Date(task.updatedAt).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-4">
                  <span className={`text-[9px] font-black px-2.5 py-1 rounded-lg border uppercase tracking-wider ${getPriorityStyle(task.priority)}`}>
                    {task.priority}
                  </span>

                  <div className="w-8 h-8 rounded-full bg-slate-100 border-2 border-white flex items-center justify-center text-[10px] font-bold text-slate-600 group-hover:border-indigo-100 transition-all" title={task.assigneeName}>
                    {task.assigneeName ? task.assigneeName.substring(0, 2).toUpperCase() : '--'}
                  </div>
                  
                  <ChevronRight size={16} className="text-slate-300 group-hover:text-indigo-400 transition-colors" />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}