import React, { useEffect, useState } from 'react';
import { Clock, ChevronDown, ChevronRight, CheckCircle2, XCircle } from 'lucide-react';
import api from '../../api/axios';

export default function SprintHistoryTab({ projectId }) {
  const [history, setHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);
  const [detailTasks, setDetailTasks] = useState([]);
  const [loadingTasks, setLoadingTasks] = useState(false);

  useEffect(() => {
    if (!projectId) return;
    setIsLoading(true);
    api.get(`/projects/${projectId}/sprints/history`)
      .then(res => setHistory(res.data))
      .catch(() => {})
      .finally(() => setIsLoading(false));
  }, [projectId]);

  const handleExpand = async (sprintId) => {
    if (expandedId === sprintId) {
      setExpandedId(null);
      setDetailTasks([]);
      return;
    }
    setExpandedId(sprintId);
    setLoadingTasks(true);
    try {
      const res = await api.get(`/projects/${projectId}/sprints/${sprintId}`);
      setDetailTasks(res.data.tasks || []);
    } catch {
      setDetailTasks([]);
    } finally {
      setLoadingTasks(false);
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-5xl mx-auto py-8 px-4">
        <div className="flex items-center justify-center py-24">
          <p className="text-slate-400 font-bold text-sm">Loading history...</p>
        </div>
      </div>
    );
  }

  if (history.length === 0) {
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

  return (
    <div className="max-w-5xl mx-auto py-8 px-4 space-y-3">
      <h3 className="font-bold text-slate-800 mb-6">Sprint History</h3>
      {history.map(sprint => (
        <div key={sprint.id} className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
          <button
            onClick={() => handleExpand(sprint.id)}
            className="w-full flex items-center justify-between p-5 hover:bg-slate-50/50 transition text-left"
          >
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 bg-slate-100 rounded-xl flex items-center justify-center text-slate-500">
                {expandedId === sprint.id ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
              </div>
              <div>
                <p className="font-bold text-slate-800">{sprint.name}</p>
                {sprint.goal && <p className="text-xs text-slate-500 mt-0.5">{sprint.goal}</p>}
              </div>
            </div>
            <div className="flex items-center gap-6">
              {sprint.startDate && (
                <span className="text-xs text-slate-400 font-medium">
                  {new Date(sprint.startDate).toLocaleDateString()} – {sprint.endDate ? new Date(sprint.endDate).toLocaleDateString() : '?'}
                </span>
              )}
              <div className="flex items-center gap-3 text-xs font-bold">
                <span className="text-green-600">{sprint.completedTasks} done</span>
                <span className="text-slate-300">·</span>
                <span className="text-amber-600">{sprint.incompleteTasks} incomplete</span>
              </div>
            </div>
          </button>

          {expandedId === sprint.id && (
            <div className="border-t border-slate-100">
              {loadingTasks ? (
                <div className="p-6 text-center text-sm text-slate-400">Loading tasks...</div>
              ) : detailTasks.length === 0 ? (
                <div className="p-6 text-center text-sm text-slate-400">No tasks in this sprint</div>
              ) : (
                <div className="divide-y divide-slate-50">
                  {detailTasks.map(task => (
                    <div key={task.id} className="flex items-center gap-4 px-5 py-3">
                      {task.status === 'COMPLETED' || task.status === 'CANCELLED' ? (
                        <CheckCircle2 size={16} className="text-green-500 flex-shrink-0" />
                      ) : (
                        <XCircle size={16} className="text-slate-300 flex-shrink-0" />
                      )}
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-semibold text-slate-700 truncate">{task.title}</p>
                      </div>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider bg-slate-100 text-slate-600 flex-shrink-0">
                        {task.status.replace('_', ' ')}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
