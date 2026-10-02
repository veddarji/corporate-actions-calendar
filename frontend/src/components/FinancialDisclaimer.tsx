import React from 'react';
import { AlertCircle } from 'lucide-react';

export const FinancialDisclaimer: React.FC = () => {
  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-lg p-3.5 flex items-start gap-3 text-xs text-slate-400 my-4">
      <AlertCircle className="w-4 h-4 text-amber-400 flex-shrink-0 mt-0.5" />
      <div>
        <span className="font-semibold text-slate-300">Financial Disclaimer:</span> The corporate actions and event data provided on this platform are for informational and educational purposes only. This information does not constitute financial, investment, or trading advice. Data is continuously ingested from primary sources (NSE / Yahoo Finance) and precomputed; always verify record dates and amounts with official exchange filings before taking financial decisions.
      </div>
    </div>
  );
};
