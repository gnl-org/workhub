import React, { useState } from 'react';
import { useDroppable } from '@dnd-kit/core';
import StageHeader from './StageHeader';
import BacklogTaskRow from './BacklogTaskRow';

export default function StageSection({ stage, tasks, stages, onRename, onDelete, onMoveTask }) {
  const [isCollapsed, setIsCollapsed] = useState(false);

  const { setNodeRef, isOver } = useDroppable({
    id: stage.id,
    data: { type: 'stage' },
  });

  return (
    <div className={`bg-white rounded-2xl border shadow-sm transition-colors ${
      isOver ? 'border-indigo-400 bg-indigo-50/30' : 'border-slate-100'
    }`}>
      <StageHeader
        stage={stage}
        taskCount={tasks.length}
        onRename={onRename}
        onDelete={onDelete}
        isCollapsed={isCollapsed}
        onToggleCollapse={() => setIsCollapsed(!isCollapsed)}
      />

      {!isCollapsed && (
        <div ref={setNodeRef} className="divide-y divide-slate-50 min-h-[40px]">
          {tasks.length === 0 ? (
            <div className="py-8 text-center">
              <p className="text-xs text-slate-400 font-medium">No tasks in this stage</p>
            </div>
          ) : (
            tasks.map(task => (
              <BacklogTaskRow
                key={task.id}
                task={task}
                stages={stages}
                onMoveTask={onMoveTask}
              />
            ))
          )}
        </div>
      )}
    </div>
  );
}
