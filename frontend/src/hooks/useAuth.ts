import { useAuthStore } from '../store/authStore';
import { authApi } from '../api/auth';
import { LoginRequest, RegisterRequest } from '../types';
import toast from 'react-hot-toast';

export function useAuth() {
  const { user, isAuthenticated, login, logout, isAdmin } = useAuthStore();

  const handleLogin = async (data: LoginRequest) => {
    const response = await authApi.login(data);
    login(
      {
        id: response.userId,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        roles: response.roles,
      },
      response.accessToken,
      response.refreshToken
    );
    return response;
  };

  const handleRegister = async (data: RegisterRequest) => {
    const response = await authApi.register(data);
    login(
      {
        id: response.userId,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        roles: response.roles,
      },
      response.accessToken,
      response.refreshToken
    );
    return response;
  };

  const handleLogout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) await authApi.logout(refreshToken);
    } catch {
      // Ignore logout errors
    } finally {
      logout();
      toast.success('Logged out successfully');
    }
  };

  return { user, isAuthenticated, isAdmin: isAdmin(), login: handleLogin, register: handleRegister, logout: handleLogout };
}
