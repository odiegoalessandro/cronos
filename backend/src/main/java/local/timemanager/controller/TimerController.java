package local.timemanager.controller;

import local.timemanager.service.TimerService;
import local.timemanager.dto.response.TimerSnapshotResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timer")
public class TimerController {

  private final TimerService timerService;

  public TimerController(TimerService timerService) {
    this.timerService = timerService;
  }

  @GetMapping
  public TimerSnapshotResponse snapshot() {
    return timerService.snapshot();
  }

  @PostMapping("/resume")
  public TimerSnapshotResponse resume() {
    return timerService.resume();
  }

  @PostMapping("/pause")
  public TimerSnapshotResponse pause() {
    return timerService.pause();
  }

  @PostMapping("/activate/{blockId}")
  public TimerSnapshotResponse activate(@PathVariable Long blockId) {
    return timerService.activate(blockId);
  }

  @PostMapping("/finish-task/{blockId}")
  public TimerSnapshotResponse finishTask(@PathVariable Long blockId) {
    return timerService.finishTask(blockId);
  }

  @PostMapping("/finish-day")
  public TimerSnapshotResponse finishDay() {
    return timerService.finishDay();
  }
}
