import axios from 'axios';

const notificationsApi = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

export default notificationsApi;
