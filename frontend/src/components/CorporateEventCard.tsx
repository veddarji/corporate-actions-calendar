import React from 'react';
import { CorporateAction } from '../types';
import { EventTypeBadge } from './EventTypeBadge';
import { Calendar, DollarSign, Building2, Clock } from 'lucide-react';
import { format, parseISO } from 'date-fns';

interface Props {
  event: CorporateAction;
  onSelect?: (event: CorporateAction) => void;
}

export const CorporateEventCard: React.FC<Props> = ({ event, onSelect }) => {
  const displayDate = event.exDate || event.recordDate || event.paymentDate || event.meetingDate;
  
  const formatDateStr = (dateStr?: string) => {
    if (!dateStr) return 'N/A';
    try {
      return format(parseISO(dateStr), 'dd MMM yyyy');
    } catch {
      return dateStr;
    }
  };

  return (
    <div
      onClick={() => onSelect?.(event)}
      className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl p-4 transition-all duration-200 cursor-pointer hover:shadow-lg hover:shadow-blue-950/20 group"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-lg bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-xs text-blue-400 group-hover:bg-blue-600 group-hover:text-white transition-colors">
            {event.companySymbol?.slice(0, 3) || 'SEC'}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h4 className="font-semibold text-white text-sm group-hover:text-blue-400 transition-colors">
                {event.companySymbol}
              </h4>
              <span className="text-xs text-slate-500 font-mono">({event.companySymbol})</span>
            </div>
            <p className="text-xs text-slate-400 truncate max-w-[200px]">{event.companyName}</p>
          </div>
        </div>
        <EventTypeBadge type={event.eventType} />
      </div>

      <div className="mt-4 pt-3 border-t border-slate-800/80 grid grid-cols-2 gap-2 text-xs">
        <div className="flex items-center gap-1.5 text-slate-400">
          <Calendar className="w-3.5 h-3.5 text-slate-500" />
          <span>Ex-Date: <strong className="text-slate-200">{formatDateStr(event.exDate)}</strong></span>
        </div>

        {event.amount !== undefined && event.amount !== null && (
          <div className="flex items-center gap-1.5 text-slate-400">
            <DollarSign className="w-3.5 h-3.5 text-emerald-500" />
            <span>Amount: <strong className="text-emerald-400">₹{event.amount}</strong></span>
          </div>
        )}

        {event.ratio && (
          <div className="flex items-center gap-1.5 text-slate-400">
            <Building2 className="w-3.5 h-3.5 text-amber-500" />
            <span>Ratio: <strong className="text-amber-300">{event.ratio}</strong></span>
          </div>
        )}

        <div className="flex items-center gap-1.5 text-slate-500 col-span-2 mt-1 text-[11px]">
          <Clock className="w-3 h-3" />
          <span>Source: {event.source || 'NSE'} • Updated {formatDateStr(event.lastUpdated)}</span>
        </div>
      </div>
    </div>
  );
};
