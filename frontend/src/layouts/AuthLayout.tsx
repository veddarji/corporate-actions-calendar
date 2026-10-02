import { Outlet } from 'react-router-dom';
import { TrendingUp } from 'lucide-react';

export function AuthLayout() {
  return (
    <div className="min-h-screen bg-slate-950 flex">
      {/* Left panel - branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-blue-900 via-slate-900 to-slate-950 flex-col justify-between p-12">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
            <TrendingUp className="w-6 h-6 text-white" />
          </div>
          <span className="text-white font-bold text-xl">CorpActions Calendar</span>
        </div>

        <div>
          <h1 className="text-4xl font-bold text-white leading-tight">
            Stay ahead of<br />
            <span className="text-blue-400">corporate events</span>
          </h1>
          <p className="mt-4 text-slate-300 text-lg max-w-sm">
            Track dividends, splits, results, AGMs, and more — all automatically updated in real-time.
          </p>
          <div className="mt-8 grid grid-cols-2 gap-4">
            {[
              { label: 'Event Types', value: '8+' },
              { label: 'Companies', value: '500+' },
              { label: 'Data Sources', value: 'NSE & More' },
              { label: 'Updates', value: 'Every 15 min' },
            ].map(({ label, value }) => (
              <div key={label} className="bg-slate-800/50 rounded-lg p-4">
                <p className="text-2xl font-bold text-blue-400">{value}</p>
                <p className="text-sm text-slate-400 mt-1">{label}</p>
              </div>
            ))}
          </div>
        </div>

        <p className="text-xs text-slate-500">
          ⚠️ For informational purposes only. Not investment advice.
        </p>
      </div>

      {/* Right panel - form */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          <div className="lg:hidden flex items-center gap-3 mb-8">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
              <TrendingUp className="w-5 h-5 text-white" />
            </div>
            <span className="text-white font-bold">CorpActions Calendar</span>
          </div>
          <Outlet />
        </div>
      </div>
    </div>
  );
}
