package local.timemanager;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/blocks") @CrossOrigin(origins="*")
public class TimeBlockController {
  private final TimeBlockRepository repo;
  public TimeBlockController(TimeBlockRepository repo){this.repo=repo;}
  @GetMapping public List<TimeBlock> all(){return repo.findAll().stream().sorted(Comparator.comparing(TimeBlock::getPosition)).toList();}
  @PostMapping public TimeBlock add(@RequestBody Map<String,Object> body){
    String name=String.valueOf(body.getOrDefault("name", "Sem nome")).trim();
    int minutes=((Number)body.getOrDefault("durationMinutes", 0)).intValue();
    if(name.isBlank() || minutes<1 || minutes>1440 || repo.findAll().stream().mapToInt(TimeBlock::getDurationMinutes).sum()+minutes>1440) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Duração inválida ou limite diário excedido");
    return repo.save(new TimeBlock(name, minutes, (int)repo.count()));
  }
  @DeleteMapping("/{id}") public void delete(@PathVariable Long id){repo.deleteById(id);}
}
