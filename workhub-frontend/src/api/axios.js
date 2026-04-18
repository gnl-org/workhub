import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

// Request Interceptor: Attach JWT to every outgoing request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response Interceptor: Catch 401s and handle logout
api.interceptors.response.use(
  (response) => response, // Pass through successful responses
  (error) => {
    // Check if the error is a 401 Unauthorized (standard for expired/invalid JWT)
    if (error.response && error.response.status === 401) {
      console.warn("Session expired or unauthorized. Logging out...");
      
      localStorage.removeItem('token');
      
      // Redirect to login. window.location.href is used here because 
      // this file is outside the React component tree (no access to useNavigate)
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;