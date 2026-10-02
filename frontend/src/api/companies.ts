import apiClient from './client';
import { ApiResponse, Company, PagedResponse, CorporateAction } from '../types';

export const companiesApi = {
  search: async (query: string, page = 0, size = 20): Promise<PagedResponse<Company>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<Company>>>(
      `/api/v1/companies/search`,
      { params: { q: query, page, size } }
    );
    return res.data.data;
  },

  searchPublic: async (query: string, page = 0, size = 20): Promise<PagedResponse<Company>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<Company>>>(
      `/api/v1/public/companies/search`,
      { params: { q: query, page, size } }
    );
    return res.data.data;
  },

  getById: async (id: string | number): Promise<Company> => {
    const res = await apiClient.get<ApiResponse<Company>>(`/api/v1/companies/${id}`);
    return res.data.data;
  },

  getEvents: async (companyId: string | number, page = 0, size = 20) => {
    const res = await apiClient.get<ApiResponse<CorporateAction[]>>(
      `/api/v1/calendar/company/${companyId}`,
      { params: { page, size } }
    );
    return res.data.data;
  },
};
