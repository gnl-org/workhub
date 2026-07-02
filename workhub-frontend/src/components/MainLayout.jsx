import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import NotificationBell from './NotificationBell';

export default function MainLayout() {
  return (
    <div className="flex h-screen w-full bg-slate-50 overflow-hidden">
      <Sidebar />
      
      <div className="flex-1 flex flex-col h-full overflow-hidden">
        <header className="flex items-center justify-end px-6 py-3 border-b border-slate-200 bg-white shrink-0">
          <NotificationBell />
        </header>
        <main className="flex-1 overflow-y-auto overflow-x-hidden">
          <Outlet />
        </main>
      </div>
    </div>
  );
}