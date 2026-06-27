import React, { useState } from 'react';
import StageHeader from './StageHeader';
import BacklogTaskRow from './BacklogTaskRow';

export default function StageSection({ stage, tasks, stages, onRename, onDelete, onMoveTask }) {
  const [isCollapsed, setIsCollapsed] = useState(false);

  return (
    <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <StageHeader
        stage={stage}
        taskCount={tasks.length}
        onRename={onRename}
        onDelete={onDelete}
        isCollapsed={isCollapsed}
        onToggleCollapse={() => setIsCollapsed(!isCollapsed)}
      />

      {!isCollapsed && (
        <div className="divide-y divide-slate-50">
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
