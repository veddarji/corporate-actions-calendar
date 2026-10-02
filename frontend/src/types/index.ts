export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'USER' | 'ADMIN';
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface Company {
  id: string;
  symbol: string;
  name: string;
  exchange: string;
  sector?: string;
  industry?: string;
}

export interface CompanySearchResult {
  companies: Company[];
}

export enum EventType {
  DIVIDEND = 'DIVIDEND',
  STOCK_SPLIT = 'STOCK_SPLIT',
  BONUS_ISSUE = 'BONUS_ISSUE',
  RIGHTS_ISSUE = 'RIGHTS_ISSUE',
  EARNINGS = 'EARNINGS',
  AGM = 'AGM',
  EGM = 'EGM',
  BUYBACK = 'BUYBACK',
  OTHER = 'OTHER'
}

export interface CorporateAction {
  id: string;
  companyId: string;
  companySymbol: string;
  companyName: string;
  eventType: EventType;
  title: string;
  description?: string;
  recordDate?: string;
  exDate?: string;
  paymentDate?: string;
  meetingDate?: string;
  ratio?: string;
  amount?: number;
  currency?: string;
  source: string;
  lastUpdated: string;
}

export interface CalendarEvent {
  id: string;
  title: string;
  start: string;
  end?: string;
  allDay: boolean;
  extendedProps: CorporateAction;
}

export interface CalendarResponse {
  events: CorporateAction[];
}

export interface WatchlistCompany {
  id: string;
  companyId: string;
  company: Company;
  addedAt: string;
}

export interface Watchlist {
  id: string;
  userId: string;
  companies: WatchlistCompany[];
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface AdminDashboard {
  totalCompanies: number;
  totalEvents: number;
  activeJobs: number;
  conflictsCount: number;
}

export interface IngestionJobLog {
  id: string;
  source: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  recordsProcessed: number;
  startedAt: string;
  completedAt?: string;
  errorMessage?: string;
}

export interface CalendarFilter {
  eventTypes: EventType[];
  startDate?: string;
  endDate?: string;
  companyIds: string[];
}
