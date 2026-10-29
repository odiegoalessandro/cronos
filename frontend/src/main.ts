import {AfterViewInit, Component, ElementRef, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClient, provideHttpClient} from '@angular/common/http';
import {bootstrapApplication} from '@angular/platform-browser';

type Block = { id?: number; name: string; durationMinutes: number; position: number; remainingSeconds: number; completed: boolean };
type TimerSnapshot = { blocks: Block[]; activeBlockId: number | null; running: boolean };

@Component({selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './app.html'})
class App implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('taskInput') taskInput?: ElementRef<HTMLInputElement>;
  readonly http = inject(HttpClient); readonly presets = [15, 30, 45, 60, 90, 120];
  blocks: Block[] = []; name = ''; duration = 60; active = 0; paused = true; dark = false;
  private timer?: number; private audio?: AudioContext;
  ngOnInit() { this.dark = localStorage.getItem('cronos-theme') === 'dark'; this.applyTheme(); this.load(); }
  ngAfterViewInit() { this.focusTaskInput(); }
  ngOnDestroy() { this.stopClock(); }
  load() { this.http.get<TimerSnapshot>('/api/timer').subscribe(snapshot => this.applySnapshot(snapshot)); }
  get total() { return this.blocks.reduce((sum, block) => sum + block.durationMinutes, 0); }
  get activeBlock(): Block | undefined { return this.blocks[this.active]; }
  add() { if (!this.name.trim() || this.duration < 1) return; this.http.post<Block>('/api/blocks', {name: this.name.trim(), durationMinutes: this.duration}).subscribe(() => { this.name = ''; this.load(); this.focusTaskInput(); }); }
  remove(index: number) { const block = this.blocks[index]; if (!block?.id) return; this.http.delete(`/api/blocks/${block.id}`).subscribe(() => this.load()); }
  selectDuration(minutes: number) { this.duration = minutes; }
  toggle() { if (!this.activeBlock) return; const endpoint = this.paused ? '/api/timer/resume' : '/api/timer/pause'; this.http.post<TimerSnapshot>(endpoint, {}).subscribe(snapshot => { this.applySnapshot(snapshot); this.playTone(this.paused ? 420 : 660, 0.09); }); }
  toggleTheme() { this.dark = !this.dark; localStorage.setItem('cronos-theme', this.dark ? 'dark' : 'light'); this.applyTheme(); }
  private tick() { if (this.paused || !this.activeBlock) return; const remaining = this.activeBlock.remainingSeconds - 1; if (remaining > 0) { this.activeBlock.remainingSeconds = remaining; return; } this.activeBlock.remainingSeconds = 0; this.playCompletionSound(); this.http.post<TimerSnapshot>('/api/timer/pause', {}).subscribe(snapshot => this.applySnapshot(snapshot)); }
  private stopClock() { this.paused = true; if (this.timer) { clearInterval(this.timer); this.timer = undefined; } }
  private applySnapshot(snapshot: TimerSnapshot) { this.stopClock(); this.blocks = snapshot.blocks; const index = this.blocks.findIndex(block => block.id === snapshot.activeBlockId); this.active = index >= 0 ? index : 0; this.paused = !snapshot.running; if (snapshot.running) this.timer = window.setInterval(() => this.tick(), 1000); }
  private focusTaskInput() { setTimeout(() => this.taskInput?.nativeElement.focus()); }
  private applyTheme() { document.documentElement.dataset['theme'] = this.dark ? 'dark' : 'light'; }
  private playCompletionSound() { this.playTone(740, 0.11); window.setTimeout(() => this.playTone(988, 0.16), 130); }
  private playTone(frequency: number, duration: number) { try { this.audio ??= new AudioContext(); const oscillator = this.audio.createOscillator(); const gain = this.audio.createGain(); oscillator.frequency.value = frequency; oscillator.type = 'sine'; gain.gain.setValueAtTime(0.0001, this.audio.currentTime); gain.gain.exponentialRampToValueAtTime(0.08, this.audio.currentTime + 0.01); gain.gain.exponentialRampToValueAtTime(0.0001, this.audio.currentTime + duration); oscillator.connect(gain).connect(this.audio.destination); oscillator.start(); oscillator.stop(this.audio.currentTime + duration); } catch { /* Browser audio unavailable. */ } }
  fmt(seconds: number) { return `${Math.floor(seconds / 3600).toString().padStart(2, '0')}:${Math.floor(seconds % 3600 / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`; }
}
bootstrapApplication(App, {providers: [provideHttpClient()]});
