package local.timemanager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
public class TimerService {
  private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
  private final TimeBlockRepository blocks;
  private final TimerStateRepository states;
  private final DayArchiveRepository archives;

  public TimerService(TimeBlockRepository blocks, TimerStateRepository states, DayArchiveRepository archives) { this.blocks = blocks; this.states = states; this.archives = archives; }
  public String today() { return LocalDate.now(ZONE).toString(); }
  public boolean isDayFinished() { return archives.existsById(today()); }

  @Transactional public Snapshot snapshot() {
    var all = orderedBlocks(); var state = state(); normalizeLegacyBlocks(all); synchronize(state, all); ensureActiveBlock(state, all);
    blocks.saveAll(all); states.save(state); return new Snapshot(all, state.getActiveBlockId(), state.isRunning(), today(), isDayFinished());
  }
  @Transactional public Snapshot resume() { if (isDayFinished()) return snapshot(); snapshot(); var state = state(); if (state.getActiveBlockId() != null) { state.setRunning(true); state.setResumedAtEpochMs(System.currentTimeMillis()); states.save(state); } return snapshot(); }
  @Transactional public Snapshot pause() { snapshot(); var state = state(); state.setRunning(false); state.setResumedAtEpochMs(null); states.save(state); return snapshot(); }
  @Transactional public Snapshot activate(Long blockId) { if (isDayFinished()) return snapshot(); snapshot(); var block = blocks.findById(blockId).orElseThrow(); if (!today().equals(block.getPlanDate()) || block.isCompleted()) throw new IllegalArgumentException("Tarefa indisponível"); var state = state(); state.setActiveBlockId(blockId); state.setRunning(false); state.setResumedAtEpochMs(null); states.save(state); return snapshot(); }
  @Transactional public Snapshot finishTask(Long blockId) { snapshot(); var block = blocks.findById(blockId).orElseThrow(); if (!today().equals(block.getPlanDate())) throw new IllegalArgumentException("Tarefa indisponível"); block.setRemainingSeconds(0); block.setCompleted(true); blocks.save(block); var state = state(); if (blockId.equals(state.getActiveBlockId())) { state.setRunning(false); state.setResumedAtEpochMs(null); state.setActiveBlockId(null); states.save(state); } return snapshot(); }
  @Transactional public Snapshot finishDay() { snapshot(); var state = state(); state.setRunning(false); state.setResumedAtEpochMs(null); state.setActiveBlockId(null); states.save(state); archives.save(new DayArchive(today())); return snapshot(); }
  @Transactional public Snapshot reorder(List<Long> blockIds) { if (isDayFinished()) return snapshot(); var all = orderedBlocks(); var existingIds = all.stream().map(TimeBlock::getId).collect(java.util.stream.Collectors.toSet()); if (all.size() != blockIds.size() || !existingIds.equals(java.util.Set.copyOf(blockIds))) throw new IllegalArgumentException("A lista de tarefas é inválida"); var byId = all.stream().collect(java.util.stream.Collectors.toMap(TimeBlock::getId, block -> block)); for (int position = 0; position < blockIds.size(); position++) byId.get(blockIds.get(position)).setPosition(position); blocks.saveAll(all); return snapshot(); }
  @Transactional public void delete(Long blockId) { if (isDayFinished()) return; var state = state(); if (blockId.equals(state.getActiveBlockId())) { state.setActiveBlockId(null); state.setRunning(false); state.setResumedAtEpochMs(null); states.save(state); } blocks.findById(blockId).filter(block -> today().equals(block.getPlanDate())).ifPresent(blocks::delete); }
  @Transactional public List<HistoryDay> history() { snapshot(); return blocks.findAll().stream().collect(java.util.stream.Collectors.groupingBy(TimeBlock::getPlanDate)).entrySet().stream().map(entry -> { var day = entry.getValue(); return new HistoryDay(entry.getKey(), day.stream().mapToInt(TimeBlock::getDurationMinutes).sum(), day.stream().mapToInt(TimeBlock::getSpentSeconds).sum() / 60, (int) day.stream().filter(TimeBlock::isCompleted).count(), day.size(), archives.existsById(entry.getKey())); }).sorted(Comparator.comparing(HistoryDay::date).reversed()).toList(); }
  @Transactional public TimeBlock add(String name, int minutes) { if (isDayFinished()) throw new IllegalStateException("O dia já foi finalizado"); var all = orderedBlocks(); if (all.stream().mapToInt(TimeBlock::getDurationMinutes).sum() + minutes > 1440) throw new IllegalArgumentException("O limite diário de 24 horas foi excedido"); return blocks.save(new TimeBlock(name, minutes, all.size(), today())); }

  private List<TimeBlock> orderedBlocks() { return blocks.findAll().stream().filter(block -> today().equals(block.getPlanDate())).sorted(Comparator.comparing(TimeBlock::getPosition)).toList(); }
  private TimerState state() { return states.findById(1L).orElseGet(() -> states.save(new TimerState())); }
  private void normalizeLegacyBlocks(List<TimeBlock> all) { all.stream().filter(block -> !block.isCompleted() && block.getRemainingSeconds() == 0).forEach(block -> block.setRemainingSeconds(block.getDurationMinutes() * 60)); all.forEach(block -> { if (block.getSpentSeconds() == 0 && block.getRemainingSeconds() < block.getDurationMinutes() * 60) block.setSpentSeconds(block.getDurationMinutes() * 60 - block.getRemainingSeconds()); }); }
  private void ensureActiveBlock(TimerState state, List<TimeBlock> all) { boolean valid = all.stream().anyMatch(block -> block.getId().equals(state.getActiveBlockId()) && !block.isCompleted()); if (!valid) { state.setActiveBlockId(all.stream().filter(block -> !block.isCompleted()).map(TimeBlock::getId).findFirst().orElse(null)); state.setRunning(false); state.setResumedAtEpochMs(null); } }
  private void synchronize(TimerState state, List<TimeBlock> all) { if (!state.isRunning() || state.getActiveBlockId() == null || state.getResumedAtEpochMs() == null) return; var active = all.stream().filter(block -> block.getId().equals(state.getActiveBlockId())).findFirst().orElse(null); if (active == null) { state.setRunning(false); state.setResumedAtEpochMs(null); return; } int elapsed = (int) ((System.currentTimeMillis() - state.getResumedAtEpochMs()) / 1000); if (elapsed <= 0) return; int consumed = Math.min(elapsed, active.getRemainingSeconds()); active.setRemainingSeconds(active.getRemainingSeconds() - consumed); active.setSpentSeconds(active.getSpentSeconds() + consumed); if (active.getRemainingSeconds() > 0) { state.setResumedAtEpochMs(state.getResumedAtEpochMs() + elapsed * 1000L); return; } active.setCompleted(true); state.setRunning(false); state.setResumedAtEpochMs(null); state.setActiveBlockId(all.stream().filter(block -> !block.isCompleted()).map(TimeBlock::getId).findFirst().orElse(null)); }
  public record Snapshot(List<TimeBlock> blocks, Long activeBlockId, boolean running, String date, boolean dayFinished) {}
  public record HistoryDay(String date, int plannedMinutes, int spentMinutes, int completedTasks, int totalTasks, boolean finished) {}
}
