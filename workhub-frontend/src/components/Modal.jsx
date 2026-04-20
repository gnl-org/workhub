import React from 'react';
import { X } from 'lucide-react';

export default function Modal({ isOpen, onClose, title, subtitle, children, isPreventClose = false }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-slate-900/40 backdrop-blur-md animate-in fade-in duration-300"
        onClick={() => !isPreventClose && onClose()}
      />
      
      {/* Container */}
      <div className="relative bg-white w-full max-w-lg rounded-[3rem] p-10 shadow-2xl animate-in zoom-in-95 duration-200">
        <button 
          onClick={onClose}
          disabled={isPreventClose}
          className="absolute top-8 right-8 p-2 text-slate-300 hover:text-slate-600 transition-colors disabled:opacity-0"
        >
          <X size={24} />
        </button>
        
        <div className="mb-8">
          <h2 className="text-3xl font-black text-slate-900">{title}</h2>
          {subtitle && <p className="text-slate-500 font-medium mt-1">{subtitle}</p>}
        </div>

        {children}
      </div>
    </div>
  );
}