import React, { useState } from 'react';
import { DndContext, DragOverlay, PointerSensor, useSensor, useSensors, useDroppable } from '@dnd-kit/core';
import SprintTaskCard from './SprintTaskCard';

const STATUS_COLUMNS = [
  { id: 'OPEN', title: 'To Do', color: 'bg-slate-400' },
  { id: 'BLOCKED', title: 'Blocked', color: 'bg-red-500' },
  { id: 'IN_PROGRESS', title: 'In Progress', color: 'bg-blue-500' },
  { id: 'IN_REVIEW', title: 'In Review', color: 'bg-purple-500' },
  { id: 'COMPLETED', title: 'Done', color: 'bg-green-500' },
];

function KanbanDropZone({ columnId, children }) {
  const { setNodeRef, isOver } = useDroppable({ id: columnId });
  return (
    <div
      ref={setNodeRef}
      className={`flex-1 bg-slate-100/50 rounded-2xl p-2 space-y-3 border border-dashed min-h-[200px] transition-colors ${
        isOver ? 'border-indigo-400 bg-indigo-50/50' : 'border-slate-200'
      }`}
    >
      {children}
    </div>
  );
}

export default function KanbanBoard({ tasks = [], onStatusChange, onTaskClick }) {
  const [activeTask, setActiveTask] = useState(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );

  const getTasksByStatus = (status) =>
    tasks.filter(t => t.status === status);

  const handleDragStart = (event) => {
    const task = tasks.find(t => t.id === event.active.id);
    if (task) setActiveTask(task);
  };

  const handleDragEnd = (event) => {
    setActiveTask(null);
    const { active, over } = event;
    if (!over) return;

    const taskId = active.id;
    const task = tasks.find(t => t.id === taskId);
    if (!task) return;

    // Determine target status: could be a column ID or a task whose status to adopt
    let targetStatus = over.id;
    const isColumn = STATUS_COLUMNS.some(c => c.id === targetStatus);
    if (!isColumn) {
      const overTask = tasks.find(t => t.id === over.id);
      if (overTask) targetStatus = overTask.status;
      else return;
    }

    if (targetStatus !== task.status) {
      onStatusChange(taskId, targetStatus);
    }
  };

  return (
    <DndContext
      sensors={sensors}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      <div className="flex-1 flex gap-6 overflow-x-auto pb-4">
        {STATUS_COLUMNS.map(column => (
          <div key={column.id} className="w-80 flex-shrink-0 flex flex-col">
            <div className="flex items-center justify-between mb-4 px-1">
              <div className="flex items-center gap-2">
                <span className={`w-2 h-2 rounded-full ${column.color}`} />
                <h4 className="text-xs font-black text-slate-500 uppercase tracking-wider">
                  {column.title}
                </h4>
                <span className="ml-1 bg-slate-200 text-slate-600 text-[10px] px-1.5 py-0.5 rounded-full font-bold">
                  {getTasksByStatus(column.id).length}
                </span>
              </div>
            </div>
            <KanbanDropZone columnId={column.id}>
              {getTasksByStatus(column.id).length === 0 ? (
                <div className="flex items-center justify-center h-24 text-xs text-slate-400 font-medium">
                  No tasks
                </div>
              ) : (
                getTasksByStatus(column.id).map(task => (
                  <SprintTaskCard key={task.id} task={task} onStatusChange={onStatusChange} onClick={() => onTaskClick?.(task.id)} />
                ))
              )}
            </KanbanDropZone>
          </div>
        ))}
      </div>

      <DragOverlay>
        {activeTask ? (
          <div className="bg-white p-4 rounded-xl shadow-lg border border-indigo-300 rotate-2 opacity-90">
            <p className="text-sm font-semibold text-slate-800">{activeTask.title}</p>
          </div>
        ) : null}
      </DragOverlay>
    </DndContext>
  );
}
