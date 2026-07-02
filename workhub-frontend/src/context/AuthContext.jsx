import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import api from '../api/axios';
import { useWebSocket } from '../hooks/useWebSocket';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null); // Holds { email, role, fullName }
  const [loading, setLoading] = useState(true);
  const [wsNotification, setWsNotification] = useState(null);

  const checkAuthStatus = async () => {
    try {
      const response = await api.get('/api/v1/auth/me'); 
      setUser(response.data); 
    } catch (error) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const handleNotification = useCallback((notification) => {
    setWsNotification(notification);
  }, []);

  // Compute authenticated status implicitly based on whether a user object exists
  const isAuthenticated = !!user;

  useWebSocket({ isAuthenticated, onNotification: handleNotification });

  const logout = async () => {
    try {
      await api.post('/api/v1/auth/logout');
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      // Always clear user state even if logout endpoint fails
      setUser(null);
    }
  };

  // Memoize the context value to prevent unnecessary re-renders of consumers
  const value = useMemo(
    () => ({ user, setUser, isAuthenticated, loading, logout, wsNotification, setWsNotification }),
    [user, isAuthenticated, loading, wsNotification]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);