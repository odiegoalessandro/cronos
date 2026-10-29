import {Injectable} from '@angular/core';

@Injectable({providedIn: 'root'})
export class SoundService {
  private audioContext?: AudioContext;

  playResume() {
    this.playTone(660, 0.09);
  }

  playPause() {
    this.playTone(420, 0.09);
  }

  playCompletion() {
    this.playTone(740, 0.11);
    window.setTimeout(() => this.playTone(988, 0.16), 130);
  }

  private playTone(frequency: number, duration: number) {
    try {
      this.audioContext ??= new AudioContext();

      const oscillator = this.audioContext.createOscillator();
      const gain = this.audioContext.createGain();
      const currentTime = this.audioContext.currentTime;

      oscillator.frequency.value = frequency;
      oscillator.type = 'sine';
      gain.gain.setValueAtTime(0.0001, currentTime);
      gain.gain.exponentialRampToValueAtTime(0.08, currentTime + 0.01);
      gain.gain.exponentialRampToValueAtTime(0.0001, currentTime + duration);

      oscillator.connect(gain).connect(this.audioContext.destination);
      oscillator.start();
      oscillator.stop(currentTime + duration);
    } catch {
      // Audio is optional and may be blocked by the browser.
    }
  }
}
