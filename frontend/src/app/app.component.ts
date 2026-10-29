import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HistoryDay} from './models/history-day.model';
import {TimeBlock} from './models/time-block.model';
import {TimerSnapshot} from './models/timer-snapshot.model';
import {SoundService} from './services/sound.service';
import {TimerApiService} from './services/timer-api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('taskInput') taskInput?: ElementRef<HTMLInputElement>;

  readonly presets = [15, 30, 45, 60, 90, 120];

  blocks: TimeBlock[] = [];
  history: HistoryDay[] = [];
  taskName = '';
  durationMinutes = 60;
  activeIndex = 0;
  paused = true;
  dark = false;
  dayFinished = false;
  draggedIndex?: number;

  private readonly timerApi = inject(TimerApiService);
  private readonly sound = inject(SoundService);
  private timerId?: number;

  ngOnInit() {
    this.dark = localStorage.getItem('cronos-theme') === 'dark';
    this.applyTheme();
    this.loadDashboard();
  }

  ngAfterViewInit() {
    this.focusTaskInput();
  }

  ngOnDestroy() {
    this.stopClock();
  }

  get totalPlannedMinutes() {
    return this.blocks.reduce((total, block) => total + block.durationMinutes, 0);
  }

  get activeBlock(): TimeBlock | undefined {
    return this.blocks[this.activeIndex];
  }

  addTask() {
    const name = this.taskName.trim();
    if (this.dayFinished || !name || this.durationMinutes < 1) {
      return;
    }

    this.timerApi.createBlock(name, this.durationMinutes).subscribe(() => {
      this.taskName = '';
      this.loadDashboard();
      this.focusTaskInput();
    });
  }

  removeTask(index: number) {
    const blockId = this.blocks[index]?.id;
    if (!blockId || this.dayFinished) {
      return;
    }

    this.timerApi.deleteBlock(blockId).subscribe(() => this.loadDashboard());
  }

  selectDuration(minutes: number) {
    this.durationMinutes = minutes;
  }

  selectNow(block: TimeBlock) {
    if (!block.id || block.completed || this.dayFinished) {
      return;
    }

    this.timerApi.activate(block.id).subscribe(snapshot => this.applySnapshot(snapshot));
  }

  finishTask(block: TimeBlock) {
    if (!block.id || block.completed || this.dayFinished) {
      return;
    }

    if (!confirm(`Finalizar “${block.name}”?`)) {
      return;
    }

    this.timerApi.finishTask(block.id).subscribe(snapshot => {
      this.applySnapshot(snapshot);
      this.loadHistory();
    });
  }

  finishDay() {
    if (this.dayFinished || !confirm('Finalizar o dia? As tarefas serão arquivadas no histórico.')) {
      return;
    }

    this.timerApi.finishDay().subscribe(snapshot => {
      this.applySnapshot(snapshot);
      this.loadHistory();
    });
  }

  toggleTimer() {
    if (!this.activeBlock || this.dayFinished) {
      return;
    }

    const request = this.paused ? this.timerApi.resume() : this.timerApi.pause();
    request.subscribe(snapshot => {
      this.applySnapshot(snapshot);

      if (snapshot.running) {
        this.sound.playResume();
      } else {
        this.sound.playPause();
      }
    });
  }

  toggleTheme() {
    this.dark = !this.dark;
    localStorage.setItem('cronos-theme', this.dark ? 'dark' : 'light');
    this.applyTheme();
  }

  onDragStart(index: number, event: DragEvent) {
    this.draggedIndex = index;
    event.dataTransfer?.setData('text/plain', String(index));

    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
    }
  }

  onDrop(index: number) {
    if (this.draggedIndex === undefined || this.draggedIndex === index || this.dayFinished) {
      return;
    }

    const [block] = this.blocks.splice(this.draggedIndex, 1);
    this.blocks.splice(index, 0, block);
    this.draggedIndex = undefined;

    const blockIds = this.blocks.flatMap(block => block.id === undefined ? [] : [block.id]);
    this.timerApi.reorder(blockIds).subscribe(snapshot => this.applySnapshot(snapshot));
  }

  formatDuration(seconds: number) {
    const hours = Math.floor(seconds / 3600).toString().padStart(2, '0');
    const minutes = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
    const remainingSeconds = (seconds % 60).toString().padStart(2, '0');

    return `${hours}:${minutes}:${remainingSeconds}`;
  }

  private loadDashboard() {
    this.timerApi.getSnapshot().subscribe(snapshot => this.applySnapshot(snapshot));
    this.loadHistory();
  }

  private loadHistory() {
    this.timerApi.getHistory().subscribe(history => this.history = history);
  }

  private applySnapshot(snapshot: TimerSnapshot) {
    this.stopClock();

    this.blocks = snapshot.blocks;
    this.dayFinished = snapshot.dayFinished;
    this.activeIndex = Math.max(0, this.blocks.findIndex(block => block.id === snapshot.activeBlockId));
    this.paused = !snapshot.running;

    if (snapshot.running && !snapshot.dayFinished) {
      this.timerId = window.setInterval(() => this.tick(), 1000);
    }
  }

  private tick() {
    const block = this.activeBlock;
    if (this.paused || !block) {
      return;
    }

    const remainingSeconds = block.remainingSeconds - 1;
    if (remainingSeconds > 0) {
      block.remainingSeconds = remainingSeconds;
      return;
    }

    block.remainingSeconds = 0;
    this.sound.playCompletion();
    this.timerApi.pause().subscribe(snapshot => this.applySnapshot(snapshot));
  }

  private stopClock() {
    this.paused = true;

    if (this.timerId) {
      clearInterval(this.timerId);
      this.timerId = undefined;
    }
  }

  private focusTaskInput() {
    setTimeout(() => this.taskInput?.nativeElement.focus());
  }

  private applyTheme() {
    document.documentElement.dataset['theme'] = this.dark ? 'dark' : 'light';
  }
}
