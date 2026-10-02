import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client';
import { Shield, Play, Activity, Database, AlertTriangle, Users, CheckCircle2, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';

export const AdminDashboardPage: React.FC = () => {
  const queryClient = useQueryClient();

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['adminDashboard'],
    queryFn: async () => {
      const res = await apiClient.get('/api/v1/admin/dashboard');
      return res.data.data;
    },
  });

  const { data: jobs } = useQuery({
    queryKey: ['adminJobs'],
    queryFn: async () => {
      const res = await apiClient.get('/api/v1/admin/jobs?page=0&size=10');
      return res.data.data;
    },
  });

  const triggerIngestion = useMutation({
    mutationFn: async () => {
      await apiClient.post('/api/v1/admin/ingestion/trigger');
    },
    onSuccess: () => {
      toast.success('Manual background ingestion job triggered');
      queryClient.invalidateQueries({ queryKey: ['adminDashboard'] });
      queryClient.invalidateQueries({ queryKey: ['adminJobs'] });
    },
    onError: () => toast.error('Failed to trigger ingestion job'),
  });

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8 animate-fadeIn">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white tracking-tight flex items-center gap-3">
            <Shield className="w-8 h-8 text-amber-500" /> System Admin & Ingestion Operations
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Monitor background ingestion jobs, raw data quality, deduplication, and source connectivity.
          </p>
        </div>

        <button
          onClick={() => triggerIngestion.mutate()}
          disabled={triggerIngestion.isPending}
          className="flex items-center gap-2 px-5 py-2.5 bg-amber-600 hover:bg-amber-700 disabled:opacity-50 text-white text-xs font-semibold rounded-xl transition-all shadow-lg shadow-amber-950/40"
        >
          <Play className="w-4 h-4 fill-current" />
          {triggerIngestion.isPending ? 'Triggering...' : 'Run Ingestion Now'}
        </button>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold uppercase">
            <span>Total Companies</span>
            <Database className="w-4 h-4 text-blue-400" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">{stats?.totalCompanies || 0}</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold uppercase">
            <span>Normalized Events</span>
            <Activity className="w-4 h-4 text-emerald-400" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">{stats?.totalCorporateActions || 0}</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold uppercase">
            <span>Data Conflicts</span>
            <AlertTriangle className="w-4 h-4 text-amber-400" />
          </div>
          <p className="text-2xl font-bold text-amber-400 mt-2">{stats?.conflictCount || 0}</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold uppercase">
            <span>Registered Users</span>
            <Users className="w-4 h-4 text-purple-400" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">{stats?.totalUsers || 0}</p>
        </div>
      </div>

      {/* Source Status & Job History */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Source Health */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" /> External Data Source Health
          </h3>
          <div className="space-y-3">
            <div className="p-3 bg-slate-800/60 rounded-xl flex items-center justify-between border border-slate-700/50 text-xs">
              <div>
                <p className="font-semibold text-white">NSE India (Primary)</p>
                <p className="text-slate-400 text-[11px]">Corporate Actions API</p>
              </div>
              <span className="px-2 py-1 bg-emerald-950 text-emerald-400 border border-emerald-800 rounded font-semibold">
                Available
              </span>
            </div>

            <div className="p-3 bg-slate-800/60 rounded-xl flex items-center justify-between border border-slate-700/50 text-xs">
              <div>
                <p className="font-semibold text-white">Yahoo Finance (Fallback)</p>
                <p className="text-slate-400 text-[11px]">Dividend Stream API</p>
              </div>
              <span className="px-2 py-1 bg-emerald-950 text-emerald-400 border border-emerald-800 rounded font-semibold">
                Available
              </span>
            </div>

            <div className="p-3 bg-slate-800/60 rounded-xl flex items-center justify-between border border-slate-700/50 text-xs">
              <div>
                <p className="font-semibold text-white">Mock Ingestion Adapter</p>
                <p className="text-slate-400 text-[11px]">Dev / Test Standby</p>
              </div>
              <span className="px-2 py-1 bg-blue-950 text-blue-400 border border-blue-800 rounded font-semibold">
                Ready
              </span>
            </div>
          </div>
        </div>

        {/* Job History Table */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6">
          <h3 className="text-base font-bold text-white mb-4 flex items-center gap-2">
            <RefreshCw className="w-4 h-4 text-blue-400" /> Recent Ingestion Job History
          </h3>

          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-slate-950 text-slate-400 uppercase font-semibold">
                <tr>
                  <th className="p-3 rounded-l-lg">Job Name</th>
                  <th className="p-3">Source</th>
                  <th className="p-3">Status</th>
                  <th className="p-3">Processed</th>
                  <th className="p-3 rounded-r-lg">Started</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {jobs?.content?.map((job: any) => (
                  <tr key={job.id} className="hover:bg-slate-800/50">
                    <td className="p-3 font-semibold text-white">{job.jobName}</td>
                    <td className="p-3 text-slate-400">{job.source}</td>
                    <td className="p-3">
                      <span className={`px-2 py-0.5 rounded font-semibold ${
                        job.status === 'COMPLETED' ? 'bg-emerald-950 text-emerald-400' : 'bg-rose-950 text-rose-400'
                      }`}>
                        {job.status}
                      </span>
                    </td>
                    <td className="p-3 text-slate-300">{job.recordsProcessed || 0} records</td>
                    <td className="p-3 text-slate-400">{job.startedAt ? new Date(job.startedAt).toLocaleString() : 'N/A'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};
