import { Injectable, NgZone } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { BehaviorSubject } from 'rxjs';
import { SessionAlert } from '../models';
import SockJS from 'sockjs-client';

@Injectable({ providedIn: 'root' })
export class WebsocketService {
  private stomp = new RxStomp();
  alerts$ = new BehaviorSubject<SessionAlert[]>([]);

  constructor(private zone: NgZone) {}

  connect() {
    this.stomp.configure({
      webSocketFactory: () => new SockJS('/ws') as WebSocket,
      reconnectDelay: 5000,
    });
    this.stomp.activate();

    this.stomp.watch('/topic/alerts').subscribe(msg => {
      const alert: SessionAlert = JSON.parse(msg.body);
      this.zone.run(() => {
        this.alerts$.next([...this.alerts$.value, alert]);
      });
    });
  }

  dismissFirst() {
    this.alerts$.next(this.alerts$.value.slice(1));
  }
}
