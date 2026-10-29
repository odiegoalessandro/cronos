package local.timemanager.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
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
  public TimeBlock create(@Valid @RequestBody CreateTimeBlock request) {
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
  public TimerService.Snapshot reorder(@RequestBody ReorderTimeBlocks request) {
    return timerService.reorder(request.blockIds());
  }

  public record CreateTimeBlock(
      @NotBlank @Size(max = 100) String name,
      @Min(1) @Max(1_440) int durationMinutes) {
  }

  public record ReorderTimeBlocks(List<Long> blockIds) {
  }
}
