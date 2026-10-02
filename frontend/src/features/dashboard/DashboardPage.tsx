import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { calendarApi } from '../../api/calendar';
import { useWatchlist } from '../../hooks/useWatchlist';
import { CorporateEventCard } from '../../components/CorporateEventCard';
import { EventDetailModal } from '../../components/EventDetailModal';
import { FinancialDisclaimer } from '../../components/FinancialDisclaimer';
import { CorporateAction, EventType } from '../../types';
import { Calendar as CalendarIcon, Bookmark, TrendingUp, RefreshCw, Filter, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  const { watchlist, isLoading: watchlistLoading } = useWatchlist();
  const [selectedEvent, setSelectedEvent] = useState<CorporateAction | null>(null);
  const [activeFilter, setActiveFilter] = useState<string>('ALL');

  const { data: upcomingEvents, isLoading: eventsLoading, refetch } = useQuery({
    queryKey: ['upcomingEvents'],
    queryFn: calendarApi.getUpcoming,
    staleTime: 30_000,
  });

  const filteredEvents = (upcomingEvents || []).filter((event) => {
    if (activeFilter === 'ALL') return true;
    return event.eventType === activeFilter;
  });

  const totalWatchlist = watchlist?.companies?.length || 0;

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8 animate-fadeIn">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white tracking-tight flex items-center gap-3">
            Corporate Actions Dashboard
            <span className="text-xs font-normal text-blue-400 bg-blue-950/60 border border-blue-800/50 px-2.5 py-1 rounded-full flex items-center gap-1.5">
              <Sparkles className="w-3 h-3 text-blue-400" /> Precomputed & Live
            </span>
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Real-time proactive financial event stream for your watched companies.
          </p>
        </div>
        <button
          onClick={() => refetch()}
          className="flex items-center gap-2 text-xs font-semibold text-slate-300 bg-slate-900 border border-slate-800 hover:bg-slate-800 px-4 py-2.5 rounded-xl transition-all"
        >
          <RefreshCw className="w-3.5 h-3.5 text-blue-400" />
          Refresh View
        </button>
      </div>

      <FinancialDisclaimer />

      {/* Overview Metric Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Watchlist Size</span>
            <Bookmark className="w-5 h-5 text-blue-400" />
          </div>
          <p className="text-3xl font-bold text-white mt-3">{totalWatchlist}</p>
          <p className="text-xs text-slate-500 mt-2">Active monitored securities</p>
          <div className="absolute -bottom-4 -right-4 w-24 h-24 bg-blue-600/5 rounded-full blur-xl" />
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Upcoming Events</span>
            <CalendarIcon className="w-5 h-5 text-emerald-400" />
          </div>
          <p className="text-3xl font-bold text-white mt-3">{upcomingEvents?.length || 0}</p>
          <p className="text-xs text-slate-500 mt-2">Next 30 days corporate actions</p>
          <div className="absolute -bottom-4 -right-4 w-24 h-24 bg-emerald-600/5 rounded-full blur-xl" />
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Ingestion Pipeline</span>
            <TrendingUp className="w-5 h-5 text-amber-400" />
          </div>
          <p className="text-3xl font-bold text-white mt-3">Active</p>
          <p className="text-xs text-slate-500 mt-2">Background ingestion running every 15m</p>
          <div className="absolute -bottom-4 -right-4 w-24 h-24 bg-amber-600/5 rounded-full blur-xl" />
        </div>
      </div>

      {/* Filter Tabs & Event Stream */}
      <div className="space-y-4">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <Filter className="w-4 h-4 text-blue-400" /> Upcoming Corporate Actions
          </h2>
          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            {['ALL', EventType.DIVIDEND, EventType.STOCK_SPLIT, EventType.BONUS_ISSUE, EventType.EARNINGS, EventType.AGM].map((filter) => (
              <button
                key={filter}
                onClick={() => setActiveFilter(filter)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  activeFilter === filter
                    ? 'bg-blue-600 text-white'
                    : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-white'
                }`}
              >
                {filter === 'ALL' ? 'All Events' : filter.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        {eventsLoading || watchlistLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="h-40 bg-slate-900/60 border border-slate-800 rounded-xl animate-pulse" />
            ))}
          </div>
        ) : filteredEvents.length === 0 ? (
          <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-12 text-center">
            <CalendarIcon className="w-12 h-12 text-slate-600 mx-auto mb-4" />
            <h3 className="text-lg font-bold text-white">No Upcoming Events Found</h3>
            <p className="text-sm text-slate-400 max-w-md mx-auto mt-1">
              Add more companies to your watchlist or adjust your filter to see precomputed events.
            </p>
            <Link
              to="/companies"
              className="mt-6 inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-xl transition-colors"
            >
              Search & Add Companies
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredEvents.map((event) => (
              <CorporateEventCard
                key={event.id}
                event={event}
                onSelect={(evt) => setSelectedEvent(evt)}
              />
            ))}
          </div>
        )}
      </div>

      <EventDetailModal event={selectedEvent} onClose={() => setSelectedEvent(null)} />
    </div>
  );
};
