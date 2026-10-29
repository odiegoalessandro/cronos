package local.timemanager;

import jakarta.persistence.*;

@Entity
public class TimeBlock {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private int durationMinutes;
  private int position;
  private int remainingSeconds;
  private int spentSeconds;
  private boolean completed;
  private String planDate;

  protected TimeBlock() {}
  public TimeBlock(String name, int durationMinutes, int position, String planDate) { this.name=name; this.durationMinutes=durationMinutes; this.position=position; this.planDate=planDate; this.remainingSeconds=durationMinutes * 60; }
  public Long getId(){return id;} public String getName(){return name;} public int getDurationMinutes(){return durationMinutes;} public int getPosition(){return position;} public int getRemainingSeconds(){return remainingSeconds;} public int getSpentSeconds(){return spentSeconds;} public boolean isCompleted(){return completed;} public String getPlanDate(){return planDate;}
  public void setName(String v){name=v;} public void setDurationMinutes(int v){durationMinutes=v;} public void setPosition(int v){position=v;}
  public void setRemainingSeconds(int v){remainingSeconds=v;} public void setSpentSeconds(int v){spentSeconds=v;} public void setCompleted(boolean v){completed=v;} public void setPlanDate(String v){planDate=v;}
}
