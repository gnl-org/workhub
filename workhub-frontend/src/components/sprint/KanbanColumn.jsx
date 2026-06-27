import React from 'react';
import { Plus } from 'lucide-react';
import SprintTaskCard from './SprintTaskCard';

export default function KanbanColumn({ column, tasks = [], onStatusChange }) {
  return (
    <div className="w-80 flex-shrink-0 flex flex-col">
      <div className="flex items-center justify-between mb-4 px-1">
        <div className="flex items-center gap-2">
          <span className={`w-2 h-2 rounded-full ${column.color}`} />
          <h4 className="text-xs font-black text-slate-500 uppercase tracking-wider">
            {column.title}
          </h4>
          <span className="ml-1 bg-slate-200 text-slate-600 text-[10px] px-1.5 py-0.5 rounded-full font-bold">
            {tasks.length}
          </span>
        </div>
      </div>

      <div className="flex-1 bg-slate-100/50 rounded-2xl p-2 space-y-3 border border-dashed border-slate-200 min-h-[200px]">
        {tasks.length === 0 ? (
          <div className="flex items-center justify-center h-24 text-xs text-slate-400 font-medium">
            No tasks
          </div>
        ) : (
          tasks.map(task => (
            <SprintTaskCard key={task.id} task={task} onStatusChange={onStatusChange} />
          ))
        )}
      </div>
    </div>
  );
}
