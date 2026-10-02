import apiClient from './client';
import { ApiResponse, CorporateAction } from '../types';

export interface CalendarResponse {
  from: string;
  to: string;
  totalEvents: number;
  events: CorporateAction[];
  eventsByDate: Record<string, CorporateAction[]>;
}

export const calendarApi = {
  getCalendar: async (from?: string, to?: string): Promise<CalendarResponse> => {
    const params: Record<string, string> = {};
    if (from) params.from = from;
    if (to) params.to = to;
    const res = await apiClient.get<ApiResponse<CalendarResponse>>('/api/v1/calendar', { params });
    return res.data.data;
  },

  getUpcoming: async (): Promise<CorporateAction[]> => {
    const res = await apiClient.get<ApiResponse<CorporateAction[]>>('/api/v1/calendar/upcoming');
    return res.data.data;
  },

  getByCompany: async (companyId: string | number, page = 0, size = 20): Promise<CorporateAction[]> => {
    const res = await apiClient.get<ApiResponse<CorporateAction[]>>(
      `/api/v1/calendar/company/${companyId}`,
      { params: { page, size } }
    );
    return res.data.data;
  },
};
