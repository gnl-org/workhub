import React, { useEffect, useState, useRef } from 'react';
import { X, Save, Loader2, Paperclip, Trash2, Upload } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axios';

const TASK_TYPES = ['TASK', 'BUG', 'STORY', 'EPIC', 'SUB_TASK'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const STATUSES = ['OPEN', 'IN_PROGRESS', 'IN_REVIEW', 'BLOCKED', 'COMPLETED', 'CANCELLED'];

const typeColors = {
  TASK: 'bg-slate-100 text-slate-700',
  BUG: 'bg-red-100 text-red-700',
  STORY: 'bg-blue-100 text-blue-700',
  EPIC: 'bg-purple-100 text-purple-700',
  SUB_TASK: 'bg-amber-100 text-amber-700',
};

export default function TaskDetailModal({ projectId, taskId, isOpen, onClose, onUpdated, members }) {
  const { user } = useAuth();
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [files, setFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (!isOpen || !taskId) return;
    setLoading(true);
    Promise.all([
      api.get(`/api/v1/projects/${projectId}/tasks/${taskId}`),
      api.get(`/api/v1/projects/${projectId}/tasks/${taskId}/files`),
    ])
      .then(([taskRes, filesRes]) => {
        setTask(taskRes.data);
        setForm({
          title: taskRes.data.title,
          description: taskRes.data.description || '',
          status: taskRes.data.status,
          priority: taskRes.data.priority,
          taskType: taskRes.data.taskType,
          storyPoints: taskRes.data.storyPoints ?? '',
          assignedToId: taskRes.data.assignee?.id || '',
        });
        setFiles(filesRes.data || []);
      })
      .catch(() => setTask(null))
      .finally(() => setLoading(false));
  }, [projectId, taskId, isOpen]);

  const handleUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await api.post(`/api/v1/projects/${projectId}/tasks/${taskId}/files`, formData);
      setFiles(prev => [res.data, ...prev]);
    } catch {
      // ignore
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleDeleteFile = async (fileId) => {
    try {
      await api.delete(`/api/v1/files/${fileId}`);
      setFiles(prev => prev.filter(f => f.id !== fileId));
    } catch {
      // ignore
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const body = {
        title: form.title,
        description: form.description || null,
        status: form.status,
        priority: form.priority,
        taskType: form.taskType,
        storyPoints: form.storyPoints !== '' ? parseInt(form.storyPoints, 10) : null,
        assignedToId: form.assignedToId || null,
      };
      await api.patch(`/api/v1/projects/${projectId}/tasks/${taskId}`, body);
      const res = await api.get(`/api/v1/projects/${projectId}/tasks/${taskId}`);
      setTask(res.data);
      setForm({
        title: res.data.title,
        description: res.data.description || '',
        status: res.data.status,
        priority: res.data.priority,
        taskType: res.data.taskType,
        storyPoints: res.data.storyPoints ?? '',
        assignedToId: res.data.assignee?.id || '',
      });
      setEditing(false);
      onUpdated?.();
    } catch {
      // ignore
    } finally {
      setSaving(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="fixed inset-0 bg-black/40" onClick={onClose} />
      <div className="relative bg-white rounded-2xl shadow-xl z-10 w-full max-w-lg mx-4 max-h-[85vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b border-slate-100 px-6 py-4 flex items-center justify-between rounded-t-2xl z-10">
          <h3 className="text-lg font-bold text-slate-900">
            {task ? (editing ? 'Edit Task' : 'Task Details') : 'Task'}
          </h3>
          <div className="flex items-center gap-2">
            {task && !editing && (
              <button
                onClick={() => setEditing(true)}
                className="px-3 py-1.5 text-xs font-bold text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
              >
                Edit
              </button>
            )}
            <button onClick={onClose} className="p-1 rounded-lg hover:bg-slate-100 transition">
              <X size={18} className="text-slate-400" />
            </button>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-16">
            <Loader2 size={24} className="animate-spin text-slate-400" />
          </div>
        ) : !task ? (
          <div className="p-6 text-center text-sm text-slate-500 font-medium">Task not found</div>
        ) : editing ? (
          <div className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-bold text-slate-700 mb-1">Title</label>
              <input
                type="text"
                value={form.title}
                onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
                className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-bold text-slate-700 mb-1">Description</label>
              <textarea
                value={form.description}
                onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                rows={3}
                className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">Status</label>
                <select
                  value={form.status}
                  onChange={e => setForm(f => ({ ...f, status: e.target.value }))}
                  className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  {STATUSES.map(s => (
                    <option key={s} value={s}>{s.replace('_', ' ')}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">Type</label>
                <select
                  value={form.taskType}
                  onChange={e => setForm(f => ({ ...f, taskType: e.target.value }))}
                  className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  {TASK_TYPES.map(t => (
                    <option key={t} value={t}>{t.replace('_', ' ')}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">Priority</label>
                <select
                  value={form.priority}
                  onChange={e => setForm(f => ({ ...f, priority: e.target.value }))}
                  className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  {PRIORITIES.map(p => (
                    <option key={p} value={p}>{p}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">Story Points</label>
                <input
                  type="number"
                  min="0"
                  value={form.storyPoints}
                  onChange={e => setForm(f => ({ ...f, storyPoints: e.target.value }))}
                  className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-bold text-slate-700 mb-1">Assignee</label>
              <select
                value={form.assignedToId}
                onChange={e => setForm(f => ({ ...f, assignedToId: e.target.value }))}
                className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="">Unassigned</option>
              {user?.id && (
                <option value={user.id}>{user.fullName || user.email} (me)</option>
              )}
              {(members || []).map(m => (
                <option key={m.userId} value={m.userId}>{m.userName || m.userEmail}</option>
              ))}
              </select>
            </div>

            {task.projectName && (
              <div className="text-xs text-slate-400 font-medium">
                Project: {task.projectName}
              </div>
            )}

            <div className="flex justify-end gap-3 pt-2 border-t border-slate-100">
              <button
                onClick={() => { setEditing(false); setForm({
                  title: task.title,
                  description: task.description || '',
                  status: task.status,
                  priority: task.priority,
                  taskType: task.taskType,
                  storyPoints: task.storyPoints ?? '',
                  assignedToId: task.assignee?.id || '',
                }); }}
                onMouseDown={(e) => e.preventDefault()}
                className="px-4 py-2 text-sm font-bold text-slate-600 hover:bg-slate-100 rounded-xl transition"
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                disabled={saving}
                className="flex items-center gap-2 px-4 py-2 text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl transition disabled:opacity-50"
              >
                {saving ? <Loader2 size={14} className="animate-spin" /> : <Save size={14} />}
                Save
              </button>
            </div>
          </div>
        ) : (
          <div className="p-6 space-y-5">
            <div>
              <h4 className="text-xl font-bold text-slate-900 mb-1">{task.title}</h4>
              {task.description && (
                <p className="text-sm text-slate-600 whitespace-pre-wrap">{task.description}</p>
              )}
            </div>

            <div className="flex flex-wrap gap-2">
              <span className={`text-[10px] font-bold px-2.5 py-1 rounded-lg ${typeColors[task.taskType] || 'bg-slate-100 text-slate-700'}`}>
                {task.taskType?.replace('_', ' ') || 'TASK'}
              </span>
              <span className="text-[10px] font-bold px-2.5 py-1 rounded-lg bg-slate-100 text-slate-700">
                {task.status?.replace('_', ' ')}
              </span>
              <span className="text-[10px] font-bold px-2.5 py-1 rounded-lg bg-slate-100 text-slate-700">
                {task.priority}
              </span>
              {task.storyPoints != null && (
                <span className="text-[10px] font-bold px-2.5 py-1 rounded-lg bg-amber-100 text-amber-700">
                  {task.storyPoints} pts
                </span>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-xs font-bold text-slate-400 uppercase">Assignee</span>
                <p className="font-medium text-slate-700">{task.assignee?.fullName || task.assignee?.email || 'Unassigned'}</p>
              </div>
              <div>
                <span className="text-xs font-bold text-slate-400 uppercase">Creator</span>
                <p className="font-medium text-slate-700">{task.creator?.fullName || task.creator?.email}</p>
              </div>
              {task.dueDate && (
                <div>
                  <span className="text-xs font-bold text-slate-400 uppercase">Due Date</span>
                  <p className="font-medium text-slate-700">{new Date(task.dueDate).toLocaleDateString()}</p>
                </div>
              )}
              <div>
                <span className="text-xs font-bold text-slate-400 uppercase">Project</span>
                <p className="font-medium text-slate-700">{task.projectName}</p>
              </div>
            </div>

            {task.comments && task.comments.length > 0 && (
              <div>
                <h5 className="text-xs font-bold text-slate-400 uppercase mb-2">Comments ({task.comments.length})</h5>
                <div className="space-y-2 max-h-40 overflow-y-auto">
                  {task.comments.map(c => (
                    <div key={c.id} className="bg-slate-50 rounded-xl p-3">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-xs font-bold text-slate-700">{c.authorName}</span>
                        <span className="text-[10px] text-slate-400">{new Date(c.createdAt).toLocaleString()}</span>
                      </div>
                      <p className="text-sm text-slate-600">{c.content}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div>
              <div className="flex items-center justify-between mb-2">
                <h5 className="text-xs font-bold text-slate-400 uppercase">
                  Files ({files.length})
                </h5>
                <div className="flex items-center gap-1">
                  <input
                    ref={fileInputRef}
                    type="file"
                    onChange={handleUpload}
                    className="hidden"
                  />
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    disabled={uploading}
                    className="flex items-center gap-1 px-2.5 py-1 text-[10px] font-bold text-indigo-600 hover:bg-indigo-50 rounded-lg transition disabled:opacity-50"
                  >
                    {uploading ? <Loader2 size={12} className="animate-spin" /> : <Upload size={12} />}
                    {uploading ? 'Uploading...' : 'Upload'}
                  </button>
                </div>
              </div>
              {files.length === 0 ? (
                <p className="text-xs text-slate-400">No files</p>
              ) : (
                <div className="space-y-1.5">
                  {files.map(f => (
                    <div key={f.id} className="flex items-center justify-between bg-slate-50 rounded-xl px-3 py-2">
                      <a
                        href={f.url}
                        download={f.fileName}
                        className={`flex items-center gap-2 text-sm text-indigo-600 hover:text-indigo-800 hover:underline min-w-0 ${f.mimeType?.startsWith('image/') ? 'w-full' : ''}`}
                      >
                        {f.mimeType?.startsWith('image/') ? (
                          <img
                            src={f.url}
                            alt={f.fileName}
                            className="w-10 h-10 rounded-lg object-cover shrink-0"
                          />
                        ) : (
                          <Paperclip size={14} className="shrink-0" />
                        )}
                        <span className="truncate">{f.fileName}</span>
                        <span className="text-[10px] text-slate-400 shrink-0">({formatFileSize(f.fileSize)})</span>
                      </a>
                      <button
                        onClick={() => handleDeleteFile(f.id)}
                        className="p-1 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-500 transition shrink-0 ml-2"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
