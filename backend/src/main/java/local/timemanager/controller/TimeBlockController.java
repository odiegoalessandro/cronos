package local.timemanager.controller;

import jakarta.validation.Valid;
import java.util.List;
import local.timemanager.dto.request.CreateTimeBlockRequest;
import local.timemanager.dto.request.ReorderTimeBlocksRequest;
import local.timemanager.dto.response.TimerSnapshotResponse;
import local.timemanager.entity.TimeBlock;
import local.timemanager.service.TimerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/blocks")
public class TimeBlockController {

  private final TimerService timerService;

  public TimeBlockController(TimerService timerService) {
    this.timerService = timerService;
  }

  @GetMapping
  public List<TimeBlock> list() {
    return timerService.snapshot().blocks();
  }

  @PostMapping
  public TimeBlock create(@Valid @RequestBody CreateTimeBlockRequest request) {
    try {
      return timerService.add(request.name().trim(), request.durationMinutes());
    } catch (IllegalArgumentException | IllegalStateException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
  }

  @DeleteMapping("/{blockId}")
  public void delete(@PathVariable Long blockId) {
    timerService.delete(blockId);
  }

  @PutMapping("/order")
  public TimerSnapshotResponse reorder(@RequestBody ReorderTimeBlocksRequest request) {
    return timerService.reorder(request.blockIds());
  }

}
