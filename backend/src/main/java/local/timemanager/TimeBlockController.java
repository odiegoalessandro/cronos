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
  private final TimeBlockRepository repo;
  public TimeBlockController(TimeBlockRepository repo){this.repo=repo;}
  @GetMapping public List<TimeBlock> all(){return repo.findAll().stream().sorted(Comparator.comparing(TimeBlock::getPosition)).toList();}
  @PostMapping public TimeBlock add(@Valid @RequestBody CreateTimeBlock body){
    String name=body.name().trim();
    int minutes=body.durationMinutes();
    if(repo.findAll().stream().mapToInt(TimeBlock::getDurationMinutes).sum()+minutes>1440) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"O limite diário de 24 horas foi excedido");
    return repo.save(new TimeBlock(name, minutes, (int)repo.count()));
  }
  @DeleteMapping("/{id}") public void delete(@PathVariable Long id){repo.deleteById(id);}

  record CreateTimeBlock(@NotBlank @jakarta.validation.constraints.Size(max = 100) String name, @Min(1) @Max(1440) int durationMinutes) {}
}
