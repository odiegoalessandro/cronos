package local.timemanager.controller;

import local.timemanager.service.TimerService;
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
  public TimerService.Snapshot snapshot() {
    return timerService.snapshot();
  }

  @PostMapping("/resume")
  public TimerService.Snapshot resume() {
    return timerService.resume();
  }

  @PostMapping("/pause")
  public TimerService.Snapshot pause() {
    return timerService.pause();
  }

  @PostMapping("/activate/{blockId}")
  public TimerService.Snapshot activate(@PathVariable Long blockId) {
    return timerService.activate(blockId);
  }

  @PostMapping("/finish-task/{blockId}")
  public TimerService.Snapshot finishTask(@PathVariable Long blockId) {
    return timerService.finishTask(blockId);
  }

  @PostMapping("/finish-day")
  public TimerService.Snapshot finishDay() {
    return timerService.finishDay();
  }
}
