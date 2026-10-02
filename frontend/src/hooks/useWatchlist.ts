import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { watchlistApi } from '../api/watchlist';
import toast from 'react-hot-toast';

export function useWatchlist() {
  const queryClient = useQueryClient();

  const { data: watchlist, isLoading, error } = useQuery({
    queryKey: ['watchlist'],
    queryFn: watchlistApi.get,
    staleTime: 30_000,
  });

  const addMutation = useMutation({
    mutationFn: watchlistApi.add,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['watchlist'] });
      queryClient.invalidateQueries({ queryKey: ['calendar'] });
      toast.success('Company added to watchlist');
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Failed to add company');
    },
  });

  const removeMutation = useMutation({
    mutationFn: watchlistApi.remove,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['watchlist'] });
      queryClient.invalidateQueries({ queryKey: ['calendar'] });
      toast.success('Company removed from watchlist');
    },
    onError: () => toast.error('Failed to remove company'),
  });

  const watchedIds = new Set(watchlist?.companies?.map((wc) => Number(wc.company.id)) ?? []);

  return {
    watchlist,
    isLoading,
    error,
    addCompany: addMutation.mutate,
    removeCompany: removeMutation.mutate,
    isAdding: addMutation.isPending,
    isRemoving: removeMutation.isPending,
    isWatched: (id: number) => watchedIds.has(id),
  };
}
