import React from 'react';
import { MoreHorizontal, Plus, Filter } from 'lucide-react';

const columns = [
  { id: 'OPEN', title: 'To Do', color: 'bg-slate-400' },
  { id: 'IN_PROGRESS', title: 'In Progress', color: 'bg-blue-500' },
  { id: 'COMPLETED', title: 'Done', color: 'bg-green-500' }
];

export default function ActiveSprintTab({ tasks = [] }) {
  // In a real scenario, you'd filter the tasks by status
  // const getTasksByStatus = (status) => tasks.filter(t => t.status === status);

  return (
    <div className="h-full flex flex-col p-6 overflow-hidden">
      {/* Sprint Toolbar */}
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-4">
          <h3 className="font-bold text-slate-800">Sprint 1</h3>
          <span className="text-xs text-slate-400 font-medium italic">Ends in 4 days</span>
        </div>
        <div className="flex items-center gap-2">
          <button className="p-2 hover:bg-white rounded-lg border border-transparent hover:border-slate-200 transition text-slate-500">
            <Filter size={18} />
          </button>
          <button className="bg-white border border-slate-200 px-3 py-1.5 rounded-lg text-xs font-bold text-slate-700 hover:bg-slate-50 transition">
            Complete Sprint
          </button>
        </div>
      </div>

      {/* Kanban Board */}
      <div className="flex-1 flex gap-6 overflow-x-auto pb-4">
        {columns.map((column) => (
          <div key={column.id} className="w-80 flex-shrink-0 flex flex-col">
            {/* Column Header */}
            <div className="flex items-center justify-between mb-4 px-1">
              <div className="flex items-center gap-2">
                <span className={`w-2 h-2 rounded-full ${column.color}`} />
                <h4 className="text-xs font-black text-slate-500 uppercase tracking-wider">
                  {column.title}
                </h4>
                <span className="ml-2 bg-slate-200 text-slate-600 text-[10px] px-1.5 py-0.5 rounded-full font-bold">
                  2
                </span>
              </div>
              <button className="text-slate-400 hover:text-slate-600">
                <Plus size={16} />
              </button>
            </div>

            {/* Column Body / Droppable Area */}
            <div className="flex-1 bg-slate-100/50 rounded-2xl p-2 space-y-3 border border-dashed border-slate-200">
              {/* Mock Cards - Replace with actual task mapping later */}
              <SprintCard 
                id={`WH-${column.id.slice(0,1)}-1`} 
                title={`Configure ${column.title} Logic`} 
                priority="HIGH"
              />
              <SprintCard 
                id={`WH-${column.id.slice(0,1)}-2`} 
                title="Integrate API Endpoints" 
                priority="MEDIUM"
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function SprintCard({ id, title, priority }) {
  const priorityColors = {
    HIGH: 'text-red-600',
    MEDIUM: 'text-amber-600',
    LOW: 'text-blue-600'
  };

  return (
    <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200 hover:border-indigo-300 transition-colors cursor-grab active:cursor-grabbing group">
      <div className="flex justify-between items-start mb-2">
        <span className="text-[10px] font-mono text-slate-400 group-hover:text-indigo-500 font-bold transition">
          {id}
        </span>
        <MoreHorizontal size={14} className="text-slate-300" />
      </div>
      <p className="text-sm font-semibold text-slate-800 leading-tight mb-4">
        {title}
      </p>
      <div className="flex justify-between items-center">
        <div className={`text-[10px] font-black uppercase ${priorityColors[priority]}`}>
          {priority}
        </div>
        <div className="w-6 h-6 rounded-full bg-slate-100 border border-white flex items-center justify-center text-[10px] font-bold text-slate-500">
          JD
        </div>
      </div>
    </div>
  );
}