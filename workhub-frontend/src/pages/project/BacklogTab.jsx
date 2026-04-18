export default function BacklogTab({ projectId }) {
  return (
    <div className="p-8 max-w-6xl mx-auto">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-4 border-b flex justify-between items-center bg-slate-50/50">
          <span className="text-xs font-black text-slate-500 uppercase">Backlog List</span>
          <button className="text-xs font-bold text-indigo-600 hover:underline">+ New Task</button>
        </div>
        {/* Task rows go here */}
        <div className="divide-y divide-slate-100">
          {[1, 2, 3].map(i => (
            <div key={i} className="p-4 hover:bg-slate-50 cursor-pointer flex justify-between items-center transition">
              <div className="flex items-center gap-4">
                <div className="w-2 h-2 rounded-full bg-slate-300" />
                <span className="text-xs font-mono text-slate-400">WH-{i}</span>
                <span className="font-medium text-slate-700">Sample Task Name {i}</span>
              </div>
              <span className="text-[10px] font-bold px-2 py-1 bg-amber-50 text-amber-600 rounded">MEDIUM</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}