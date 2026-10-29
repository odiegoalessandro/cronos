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
}
