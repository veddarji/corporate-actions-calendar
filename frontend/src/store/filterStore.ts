import { create } from 'zustand';
import { EventType } from '../types';

interface FilterState {
  selectedEventTypes: EventType[];
  selectedCompanyIds: number[];
  dateFrom: string | null;
  dateTo: string | null;
  setEventTypes: (types: EventType[]) => void;
  toggleEventType: (type: EventType) => void;
  setCompanyIds: (ids: number[]) => void;
  setDateRange: (from: string | null, to: string | null) => void;
  clearFilters: () => void;
}

export const useFilterStore = create<FilterState>((set, get) => ({
  selectedEventTypes: [],
  selectedCompanyIds: [],
  dateFrom: null,
  dateTo: null,

  setEventTypes: (types) => set({ selectedEventTypes: types }),

  toggleEventType: (type) => {
    const current = get().selectedEventTypes;
    if (current.includes(type)) {
      set({ selectedEventTypes: current.filter((t) => t !== type) });
    } else {
      set({ selectedEventTypes: [...current, type] });
    }
  },

  setCompanyIds: (ids) => set({ selectedCompanyIds: ids }),

  setDateRange: (from, to) => set({ dateFrom: from, dateTo: to }),

  clearFilters: () =>
    set({ selectedEventTypes: [], selectedCompanyIds: [], dateFrom: null, dateTo: null }),
}));
