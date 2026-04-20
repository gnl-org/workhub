import React from 'react';
import Modal from './Modal';
import { AlertTriangle, Loader2 } from 'lucide-react';

export default function ConfirmModal({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title = "Are you sure?", 
  message, 
  confirmText = "Delete", 
  isSubmitting = false 
}) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} isPreventClose={isSubmitting}>
      <div className="flex flex-col items-center text-center">
        <div className="w-16 h-16 bg-red-50 text-red-500 rounded-2xl flex items-center justify-center mb-6">
          <AlertTriangle size={32} />
        </div>
        <p className="text-slate-500 font-medium mb-10">
          {message || "This action cannot be undone. Please confirm to proceed."}
        </p>
        
        <div className="flex gap-4 w-full">
          <button 
            onClick={onClose}
            disabled={isSubmitting}
            className="flex-1 py-4 rounded-2xl font-bold text-slate-500 hover:bg-slate-50 transition-all disabled:opacity-50"
          >
            Cancel
          </button>
          <button 
            onClick={onConfirm}
            disabled={isSubmitting}
            className="flex-1 bg-red-500 text-white py-4 rounded-2xl font-bold flex items-center justify-center gap-2 hover:bg-red-600 transition-all shadow-xl shadow-red-100 disabled:opacity-70"
          >
            {isSubmitting ? <Loader2 className="animate-spin" size={20} /> : confirmText}
          </button>
        </div>
      </div>
    </Modal>
  );
}