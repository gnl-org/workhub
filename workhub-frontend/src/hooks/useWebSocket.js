import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';

export function useWebSocket({ isAuthenticated, onNotification }) {
  const clientRef = useRef(null);

  useEffect(() => {
    if (!isAuthenticated) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const client = new Client({
      brokerURL: `${protocol}//${window.location.host}/ws`,
      onConnect: () => {
        client.subscribe('/user/queue/notifications', (message) => {
          const notification = JSON.parse(message.body);
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
