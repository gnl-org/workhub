import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { User, Mail, Lock, CheckCircle2 } from 'lucide-react';
import api from '../api/axios';

export default function Register() {
  const [formData, setFormData] = useState({ fullName: '', email: '', password: '' });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await api.post('/api/v1/auth/register', formData);
      navigate('/login');
    } catch (err) {
      alert("Registration failed. Please try a different email.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
      <div className="max-w-xl w-full flex bg-white rounded-[2.5rem] shadow-2xl overflow-hidden border border-slate-100">
        
        {/* Left Side: Branding/Marketing */}
        <div className="hidden md:flex w-1/2 bg-indigo-600 p-10 flex-col justify-between text-white">
          <div className="font-black text-2xl tracking-tighter">WorkHub.</div>
          <div>
            <h2 className="text-3xl font-bold leading-tight mb-4">Start managing tasks with clarity.</h2>
            <ul className="space-y-3 opacity-90 text-sm">
              <li className="flex items-center gap-2"><CheckCircle2 size={16}/> Professional Backlog</li>
              <li className="flex items-center gap-2"><CheckCircle2 size={16}/> Interactive Kanban Boards</li>
              <li className="flex items-center gap-2"><CheckCircle2 size={16}/> Team Activity Logs</li>
            </ul>
          </div>
          <div className="text-xs opacity-50">© 2026 WorkHub Inc.</div>
        </div>

        {/* Right Side: Form */}
        <div className="flex-1 p-10">
          <h1 className="text-2xl font-black text-slate-900 mb-2">Create Account</h1>
          <p className="text-slate-400 text-sm mb-8">Join the workspace to stay productive.</p>
          
          <form onSubmit={handleRegister} className="space-y-4">
            <div className="space-y-1">
              <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Full Name</label>
              <div className="relative group">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-600 transition" size={16} />
                <input 
                  type="text" required placeholder="John Doe"
                  className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-indigo-600 outline-none text-sm transition"
                  onChange={(e) => setFormData({...formData, fullName: e.target.value})}
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Email</label>
              <div className="relative group">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-600 transition" size={16} />
                <input 
                  type="email" required placeholder="john@example.com"
                  className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-indigo-600 outline-none text-sm transition"
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Password</label>
              <div className="relative group">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-600 transition" size={16} />
                <input 
                  type="password" required placeholder="••••••••"
                  className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-indigo-600 outline-none text-sm transition"
                  onChange={(e) => setFormData({...formData, password: e.target.value})}
                />
              </div>
            </div>

            <button disabled={isLoading} type="submit" className="w-full bg-indigo-600 text-white py-3 rounded-xl font-bold hover:bg-indigo-700 transition shadow-lg shadow-indigo-100 mt-4 disabled:opacity-50">
              {isLoading ? "Creating..." : "Sign Up"}
            </button>
          </form>
          <div className="text-center mt-6 text-[13px] font-medium text-slate-500">
            Have an account? <Link to="/login" className="text-indigo-600 font-bold hover:underline">Sign In</Link>
          </div>
        </div>
      </div>
    </div>
  );
}