import React, { useState, useEffect } from 'react';
import { LayoutList, Users, MessageSquare, History, Plus, MoreVertical, AlertCircle } from 'lucide-react';
import api from '../api/axios';

const Backlog = ({ projectId }) => {
  const [tasks, setTasks] = useState([]);
  const [project, setProject] = useState(null);
  const [selectedTask, setSelectedTask] = useState(null);
  const [activities, setActivities] = useState([]);

  // Fetch Backlog Data
  useEffect(() => {
    const fetchData = async () => {
      const projRes = await api.get(`/projects/${projectId}`);
      const taskRes = await api.get(`/projects/${projectId}/tasks?page=0&size=50`);
      setProject(projRes.data);
      setTasks(taskRes.data.content);
    };
    fetchData();
  }, [projectId]);

  // Fetch Task Details + Activity Log (Phase II Feature)
  const handleTaskClick = async (taskId) => {
    const detailRes = await api.get(`/projects/${projectId}/tasks/${taskId}`);
    const activityRes = await api.get(`/api/v1/projects/${projectId}/tasks/${taskId}/activities`);
    setSelectedTask(detailRes.data);
    setActivities(activityRes.data);
  };

  return (
    <div className="flex h-screen bg-slate-50">
      {/* Main Content Area */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="bg-white border-b p-6">
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-2xl font-bold text-slate-900">{project?.title}</h1>
              <p className="text-slate-500 mt-1">{project?.description}</p>
            </div>
            <button className="bg-indigo-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-indigo-700 transition">
              <Plus size={18} /> New Task
            </button>
          </div>

          {/* Tabs */}
          <div className="flex gap-8 mt-8 border-b">
            <button className="pb-3 border-b-2 border-indigo-600 text-indigo-600 font-medium">Backlog</button>
            <button className="pb-3 text-slate-400 font-medium hover:text-slate-600">Active Board</button>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-6">
          <div className="bg-white rounded-xl border shadow-sm">
            {tasks.map((task) => (
              <div 
                key={task.id}
                onClick={() => handleTaskClick(task.id)}
                className="flex items-center justify-between p-4 border-b last:border-0 hover:bg-slate-50 cursor-pointer transition"
              >
                <div className="flex items-center gap-4">
                  <StatusIcon status={task.status} />
                  <span className="text-sm font-mono text-slate-400 uppercase tracking-tighter">WH-{task.id.slice(0,4)}</span>
                  <span className="font-medium text-slate-700">{task.title}</span>
                </div>
                <div className="flex items-center gap-4">
                  <PriorityBadge priority={task.priority} />
                  <div className="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center text-xs font-bold text-slate-600">
                    {task.assigneeName?.charAt(0) || '?'}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </main>
      </div>

      {/* Task Detail Drawer (Slides out from right) */}
      {selectedTask && (
        <aside className="w-96 bg-white border-l shadow-2xl flex flex-col">
          <div className="p-6 border-b flex justify-between items-center">
            <h2 className="font-bold text-lg uppercase tracking-tight">Task Details</h2>
            <button onClick={() => setSelectedTask(null)} className="text-slate-400 hover:text-slate-600">✕</button>
          </div>
          
          <div className="flex-1 overflow-y-auto p-6 space-y-8">
            <section>
              <label className="text-xs font-bold text-slate-400 uppercase">Title</label>
              <h3 className="text-xl font-semibold mt-1 text-slate-800">{selectedTask.title}</h3>
            </section>

            <section className="space-y-4">
              <div className="flex justify-between">
                <span className="text-slate-500 text-sm">Status</span>
                <StatusIcon status={selectedTask.status} label />
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500 text-sm">Priority</span>
                <PriorityBadge priority={selectedTask.priority} />
              </div>
            </section>

            {/* Activity Log (Phase II requirement) */}
            <section>
              <div className="flex items-center gap-2 mb-4 border-b pb-2">
                <History size={16} className="text-slate-400" />
                <h4 className="font-bold text-sm text-slate-700">Activity Log</h4>
              </div>
              <div className="space-y-4">
                {activities.map((log) => (
                  <div key={log.id} className="text-xs flex gap-3">
                    <div className="w-2 h-2 rounded-full bg-slate-300 mt-1" />
                    <div>
                      <p className="text-slate-600"><span className="font-bold text-slate-900">{log.performedBy}</span> {log.action}</p>
                      <p className="text-slate-400 mt-1">{new Date(log.timestamp).toLocaleString()}</p>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </div>
        </aside>
      )}
    </div>
  );
};

// Helper Components
const StatusIcon = ({ status, label }) => {
  const colors = {
    OPEN: 'text-slate-400',
    IN_PROGRESS: 'text-blue-500',
    COMPLETED: 'text-green-500',
    BLOCKED: 'text-red-500'
  };
  return (
    <div className="flex items-center gap-2">
      <div className={`w-3 h-3 rounded-full border-2 border-current ${colors[status]}`} />
      {label && <span className="text-sm font-medium">{status}</span>}
    </div>
  );
};

const PriorityBadge = ({ priority }) => {
  const styles = {
    HIGH: 'bg-red-50 text-red-700 border-red-100',
    MEDIUM: 'bg-amber-50 text-amber-700 border-amber-100',
    LOW: 'bg-slate-50 text-slate-700 border-slate-100',
    CRITICAL: 'bg-purple-50 text-purple-700 border-purple-100'
  };
  return (
    <span className={`px-2 py-1 rounded text-[10px] font-bold border uppercase ${styles[priority]}`}>
      {priority}
    </span>
  );
};

export default Backlog;