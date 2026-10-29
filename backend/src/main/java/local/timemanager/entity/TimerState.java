package local.timemanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "timer_state")
public class TimerState {

  @Id
  private Long id = 1L;

  private Long activeBlockId;
  private boolean running;
  private Long resumedAtEpochMs;

  public TimerState() {
  }

  public Long getActiveBlockId() {
    return activeBlockId;
  }

  public boolean isRunning() {
    return running;
  }

  public Long getResumedAtEpochMs() {
    return resumedAtEpochMs;
  }

  public void setActiveBlockId(Long activeBlockId) {
    this.activeBlockId = activeBlockId;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  public void setResumedAtEpochMs(Long resumedAtEpochMs) {
    this.resumedAtEpochMs = resumedAtEpochMs;
  }
}
