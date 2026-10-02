import React from 'react';
import { useWatchlist } from '../../hooks/useWatchlist';
import { Bookmark, Trash2, Plus, Building2, Globe, Shield } from 'lucide-react';
import { Link } from 'react-router-dom';

export const WatchlistPage: React.FC = () => {
  const { watchlist, isLoading, removeCompany, isRemoving } = useWatchlist();

  const companies = watchlist?.companies || [];

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8 animate-fadeIn">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white tracking-tight flex items-center gap-3">
            <Bookmark className="w-8 h-8 text-blue-500" /> My Watchlist
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Companies monitored by background proactive ingestion. Adding a company does NOT trigger scrape calls.
          </p>
        </div>

        <Link
          to="/companies"
          className="flex items-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-xl transition-colors shadow-lg shadow-blue-900/30"
        >
          <Plus className="w-4 h-4" /> Add Companies
        </Link>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 bg-slate-900/60 border border-slate-800 rounded-2xl animate-pulse" />
          ))}
        </div>
      ) : companies.length === 0 ? (
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-16 text-center">
          <Bookmark className="w-16 h-16 text-slate-700 mx-auto mb-4" />
          <h3 className="text-xl font-bold text-white">Your Watchlist is Empty</h3>
          <p className="text-sm text-slate-400 max-w-md mx-auto mt-2">
            Search for companies listed on NSE and add them to your watch list to view their pre-computed corporate actions calendar.
          </p>
          <Link
            to="/companies"
            className="mt-6 inline-flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-xl transition-colors"
          >
            <Plus className="w-4 h-4" /> Find Companies Now
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {companies.map((item) => {
            const comp = item.company;
            return (
              <div
                key={item.id}
                className="bg-slate-900 border border-slate-800 rounded-2xl p-6 relative group hover:border-slate-700 transition-all shadow-lg"
              >
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-xl bg-blue-950 border border-blue-800 flex items-center justify-center font-bold text-blue-400 text-base">
                      {comp.symbol?.slice(0, 3)}
                    </div>
                    <div>
                      <h3 className="text-lg font-bold text-white">{comp.symbol}</h3>
                      <p className="text-xs text-slate-400 truncate max-w-[180px]">{comp.name}</p>
                    </div>
                  </div>
                  <button
                    onClick={() => removeCompany(Number(comp.id))}
                    disabled={isRemoving}
                    className="text-slate-500 hover:text-rose-400 p-2 rounded-lg hover:bg-slate-800 transition-colors"
                    title="Remove from Watchlist"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div className="mt-6 pt-4 border-t border-slate-800/80 space-y-2 text-xs text-slate-400">
                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-1.5 text-slate-500">
                      <Building2 className="w-3.5 h-3.5" /> Exchange / ISIN
                    </span>
                    <span className="font-mono text-slate-300">{comp.exchange} • {comp.isin || 'N/A'}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-1.5 text-slate-500">
                      <Globe className="w-3.5 h-3.5" /> Sector
                    </span>
                    <span className="text-slate-300">{comp.sector || 'General Market'}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-1.5 text-slate-500">
                      <Shield className="w-3.5 h-3.5 text-emerald-500" /> Status
                    </span>
                    <span className="text-emerald-400 font-semibold">Active Monitoring</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
