import React from 'react';
import { useCompanySearch } from '../../hooks/useCompanySearch';
import { useWatchlist } from '../../hooks/useWatchlist';
import { Search, Plus, Check, Building2, TrendingUp, Sparkles } from 'lucide-react';

export const CompanySearchPage: React.FC = () => {
  const { query, setQuery, results, isLoading } = useCompanySearch();
  const { addCompany, removeCompany, isWatched, isAdding } = useWatchlist();

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8 animate-fadeIn">
      <div>
        <h1 className="text-3xl font-bold text-white tracking-tight flex items-center gap-3">
          <Search className="w-8 h-8 text-blue-500" /> Search Companies
        </h1>
        <p className="text-sm text-slate-400 mt-1">
          Search the directory of listed companies to add them to your calendar watchlist.
        </p>
      </div>

      {/* Search Input */}
      <div className="relative max-w-2xl">
        <Search className="w-5 h-5 text-slate-400 absolute left-4 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search by Symbol (e.g. RELIANCE, TCS), Name, or ISIN..."
          className="w-full pl-12 pr-4 py-4 bg-slate-900 border border-slate-800 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm shadow-xl transition-all"
        />
        {isLoading && (
          <div className="absolute right-4 top-1/2 -translate-y-1/2 text-xs text-blue-400 font-medium">
            Searching...
          </div>
        )}
      </div>

      {/* Search Results */}
      {query.length === 0 ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-12 text-center text-slate-500">
          <Sparkles className="w-12 h-12 mx-auto mb-3 text-slate-600" />
          <p className="text-sm">Type a company symbol or name above to find listed securities.</p>
        </div>
      ) : results.length === 0 && !isLoading ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-12 text-center text-slate-500">
          No companies found matching "{query}".
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {results.map((company) => {
            const watched = isWatched(Number(company.id));
            return (
              <div
                key={company.id}
                className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl p-5 flex items-center justify-between transition-all"
              >
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-xl bg-blue-950 border border-blue-800 flex items-center justify-center font-bold text-blue-400 text-sm">
                    {company.symbol?.slice(0, 3)}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-bold text-white text-base">{company.symbol}</h3>
                      <span className="text-xs text-slate-500 bg-slate-800 px-2 py-0.5 rounded">
                        {company.exchange}
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 mt-0.5">{company.name}</p>
                    {company.sector && (
                      <p className="text-[11px] text-slate-500 mt-1 flex items-center gap-1">
                        <Building2 className="w-3 h-3" /> {company.sector}
                      </p>
                    )}
                  </div>
                </div>

                <button
                  onClick={() => {
                    if (watched) {
                      removeCompany(Number(company.id));
                    } else {
                      addCompany(Number(company.id));
                    }
                  }}
                  disabled={isAdding}
                  className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
                    watched
                      ? 'bg-emerald-950/80 border border-emerald-800 text-emerald-400 hover:bg-rose-950/80 hover:border-rose-800 hover:text-rose-400'
                      : 'bg-blue-600 hover:bg-blue-700 text-white'
                  }`}
                >
                  {watched ? (
                    <>
                      <Check className="w-4 h-4" /> Added
                    </>
                  ) : (
                    <>
                      <Plus className="w-4 h-4" /> Watch
                    </>
                  )}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
