import React from 'react';
import { Users, UserPlus, ShieldCheck } from 'lucide-react';

export default function ProjectMembersCard({ members = [], onAddClick, isAdmin }) {
  return (
    <div className="bg-white rounded-[2.5rem] p-10 border border-slate-100 shadow-sm">
      <div className="flex justify-between items-start mb-8">
        <div>
          <h2 className="text-2xl font-black text-slate-900">Team</h2>
          <p className="text-slate-400 text-xs font-medium mt-1">{members.length} collaborators</p>
        </div>
        {isAdmin && (
          <button 
            onClick={onAddClick}
            className="p-3 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100"
          >
            <UserPlus size={20} />
          </button>
        )}
      </div>

      <div className="space-y-4">
        {members.map((member, idx) => (
          <div key={idx} className="flex items-center justify-between p-3 hover:bg-slate-50 rounded-2xl transition-colors group">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-slate-100 text-slate-500 rounded-xl flex items-center justify-center font-bold text-sm">
                {member.userName.charAt(0).toUpperCase()}
              </div>
              <span className="text-sm font-bold text-slate-700">{member.userName}</span>
            </div>
            {/* If the user is the owner, show a badge */}
            <ShieldCheck size={16} className="text-slate-300 group-hover:text-indigo-500 transition-colors" />
          </div>
        ))}
        
        {members.length === 0 && (
          <p className="text-center py-4 text-slate-400 text-sm italic">No members yet.</p>
        )}
      </div>
    </div>
  );
}