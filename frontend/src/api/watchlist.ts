import apiClient from './client';
import { ApiResponse, Watchlist } from '../types';

export const watchlistApi = {
  get: async (): Promise<Watchlist> => {
    const res = await apiClient.get<ApiResponse<Watchlist>>('/api/v1/watchlist');
    return res.data.data;
  },

  add: async (companyId: string | number): Promise<Watchlist> => {
    const res = await apiClient.post<ApiResponse<Watchlist>>(`/api/v1/watchlist/${companyId}`);
    return res.data.data;
  },

  remove: async (companyId: string | number): Promise<void> => {
    await apiClient.delete(`/api/v1/watchlist/${companyId}`);
  },
};
