/**
 * Decodes the JWT from localStorage and returns user details.
 * Returns null if no token exists or if the token is malformed.
 */
export const getUserInfo = () => {
  const token = localStorage.getItem('token');
  
  if (!token) return null;

  try {
    // 1. Split the token (Header.Payload.Signature)
    const base64Url = token.split('.')[1];
    
    // 2. Fix Base64 encoding for browser compatibility
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    
    // 3. Decode Base64 string to JSON string
    const jsonPayload = decodeURIComponent(
      window.atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    // 4. Parse into a JS Object
    const decoded = JSON.parse(jsonPayload);
    
    return {
      email: decoded.sub,
      role: decoded.role,     // Matches your .claim("role", ...) in Java
      fullName: decoded.fullName,
      expiry: decoded.exp
    };
  } catch (error) {
    console.error("Token decoding failed:", error);
    return null;
  }
};