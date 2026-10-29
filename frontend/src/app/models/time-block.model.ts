export interface TimeBlock {
  id?: number;
  name: string;
  durationMinutes: number;
  position: number;
  remainingSeconds: number;
  completed: boolean;
}
