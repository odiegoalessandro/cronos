package local.timemanager.controller;

import java.util.List;
import local.timemanager.service.TimerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

  private final TimerService timerService;

  public HistoryController(TimerService timerService) {
    this.timerService = timerService;
  }

  @GetMapping
  public List<TimerService.HistoryDay> list() {
    return timerService.history();
  }
}
