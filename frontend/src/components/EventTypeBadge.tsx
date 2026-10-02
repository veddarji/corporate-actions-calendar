import React from 'react';
import { EventType } from '../types';
import clsx from 'clsx';

interface Props {
  type: EventType | string;
  className?: string;
}

const config: Record<string, { label: string; bg: string; text: string; border: string }> = {
  DIVIDEND: { label: 'Dividend', bg: 'bg-emerald-950/60', text: 'text-emerald-400', border: 'border-emerald-800/60' },
  STOCK_SPLIT: { label: 'Stock Split', bg: 'bg-blue-950/60', text: 'text-blue-400', border: 'border-blue-800/60' },
  BONUS_ISSUE: { label: 'Bonus Issue', bg: 'bg-amber-950/60', text: 'text-amber-400', border: 'border-amber-800/60' },
  RIGHTS_ISSUE: { label: 'Rights Issue', bg: 'bg-purple-950/60', text: 'text-purple-400', border: 'border-purple-800/60' },
  EARNINGS: { label: 'Earnings', bg: 'bg-orange-950/60', text: 'text-orange-400', border: 'border-orange-800/60' },
  AGM: { label: 'AGM', bg: 'bg-slate-800/80', text: 'text-slate-300', border: 'border-slate-700' },
  EGM: { label: 'EGM', bg: 'bg-slate-800/80', text: 'text-slate-300', border: 'border-slate-700' },
  BUYBACK: { label: 'Buyback', bg: 'bg-rose-950/60', text: 'text-rose-400', border: 'border-rose-800/60' },
  OTHER: { label: 'Other', bg: 'bg-slate-800/80', text: 'text-slate-400', border: 'border-slate-700' },
};

export const EventTypeBadge: React.FC<Props> = ({ type, className }) => {
  const item = config[type] || { label: type, bg: 'bg-slate-800', text: 'text-slate-300', border: 'border-slate-700' };

  return (
    <span
      className={clsx(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border',
        item.bg,
        item.text,
        item.border,
        className
      )}
    >
      {item.label}
    </span>
  );
};
