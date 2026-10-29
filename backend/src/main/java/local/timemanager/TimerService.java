package local.timemanager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
public class TimerService {
  private final TimeBlockRepository blocks;
  private final TimerStateRepository states;

  public TimerService(TimeBlockRepository blocks, TimerStateRepository states) { this.blocks = blocks; this.states = states; }

  @Transactional
  public Snapshot snapshot() {
    var all = orderedBlocks();
    var state = state();
    normalizeLegacyBlocks(all);
    synchronize(state, all);
    ensureActiveBlock(state, all);
    blocks.saveAll(all);
    states.save(state);
    return new Snapshot(all, state.getActiveBlockId(), state.isRunning());
  }

  @Transactional
  public Snapshot resume() {
    snapshot();
    var state = state();
    if (state.getActiveBlockId() != null) {
      state.setRunning(true);
      state.setResumedAtEpochMs(System.currentTimeMillis());
      states.save(state);
    }
    return snapshot();
  }

  @Transactional
  public Snapshot pause() {
    snapshot();
    var state = state();
    state.setRunning(false);
    state.setResumedAtEpochMs(null);
    states.save(state);
    return snapshot();
  }

  @Transactional
  public void delete(Long blockId) {
    var state = state();
    if (blockId.equals(state.getActiveBlockId())) {
      state.setActiveBlockId(null);
      state.setRunning(false);
      state.setResumedAtEpochMs(null);
      states.save(state);
    }
    blocks.deleteById(blockId);
  }

  private List<TimeBlock> orderedBlocks() { return blocks.findAll().stream().sorted(Comparator.comparing(TimeBlock::getPosition)).toList(); }
  private TimerState state() { return states.findById(1L).orElseGet(() -> states.save(new TimerState())); }
  private void normalizeLegacyBlocks(List<TimeBlock> all) { all.stream().filter(block -> !block.isCompleted() && block.getRemainingSeconds() == 0).forEach(block -> block.setRemainingSeconds(block.getDurationMinutes() * 60)); }
  private void ensureActiveBlock(TimerState state, List<TimeBlock> all) {
    boolean isValid = all.stream().anyMatch(block -> block.getId().equals(state.getActiveBlockId()) && !block.isCompleted());
    if (!isValid) state.setActiveBlockId(all.stream().filter(block -> !block.isCompleted()).map(TimeBlock::getId).findFirst().orElse(null));
  }
  private void synchronize(TimerState state, List<TimeBlock> all) {
    if (!state.isRunning() || state.getActiveBlockId() == null || state.getResumedAtEpochMs() == null) return;
    var active = all.stream().filter(block -> block.getId().equals(state.getActiveBlockId())).findFirst().orElse(null);
    if (active == null) { state.setRunning(false); state.setResumedAtEpochMs(null); return; }
    int elapsedSeconds = (int) ((System.currentTimeMillis() - state.getResumedAtEpochMs()) / 1000);
    if (elapsedSeconds <= 0) return;
    int remaining = active.getRemainingSeconds() - elapsedSeconds;
    if (remaining > 0) { active.setRemainingSeconds(remaining); state.setResumedAtEpochMs(state.getResumedAtEpochMs() + elapsedSeconds * 1000L); return; }
    active.setRemainingSeconds(0);
    active.setCompleted(true);
    state.setRunning(false);
    state.setResumedAtEpochMs(null);
    state.setActiveBlockId(all.stream().filter(block -> !block.isCompleted()).map(TimeBlock::getId).findFirst().orElse(null));
  }

  public record Snapshot(List<TimeBlock> blocks, Long activeBlockId, boolean running) {}
}
