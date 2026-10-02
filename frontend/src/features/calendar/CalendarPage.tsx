import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { calendarApi } from '../../api/calendar';
import { EventDetailModal } from '../../components/EventDetailModal';
import { EventTypeBadge } from '../../components/EventTypeBadge';
import { FinancialDisclaimer } from '../../components/FinancialDisclaimer';
import { CorporateAction, EventType } from '../../types';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon, Filter, Layers, ListFilter } from 'lucide-react';
import { format, addMonths, subMonths, startOfMonth, endOfMonth, startOfWeek, endOfWeek, eachDayOfInterval, isSameMonth, isSameDay, parseISO } from 'date-fns';

export const CalendarPage: React.FC = () => {
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [selectedEvent, setSelectedEvent] = useState<CorporateAction | null>(null);
  const [selectedType, setSelectedType] = useState<string>('ALL');
  const [viewMode, setViewMode] = useState<'MONTH' | 'LIST'>('MONTH');

  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(currentDate);
  const calendarStart = startOfWeek(monthStart, { weekStartsOn: 0 });
  const calendarEnd = endOfWeek(monthEnd, { weekStartsOn: 0 });
  const fromStr = format(monthStart, 'yyyy-MM-dd');
  const toStr = format(monthEnd, 'yyyy-MM-dd');

  const { data: calendarData, isLoading } = useQuery({
    queryKey: ['calendar', fromStr, toStr],
    queryFn: () => calendarApi.getCalendar(fromStr, toStr),
    staleTime: 60_000,
  });

  const events = (calendarData?.events || []).filter((e) => {
    if (selectedType === 'ALL') return true;
    return e.eventType === selectedType;
  });

  const calendarDays = eachDayOfInterval({ start: calendarStart, end: calendarEnd });

  const getEventsForDay = (day: Date) => {
    return events.filter((e) => {
      const targetDate = e.effectiveDate || e.exDate || e.recordDate || e.announcementDate || e.paymentDate || (e as any).meetingDate;
      if (!targetDate) return false;
      try {
        return isSameDay(parseISO(targetDate), day);
      } catch {
        return false;
      }
    });
  };

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-6 animate-fadeIn">
      {/* Header Controls */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-white tracking-tight flex items-center gap-3">
            <CalendarIcon className="w-8 h-8 text-blue-500" /> Corporate Actions Calendar
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Pre-computed financial timeline across all your watched securities.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="bg-slate-900 border border-slate-800 p-1 rounded-xl flex items-center gap-1">
            <button
              onClick={() => setViewMode('MONTH')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                viewMode === 'MONTH' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              <Layers className="w-3.5 h-3.5 inline mr-1" /> Month View
            </button>
            <button
              onClick={() => setViewMode('LIST')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                viewMode === 'LIST' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              <ListFilter className="w-3.5 h-3.5 inline mr-1" /> List View
            </button>
          </div>

          <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 rounded-xl p-1">
            <button
              onClick={() => setCurrentDate(subMonths(currentDate, 1))}
              className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
            >
              <ChevronLeft className="w-5 h-5" />
            </button>
            <span className="text-sm font-semibold text-white px-3 min-w-[140px] text-center">
              {format(currentDate, 'MMMM yyyy')}
            </span>
            <button
              onClick={() => setCurrentDate(addMonths(currentDate, 1))}
              className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
            >
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <FinancialDisclaimer />

      {/* Filter Row */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-slate-800">
        <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider flex items-center gap-1 mr-2">
          <Filter className="w-3.5 h-3.5" /> Filter:
        </span>
        {['ALL', EventType.DIVIDEND, EventType.STOCK_SPLIT, EventType.BONUS_ISSUE, EventType.RIGHTS_ISSUE, EventType.EARNINGS, EventType.AGM, EventType.BUYBACK].map((type) => (
          <button
            key={type}
            onClick={() => setSelectedType(type)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition-all ${
              selectedType === type
                ? 'bg-blue-600 text-white'
                : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-white'
            }`}
          >
            {type === 'ALL' ? 'All Types' : type.replace('_', ' ')}
          </button>
        ))}
      </div>

      {/* Main Calendar Render */}
      {isLoading ? (
        <div className="h-96 bg-slate-900/60 border border-slate-800 rounded-2xl animate-pulse flex items-center justify-center text-slate-500">
          Loading pre-computed calendar data...
        </div>
      ) : viewMode === 'MONTH' ? (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          {/* Day Headers */}
          <div className="grid grid-cols-7 bg-slate-950 border-b border-slate-800 text-center py-3 text-xs font-semibold text-slate-400">
            {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((d) => (
              <div key={d}>{d}</div>
            ))}
          </div>

          {/* Days Grid */}
          <div className="grid grid-cols-7 auto-rows-fr divide-x divide-y divide-slate-800/60 bg-slate-900">
            {calendarDays.map((day) => {
              const dayEvents = getEventsForDay(day);
              const isToday = isSameDay(day, new Date());
              const isCurrentMonth = isSameMonth(day, currentDate);

              return (
                <div
                  key={day.toISOString()}
                  className={`min-h-[120px] p-2 transition-colors ${
                    !isCurrentMonth ? 'bg-slate-950/50 opacity-40' : isToday ? 'bg-blue-950/20' : 'bg-slate-900/60'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1.5">
                    <span
                      className={`text-xs font-bold w-6 h-6 rounded-full flex items-center justify-center ${
                        isToday ? 'bg-blue-600 text-white' : isCurrentMonth ? 'text-slate-300' : 'text-slate-600'
                      }`}
                    >
                      {format(day, 'd')}
                    </span>
                    {dayEvents.length > 0 && (
                      <span className="text-[10px] font-semibold text-blue-400 bg-blue-950/80 px-1.5 py-0.5 rounded border border-blue-900">
                        {dayEvents.length}
                      </span>
                    )}
                  </div>

                  <div className="space-y-1 overflow-y-auto max-h-[85px]">
                    {dayEvents.map((evt) => {
                      const colorClass =
                        evt.eventType === EventType.DIVIDEND
                          ? 'border-emerald-600/60 bg-emerald-950/60 text-emerald-300 hover:bg-emerald-900/60'
                          : evt.eventType === EventType.EARNINGS
                          ? 'border-amber-600/60 bg-amber-950/60 text-amber-300 hover:bg-amber-900/60'
                          : evt.eventType === EventType.STOCK_SPLIT
                          ? 'border-blue-600/60 bg-blue-950/60 text-blue-300 hover:bg-blue-900/60'
                          : evt.eventType === EventType.BONUS_ISSUE
                          ? 'border-yellow-600/60 bg-yellow-950/60 text-yellow-300 hover:bg-yellow-900/60'
                          : 'border-slate-700/60 bg-slate-800/90 text-slate-300 hover:bg-slate-700/90';

                      return (
                        <div
                          key={evt.id}
                          onClick={() => setSelectedEvent(evt)}
                          className={`text-[11px] p-1.5 border rounded-md cursor-pointer truncate transition-all shadow-sm ${colorClass}`}
                        >
                          <div className="font-bold truncate">{evt.companySymbol}</div>
                          <div className="text-[10px] opacity-80 truncate">{evt.eventType}</div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ) : (
        /* List View */
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden divide-y divide-slate-800">
          {events.length === 0 ? (
            <div className="p-12 text-center text-slate-500 text-sm">No events found for this month and filter.</div>
          ) : (
            events.map((evt) => (
              <div
                key={evt.id}
                onClick={() => setSelectedEvent(evt)}
                className="p-4 hover:bg-slate-800/50 flex items-center justify-between cursor-pointer transition-colors"
              >
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-xl bg-blue-950 border border-blue-800 flex items-center justify-center text-blue-400 font-bold text-xs">
                    {evt.companySymbol?.slice(0, 3)}
                  </div>
                  <div>
                    <h4 className="font-bold text-white text-sm">{evt.companySymbol} - {evt.companyName}</h4>
                    <p className="text-xs text-slate-400 mt-0.5">{evt.description || evt.eventType}</p>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <div className="text-right text-xs">
                    <span className="text-slate-500 block">Ex-Date</span>
                    <span className="font-semibold text-slate-200">{evt.exDate || 'N/A'}</span>
                  </div>
                  <EventTypeBadge type={evt.eventType} />
                </div>
              </div>
            ))
          )}
        </div>
      )}

      <EventDetailModal event={selectedEvent} onClose={() => setSelectedEvent(null)} />
    </div>
  );
};
