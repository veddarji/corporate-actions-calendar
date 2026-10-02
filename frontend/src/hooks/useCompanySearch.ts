import { useState, useEffect, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { companiesApi } from '../api/companies';

export function useCompanySearch(debounceMs = 300) {
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query), debounceMs);
    return () => clearTimeout(timer);
  }, [query, debounceMs]);

  const { data, isLoading } = useQuery({
    queryKey: ['companies', 'search', debouncedQuery],
    queryFn: () => companiesApi.search(debouncedQuery),
    enabled: debouncedQuery.length >= 1,
    staleTime: 60_000,
  });

  return {
    query,
    setQuery,
    results: data?.content ?? [],
    isLoading: isLoading && debouncedQuery.length >= 1,
    totalResults: data?.totalElements ?? 0,
  };
}
