export default function InfoTab({ project, projectId }) {
  return (
    <div className="p-8 max-w-5xl mx-auto grid grid-cols-3 gap-8">
      <div className="col-span-2 space-y-6">
        <section className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <h3 className="text-sm font-black text-slate-400 uppercase mb-4">About Project</h3>
          <p className="text-slate-700 leading-relaxed">{project?.description}</p>
        </section>
        
        <section className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <h3 className="text-sm font-black text-slate-400 uppercase mb-4">Recent Activity</h3>
          <div className="text-slate-400 italic text-sm text-center py-10">
            Latest 10 tasks will be listed here...
          </div>
        </section>
      </div>

      <div className="col-span-1 space-y-6">
        <section className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <h3 className="text-sm font-black text-slate-400 uppercase mb-4">Team Members</h3>
          <div className="space-y-3">
             <div className="flex items-center gap-3 text-sm font-medium">
               <div className="w-8 h-8 rounded-full bg-indigo-600 text-white flex items-center justify-center">JD</div>
               <span>{project?.ownerName} (Owner)</span>
             </div>
          </div>
        </section>
      </div>
    </div>
  );
}