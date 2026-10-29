import {TimeBlock} from './time-block.model';

export interface TimerSnapshot {
  blocks: TimeBlock[];
  activeBlockId: number | null;
  running: boolean;
  date: string;
  dayFinished: boolean;
}
