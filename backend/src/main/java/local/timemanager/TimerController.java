package local.timemanager;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timer")
public class TimerController {
  private final TimerService timer;
  public TimerController(TimerService timer) { this.timer = timer; }
  @GetMapping public TimerService.Snapshot snapshot() { return timer.snapshot(); }
  @PostMapping("/resume") public TimerService.Snapshot resume() { return timer.resume(); }
  @PostMapping("/pause") public TimerService.Snapshot pause() { return timer.pause(); }
  @PostMapping("/activate/{blockId}") public TimerService.Snapshot activate(@PathVariable Long blockId) { return timer.activate(blockId); }
  @PostMapping("/finish-task/{blockId}") public TimerService.Snapshot finishTask(@PathVariable Long blockId) { return timer.finishTask(blockId); }
  @PostMapping("/finish-day") public TimerService.Snapshot finishDay() { return timer.finishDay(); }
}
