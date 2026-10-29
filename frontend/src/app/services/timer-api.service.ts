import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {HistoryDay} from '../models/history-day.model';
import {TimeBlock} from '../models/time-block.model';
import {TimerSnapshot} from '../models/timer-snapshot.model';

@Injectable({providedIn: 'root'})
export class TimerApiService {
  private readonly http = inject(HttpClient);

  getSnapshot() {
    return this.http.get<TimerSnapshot>('/api/timer');
  }

  getHistory() {
    return this.http.get<HistoryDay[]>('/api/history');
  }

  createBlock(name: string, durationMinutes: number) {
    return this.http.post<TimeBlock>('/api/blocks', {name, durationMinutes});
  }

  deleteBlock(blockId: number) {
    return this.http.delete(`/api/blocks/${blockId}`);
  }

  reorder(blockIds: number[]) {
    return this.http.put<TimerSnapshot>('/api/blocks/order', {blockIds});
  }

  resume() {
    return this.http.post<TimerSnapshot>('/api/timer/resume', {});
  }

  pause() {
    return this.http.post<TimerSnapshot>('/api/timer/pause', {});
  }

  activate(blockId: number) {
    return this.http.post<TimerSnapshot>(`/api/timer/activate/${blockId}`, {});
  }

  finishTask(blockId: number) {
    return this.http.post<TimerSnapshot>(`/api/timer/finish-task/${blockId}`, {});
  }

  finishDay() {
    return this.http.post<TimerSnapshot>('/api/timer/finish-day', {});
  }
}
