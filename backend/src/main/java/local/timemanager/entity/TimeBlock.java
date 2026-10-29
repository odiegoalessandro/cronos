package local.timemanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TimeBlock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private int durationMinutes;
  private int position;
  private int remainingSeconds;
  private int spentSeconds;
  private boolean completed;
  private String planDate;

  protected TimeBlock() {
  }

  public TimeBlock(String name, int durationMinutes, int position, String planDate) {
    this.name = name;
    this.durationMinutes = durationMinutes;
    this.position = position;
    this.planDate = planDate;
    this.remainingSeconds = durationMinutes * 60;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public int getPosition() {
    return position;
  }

  public int getRemainingSeconds() {
    return remainingSeconds;
  }

  public int getSpentSeconds() {
    return spentSeconds;
  }

  public boolean isCompleted() {
    return completed;
  }

  public String getPlanDate() {
    return planDate;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public void setRemainingSeconds(int remainingSeconds) {
    this.remainingSeconds = remainingSeconds;
  }

  public void setSpentSeconds(int spentSeconds) {
    this.spentSeconds = spentSeconds;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }
}
