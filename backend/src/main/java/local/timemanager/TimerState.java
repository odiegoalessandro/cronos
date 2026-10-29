package local.timemanager;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "timer_state")
public class TimerState {
  @Id private Long id = 1L;
  private Long activeBlockId;
  private boolean running;
  private Long resumedAtEpochMs;

  protected TimerState() {}
  public Long getActiveBlockId(){return activeBlockId;} public boolean isRunning(){return running;} public Long getResumedAtEpochMs(){return resumedAtEpochMs;}
  public void setActiveBlockId(Long v){activeBlockId=v;} public void setRunning(boolean v){running=v;} public void setResumedAtEpochMs(Long v){resumedAtEpochMs=v;}
}
