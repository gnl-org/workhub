import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  withCredentials: true, // Tells Axios to include the HttpOnly cookie automatically
  withXSRFToken: true,
  // Axios will automatically read this cookie name...
  xsrfCookieName: 'XSRF-TOKEN',
  // ...and inject its text value into this HTTP header name on every mutable request
  xsrfHeaderName: 'X-XSRF-TOKEN'
});

let isRefreshing = false;
let refreshSubscribers = [];

const AUTH_ENDPOINTS_NO_REFRESH = [
  '/api/v1/auth/refresh',
  '/api/v1/auth/authenticate',
  '/api/v1/auth/register',
];

const isAuthCheckRequest = (url) => url === '/api/v1/auth/me';

const isPublicPath = () => {
  const path = window.location.pathname;
  return path === '/login' || path === '/register';
};

const redirectToLogin = () => {
  if (isPublicPath()) {
    return;
  }

  console.warn('Session expired. Redirecting to login...');
  localStorage.removeItem('user');
  window.location.href = '/login';
};

const onRefreshed = () => {
  refreshSubscribers.forEach(({ resolve }) => resolve());
  refreshSubscribers = [];
};

const onRefreshFailed = (refreshError) => {
  refreshSubscribers.forEach(({ reject }) => reject(refreshError));
  refreshSubscribers = [];
};

const waitForTokenRefresh = (originalRequest) =>
  new Promise((resolve, reject) => {
    refreshSubscribers.push({
      resolve: () => resolve(api(originalRequest)),
      reject,
    });
  });

const refreshAndRetry = async (originalRequest, { redirectOnFailure }) => {
  if (isRefreshing) {
    return waitForTokenRefresh(originalRequest);
  }

  isRefreshing = true;
  originalRequest._retry = true;

  try {
    await api.post('/api/v1/auth/refresh');
    isRefreshing = false;
    onRefreshed();
    return api(originalRequest);
  } catch (refreshError) {
    isRefreshing = false;
    onRefreshFailed(refreshError);
    if (redirectOnFailure) {
      redirectToLogin();
    }
    return Promise.reject(refreshError);
  }
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status !== 401) {
      return Promise.reject(error);
    }

    const requestUrl = originalRequest?.url ?? '';

    if (AUTH_ENDPOINTS_NO_REFRESH.includes(requestUrl)) {
      return Promise.reject(error);
    }

    // Already on login/register — no need to refresh
    if (isPublicPath()) {
      return Promise.reject(error);
    }

    // Already tried refresh once — give up
    if (originalRequest._retry) {
      if (!isAuthCheckRequest(requestUrl)) {
        redirectToLogin();
      }
      return Promise.reject(error);
    }

    // /me: try refresh silently; other APIs: redirect to login if refresh fails
    return refreshAndRetry(originalRequest, {
      redirectOnFailure: !isAuthCheckRequest(requestUrl),
    });
  }
);

export default api;
