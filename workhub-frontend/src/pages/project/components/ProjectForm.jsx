import React, { useState } from 'react';
import { Loader2 } from 'lucide-react';

export default function ProjectForm({ onSubmit, onCancel, isSubmitting, initialData = {} }) {
  const [formData, setFormData] = useState({
    title: initialData.title || '',
    description: initialData.description || ''
  });  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    // Validation
    if (formData.title.trim().length < 3) {
      setError('Project title must be at least 3 characters.');
      return;
    }

    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div>
        <label className="text-xs font-bold text-slate-400 uppercase tracking-widest ml-1">
          Project Title
        </label>
        <input 
          type="text"
          required
          placeholder="e.g. WorkHub Backend"
          className="w-full mt-2 px-6 py-4 bg-slate-50 border border-slate-100 rounded-2xl focus:bg-white focus:ring-4 focus:ring-indigo-600/5 focus:border-indigo-600 outline-none transition-all font-medium"
          value={formData.title}
          onChange={(e) => setFormData({ ...formData, title: e.target.value })}
        />
      </div>

      <div>
        <label className="text-xs font-bold text-slate-400 uppercase tracking-widest ml-1">
          Description
        </label>
        <textarea 
          rows="4"
          placeholder="Describe the goals of this project..."
          className="w-full mt-2 px-6 py-4 bg-slate-50 border border-slate-100 rounded-2xl focus:bg-white focus:ring-4 focus:ring-indigo-600/5 focus:border-indigo-600 outline-none transition-all font-medium resize-none"
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
        />
      </div>

      {error && (
        <p className="text-red-500 text-sm font-bold animate-pulse">{error}</p>
      )}

      <div className="flex gap-4 pt-2">
        <button 
          type="button"
          onClick={onCancel}
          className="flex-1 py-4 rounded-2xl font-bold text-slate-500 hover:bg-slate-50 transition-all"
        >
          Cancel
        </button>
        <button 
          disabled={isSubmitting}
          className="flex-1 bg-indigo-600 text-white py-4 rounded-2xl font-bold flex items-center justify-center gap-2 hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-100 disabled:opacity-70"
        >
          {isSubmitting ? <Loader2 className="animate-spin" size={20} /> : "Submit"}
        </button>
      </div>
    </form>
  );
}