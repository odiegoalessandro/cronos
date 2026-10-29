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
  private boolean completed;

  protected TimeBlock() {}
  public TimeBlock(String name, int durationMinutes, int position) { this.name=name; this.durationMinutes=durationMinutes; this.position=position; this.remainingSeconds=durationMinutes * 60; }
  public Long getId(){return id;} public String getName(){return name;} public int getDurationMinutes(){return durationMinutes;} public int getPosition(){return position;} public int getRemainingSeconds(){return remainingSeconds;} public boolean isCompleted(){return completed;}
  public void setName(String v){name=v;} public void setDurationMinutes(int v){durationMinutes=v;} public void setPosition(int v){position=v;}
  public void setRemainingSeconds(int v){remainingSeconds=v;} public void setCompleted(boolean v){completed=v;}
}
