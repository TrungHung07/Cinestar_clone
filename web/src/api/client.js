import axios from 'axios';

/** Địa chỉ backend - đổi thành IP server khi deploy */
export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8090';

const client = axios.create({ baseURL: API_BASE_URL });

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Tự refresh token khi access token hết hạn (401), thử lại request gốc 1 lần
client.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry && !original.url.includes('/auth/')) {
      original._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const res = await axios.post(`${API_BASE_URL}/api/v1/auth/refresh`, { refreshToken });
          const { accessToken, refreshToken: newRefresh } = res.data.data;
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', newRefresh);
          original.headers.Authorization = `Bearer ${accessToken}`;
          return client(original);
        } catch {
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

/** Chuẩn hoá lỗi thành message dễ hiểu - hiện NGUYÊN NHÂN THẬT để dễ chẩn đoán */
export const errorMessage = (error) => {
  if (error?.response?.data?.message) return error.response.data.message;
  if (error?.response) return `Lỗi ${error.response.status} từ máy chủ`;
  if (error?.code === 'ERR_NETWORK' || error?.message === 'Network Error') {
    return 'Không kết nối được máy chủ — kiểm tra backend có đang chạy (docker compose ps) và URL API trong config';
  }
  return error?.message || 'Có lỗi xảy ra, vui lòng thử lại';
};

/** Ảnh lưu trên backend (poster phim, banner rạp...) - path tương đối -> URL đầy đủ */
export const imgUrl = (url) => {
  if (!url) return undefined;
  return url.startsWith('http') ? url : API_BASE_URL + url;
};

export default client;
