import React from 'react';
import { useNavigate, NavLink } from 'react-router-dom';
import { LayoutGrid, LogOut, Settings } from 'lucide-react';

export default function Sidebar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    /* This MUST have h-screen and w-20 */
    <aside className="w-20 bg-slate-900 h-screen flex flex-col items-center py-8 justify-between border-r border-slate-800 flex-shrink-0">
      <div className="flex flex-col items-center gap-8">
        <div className="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center font-black text-white text-xl shadow-lg shadow-indigo-500/20">W</div>
        
        <NavLink to="/" className={({isActive}) => `p-3 rounded-xl transition-all ${isActive ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/40' : 'text-slate-500 hover:text-white hover:bg-slate-800'}`}>
          <LayoutGrid size={24} />
        </NavLink>
        
        <div className="p-3 rounded-xl text-slate-500 hover:text-white hover:bg-slate-800 cursor-pointer transition-all">
          <Settings size={24} />
        </div>
      </div>

      <button 
        onClick={handleLogout}
        className="p-3 rounded-xl text-slate-400 hover:bg-red-500/10 hover:text-red-500 transition-all group mb-4"
        title="Logout"
      >
        <LogOut size={24} />
      </button>
    </aside>
  );
}