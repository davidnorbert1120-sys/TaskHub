import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import { TaskItemModel } from '../model/task-item.model';

export interface TaskEventModel {
  type: 'STATUS_CHANGED' | 'CREATED' | 'UPDATED' | 'DELETED';
  task: TaskItemModel;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService {

  private client: Client | null = null;
  private connected = false;

  connect(): void {
    if (this.connected) {
      console.log('WebSocketService: already connected');
    } else {
      console.log('WebSocketService: connecting to ws://localhost:8080/ws');
      this.client = new Client({
        brokerURL: 'ws://localhost:8080/ws',
        reconnectDelay: 5000,
        debug: () => {}
      });

      this.client.onConnect = () => {
        console.log('WebSocketService: connected');
        this.connected = true;
      };

      this.client.onStompError = (frame) => {
        console.error('WebSocketService: STOMP error', frame.headers['message']);
      };

      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.client !== null) {
      console.log('WebSocketService: disconnecting');
      this.client.deactivate();
      this.client = null;
      this.connected = false;
    }
  }

  subscribeToProjectTasks(projectId: number): Observable<TaskEventModel> {
    const subject = new Subject<TaskEventModel>();
    const destination = `/topic/projects/${projectId}/tasks`;

    const trySubscribe = () => {
      if (this.client !== null && this.client.connected) {
        console.log('WebSocketService: subscribing to', destination);
        this.client.subscribe(destination, (message: IMessage) => {
          const event: TaskEventModel = JSON.parse(message.body);
          console.log('WebSocketService: received event', event.type, 'for task', event.task.id);
          subject.next(event);
        });
      } else {
        setTimeout(trySubscribe, 200);
      }
    };

    trySubscribe();
    return subject.asObservable();
  }
}
