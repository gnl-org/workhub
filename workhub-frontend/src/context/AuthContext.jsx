import React, { createContext, useContext, useState, useEffect, useMemo } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null); // Holds { email, role, fullName }
  const [loading, setLoading] = useState(true);

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

  // Compute authenticated status implicitly based on whether a user object exists
  const isAuthenticated = !!user;

  // Memoize the context value to prevent unnecessary re-renders of consumers
  const value = useMemo(
    () => ({ user, setUser, isAuthenticated, loading }),
    [user, isAuthenticated, loading]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);