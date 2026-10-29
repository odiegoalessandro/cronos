package local.timemanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DayArchive {

  @Id
  private String planDate;

  private long finishedAtEpochMs;

  protected DayArchive() {
  }

  public DayArchive(String planDate) {
    this.planDate = planDate;
    this.finishedAtEpochMs = System.currentTimeMillis();
  }

  public String getPlanDate() {
    return planDate;
  }

  public long getFinishedAtEpochMs() {
    return finishedAtEpochMs;
  }
}
