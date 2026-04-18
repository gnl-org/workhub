import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

export default function MainLayout() {
  return (
    /* 'flex' makes Sidebar (left) and Content (right) sit side-by-side */
    <div className="flex h-screen w-full bg-slate-50 overflow-hidden">
      <Sidebar />
      
      {/* 'flex-1' ensures this container takes up all remaining space */}
      <main className="flex-1 h-full overflow-y-auto overflow-x-hidden">
        <Outlet />
      </main>
    </div>
  );
}