import React from 'react';
import KanbanColumn from './KanbanColumn';

const STATUS_COLUMNS = [
  { id: 'OPEN', title: 'To Do', color: 'bg-slate-400' },
  { id: 'IN_PROGRESS', title: 'In Progress', color: 'bg-blue-500' },
  { id: 'IN_REVIEW', title: 'In Review', color: 'bg-purple-500' },
  { id: 'COMPLETED', title: 'Done', color: 'bg-green-500' },
];

export default function KanbanBoard({ tasks = [], onStatusChange }) {
  const getTasksByStatus = (status) =>
    tasks.filter(t => t.status === status);

  return (
    <div className="flex-1 flex gap-6 overflow-x-auto pb-4">
      {STATUS_COLUMNS.map(column => (
        <KanbanColumn
          key={column.id}
          column={column}
          tasks={getTasksByStatus(column.id)}
          onStatusChange={onStatusChange}
        />
      ))}
    </div>
  );
}
