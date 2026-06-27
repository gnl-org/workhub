import { useDroppable } from '@dnd-kit/core';

export default function StageInsertionPoint({ index, show }) {
  const { setNodeRef, isOver } = useDroppable({
    id: `insert:${index}`,
    data: { type: 'insertion', index },
  });

  if (!show) return <div className="h-1" />;

  return (
    <div
      ref={setNodeRef}
      className={`transition-all duration-150 mx-0 ${
        isOver ? 'h-1.5 bg-indigo-500 rounded-full -mx-1' : 'h-1'
      }`}
    />
  );
}
