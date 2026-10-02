import React from 'react';
import { CorporateAction } from '../types';
import { EventTypeBadge } from './EventTypeBadge';
import { X, Calendar, Building2, ExternalLink, ShieldCheck, CheckCircle2 } from 'lucide-react';
import { format, parseISO } from 'date-fns';

interface Props {
  event: CorporateAction | null;
  onClose: () => void;
}

export const EventDetailModal: React.FC<Props> = ({ event, onClose }) => {
  if (!event) return null;

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'N/A';
    try {
      return format(parseISO(dateStr), 'EEEE, MMMM dd, yyyy');
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 animate-fadeIn">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="p-6 border-b border-slate-800 flex items-start justify-between bg-slate-950/40">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-blue-600/10 border border-blue-500/20 flex items-center justify-center text-blue-400 font-bold text-lg">
              {event.companySymbol?.slice(0, 3)}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-xl font-bold text-white">{event.companySymbol}</h3>
                <EventTypeBadge type={event.eventType} />
              </div>
              <p className="text-xs text-slate-400 mt-0.5">{event.companyName}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6 max-h-[70vh] overflow-y-auto">
          {/* Key Metric Highlight */}
          <div className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-4 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 uppercase tracking-wider font-semibold">Primary Event Detail</p>
              <p className="text-lg font-bold text-white mt-0.5">
                {event.amount !== undefined && event.amount !== null ? `₹${event.amount} per share` : event.ratio ? `Ratio: ${event.ratio}` : event.title || event.eventType}
              </p>
            </div>
            <div className="flex items-center gap-1.5 text-xs text-emerald-400 bg-emerald-950/50 border border-emerald-800/50 px-3 py-1.5 rounded-lg">
              <ShieldCheck className="w-4 h-4" />
              <span>Verified Ingestion</span>
            </div>
          </div>

          {/* Dates Grid */}
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-3">Key Timelines</h4>
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="bg-slate-800/30 p-3 rounded-lg border border-slate-800">
                <span className="text-slate-500 block mb-1">Ex-Dividend / Split Date</span>
                <span className="font-semibold text-slate-200">{formatDate(event.exDate)}</span>
              </div>
              <div className="bg-slate-800/30 p-3 rounded-lg border border-slate-800">
                <span className="text-slate-500 block mb-1">Record Date</span>
                <span className="font-semibold text-slate-200">{formatDate(event.recordDate)}</span>
              </div>
              <div className="bg-slate-800/30 p-3 rounded-lg border border-slate-800">
                <span className="text-slate-500 block mb-1">Payment / Execution Date</span>
                <span className="font-semibold text-slate-200">{formatDate(event.paymentDate)}</span>
              </div>
              <div className="bg-slate-800/30 p-3 rounded-lg border border-slate-800">
                <span className="text-slate-500 block mb-1">AGM / Meeting Date</span>
                <span className="font-semibold text-slate-200">{formatDate(event.meetingDate)}</span>
              </div>
            </div>
          </div>

          {/* Description */}
          {event.description && (
            <div>
              <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">Filing Summary</h4>
              <p className="text-xs text-slate-300 bg-slate-800/40 p-3 rounded-lg border border-slate-800 leading-relaxed">
                {event.description}
              </p>
            </div>
          )}

          {/* Provenance */}
          <div className="pt-3 border-t border-slate-800 flex items-center justify-between text-xs text-slate-500">
            <div className="flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-blue-400" />
              <span>Source: <strong className="text-slate-400">{event.source || 'NSE API'}</strong></span>
            </div>
            <span>Last Synced: {formatDate(event.lastUpdated)}</span>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 bg-slate-950 border-t border-slate-800 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
          >
            Close Details
          </button>
        </div>
      </div>
    </div>
  );
};
