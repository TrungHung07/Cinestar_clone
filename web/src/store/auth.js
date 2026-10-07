import { create } from 'zustand';

/** Store đăng nhập: token + thông tin user, lưu localStorage */
export const useAuth = create((set) => ({
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  user: JSON.parse(localStorage.getItem('user') || 'null'),

  login: ({ accessToken, refreshToken, user }) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(user));
    set({ accessToken, refreshToken, user });
  },

  logout: () => {
    localStorage.clear();
    set({ accessToken: null, refreshToken: null, user: null });
  },
}));

export const isLoggedIn = () => !!localStorage.getItem('accessToken');
export const isAdmin = () => {
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  return !!user?.roles?.includes('ROLE_ADMIN');
};
