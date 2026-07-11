import React, { useState, useRef } from 'react';
import { Upload, Loader2, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axios';
import Modal from '../Modal';

const TASK_TYPES = ['TASK', 'BUG', 'STORY', 'EPIC', 'SUB_TASK'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function CreateTaskModal({ isOpen, onClose, onCreate, stages, members, projectId }) {
  const { user } = useAuth();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [taskType, setTaskType] = useState('TASK');
  const [priority, setPriority] = useState('MEDIUM');
  const [storyPoints, setStoryPoints] = useState('');
  const [assignedToId, setAssignedToId] = useState(user?.id || '');
  const [stageId, setStageId] = useState(stages?.[0]?.id || '');
  const [files, setFiles] = useState([]);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const fileInputRef = useRef(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;
    setSubmitting(true);
    setError(null);
    const result = await onCreate({
      title: title.trim(),
      description: description.trim() || undefined,
      taskType,
      priority,
      storyPoints: storyPoints ? parseInt(storyPoints, 10) : undefined,
      assignedToId: assignedToId || undefined,
      workStageId: stageId || undefined,
    });
    if (result.success) {
      const taskId = result.data.id;
      if (files.length > 0 && taskId && projectId) {
        for (const file of files) {
          try {
            const formData = new FormData();
            formData.append('file', file);
            await api.post(`/api/v1/projects/${projectId}/tasks/${taskId}/files`, formData);
          } catch {
            // ignore individual file failures
          }
        }
      }
      setTitle('');
      setDescription('');
      setTaskType('TASK');
      setPriority('MEDIUM');
      setStoryPoints('');
      setAssignedToId('');
      setFiles([]);
      onClose();
    } else {
      setError(result.error);
    }
    setSubmitting(false);
  };

  const handleFileSelect = (e) => {
    const selected = Array.from(e.target.files || []);
    setFiles(prev => [...prev, ...selected]);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const removeFile = (index) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <form onSubmit={handleSubmit} className="p-6 space-y-4">
        <h3 className="text-lg font-bold text-slate-900">Create New Task</h3>

        {error && (
          <div className="p-3 bg-red-50 border border-red-100 rounded-xl text-sm text-red-600 font-medium">
            {error}
          </div>
        )}

        <div>
          <label className="block text-sm font-bold text-slate-700 mb-1">Title</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Task title"
            className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            autoFocus
          />
        </div>

        <div>
          <label className="block text-sm font-bold text-slate-700 mb-1">Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Optional description"
            rows={3}
            className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-bold text-slate-700 mb-1">Type</label>
            <select
              value={taskType}
              onChange={(e) => setTaskType(e.target.value)}
              className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white"
            >
              {TASK_TYPES.map(t => (
                <option key={t} value={t}>{t.replace('_', ' ')}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-bold text-slate-700 mb-1">Priority</label>
            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
              className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white"
            >
              {PRIORITIES.map(p => (
                <option key={p} value={p}>{p}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-bold text-slate-700 mb-1">Story Points</label>
            <input
              type="number"
              min="0"
              value={storyPoints}
              onChange={(e) => setStoryPoints(e.target.value)}
              placeholder="—"
              className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            />
          </div>
          <div>
            <label className="block text-sm font-bold text-slate-700 mb-1">Assignee</label>
            <select
              value={assignedToId}
              onChange={(e) => setAssignedToId(e.target.value)}
              className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white"
            >
              <option value="">Unassigned</option>
              {user?.id && (
                <option value={user.id}>{user.fullName || user.email} (me)</option>
              )}
              {(members || []).filter(m => m.userId !== user?.id).map(m => (
                <option key={m.userId} value={m.userId}>{m.userName || m.userEmail}</option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-bold text-slate-700 mb-1">Work Stage</label>
          <select
            value={stageId}
            onChange={(e) => setStageId(e.target.value)}
            className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white"
          >
            {(stages || []).map(stage => (
              <option key={stage.id} value={stage.id}>{stage.name}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-bold text-slate-700 mb-1">Files</label>
          <input
            ref={fileInputRef}
            type="file"
            multiple
            onChange={handleFileSelect}
            className="hidden"
          />
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center gap-2 px-4 py-2 text-sm font-bold text-slate-600 hover:bg-slate-100 rounded-xl transition w-full border border-dashed border-slate-200"
          >
            <Upload size={14} />
            {files.length > 0 ? `${files.length} file(s) selected` : 'Add files'}
          </button>
          {files.length > 0 && (
            <div className="mt-2 space-y-1">
              {files.map((f, i) => (
                <div key={i} className="flex items-center justify-between bg-slate-50 rounded-lg px-3 py-1.5">
                  <span className="text-xs text-slate-600 truncate">{f.name}</span>
                  <button type="button" onClick={() => removeFile(i)} className="p-0.5 hover:text-red-500 text-slate-400">
                    <X size={12} />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm font-bold text-slate-600 hover:bg-slate-100 rounded-xl transition"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting || !title.trim()}
            className="flex items-center gap-2 px-4 py-2 text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl transition disabled:opacity-50"
          >
            {submitting ? <Loader2 size={14} className="animate-spin" /> : null}
            {submitting ? 'Creating...' : 'Create Task'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
