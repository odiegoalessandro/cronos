package local.timemanager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
  private final TimerService timer;
  public HistoryController(TimerService timer) { this.timer = timer; }
  @GetMapping public List<TimerService.HistoryDay> all() { return timer.history(); }
}
