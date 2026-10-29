package local.timemanager;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/blocks")
public class TimeBlockController {
  private final TimerService timer;
  public TimeBlockController(TimerService timer){this.timer=timer;}
  @GetMapping public List<TimeBlock> all(){return timer.snapshot().blocks();}
  @PostMapping public TimeBlock add(@Valid @RequestBody CreateTimeBlock body){
    String name=body.name().trim();
    int minutes=body.durationMinutes();
    try { return timer.add(name, minutes); } catch (IllegalArgumentException | IllegalStateException error) { throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, error.getMessage()); }
  }
  @DeleteMapping("/{id}") public void delete(@PathVariable Long id){timer.delete(id);}
  @PutMapping("/order") public TimerService.Snapshot reorder(@RequestBody ReorderTimeBlocks body){return timer.reorder(body.blockIds());}

  record CreateTimeBlock(@NotBlank @jakarta.validation.constraints.Size(max = 100) String name, @Min(1) @Max(1440) int durationMinutes) {}
  record ReorderTimeBlocks(List<Long> blockIds) {}
}
