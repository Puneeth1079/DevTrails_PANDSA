import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

// Request interceptor: attach JWT
api.interceptors.request.use(config => {
  const token = localStorage.getItem('gigshield_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
}, error => Promise.reject(error));

// Response interceptor: handle 401 → logout
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('gigshield_token');
      localStorage.removeItem('gigshield_user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;
