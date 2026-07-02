import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';

export function useWebSocket({ isAuthenticated, onNotification }) {
  const clientRef = useRef(null);

  useEffect(() => {
    if (!isAuthenticated) return;

    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      onConnect: () => {
        console.log('[WS] Connected to STOMP broker');
        client.subscribe('/user/queue/notifications', (message) => {
          const notification = JSON.parse(message.body);
          console.log('[WS] Notification received:', notification);
          onNotification?.(notification);
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [isAuthenticated]);

  return clientRef;
}
