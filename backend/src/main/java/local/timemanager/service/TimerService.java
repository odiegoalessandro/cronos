package local.timemanager.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import local.timemanager.dto.response.HistoryDayResponse;
import local.timemanager.dto.response.TimerSnapshotResponse;
import local.timemanager.entity.DayArchive;
import local.timemanager.entity.TimeBlock;
import local.timemanager.entity.TimerState;
import local.timemanager.repository.DayArchiveRepository;
import local.timemanager.repository.TimeBlockRepository;
import local.timemanager.repository.TimerStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimerService {

  private static final ZoneId APPLICATION_ZONE = ZoneId.of("America/Sao_Paulo");
  private static final long TIMER_STATE_ID = 1L;

  private final TimeBlockRepository timeBlocks;
  private final TimerStateRepository timerStates;
  private final DayArchiveRepository dayArchives;

  public TimerService(
      TimeBlockRepository timeBlocks,
      TimerStateRepository timerStates,
      DayArchiveRepository dayArchives) {
    this.timeBlocks = timeBlocks;
    this.timerStates = timerStates;
    this.dayArchives = dayArchives;
  }

  @Transactional
  public TimerSnapshotResponse snapshot() {
    List<TimeBlock> blocks = currentDayBlocks();
    TimerState state = timerState();

    normalizeLegacyBlocks(blocks);
    synchronizeElapsedTime(state, blocks);
    ensureActiveBlock(state, blocks);

    timeBlocks.saveAll(blocks);
    timerStates.save(state);

    return toSnapshot(blocks, state);
  }

  @Transactional
  public TimerSnapshotResponse resume() {
    if (isCurrentDayFinished()) {
      return snapshot();
    }

    snapshot();
    TimerState state = timerState();

    if (state.getActiveBlockId() != null) {
      state.setRunning(true);
      state.setResumedAtEpochMs(System.currentTimeMillis());
      timerStates.save(state);
    }

    return snapshot();
  }

  @Transactional
  public TimerSnapshotResponse pause() {
    snapshot();
    pause(timerState());
    return snapshot();
  }

  @Transactional
  public TimerSnapshotResponse activate(Long blockId) {
    requireOpenDay();
    snapshot();

    TimeBlock block = findCurrentDayBlock(blockId);
    if (block.isCompleted()) {
      throw new IllegalArgumentException("Tarefa concluída não pode ser reativada");
    }

    TimerState state = timerState();
    state.setActiveBlockId(blockId);
    pause(state);

    return snapshot();
  }

  @Transactional
  public TimerSnapshotResponse finishTask(Long blockId) {
    snapshot();

    TimeBlock block = findCurrentDayBlock(blockId);
    block.setRemainingSeconds(0);
    block.setCompleted(true);
    timeBlocks.save(block);

    TimerState state = timerState();
    if (blockId.equals(state.getActiveBlockId())) {
      state.setActiveBlockId(null);
      pause(state);
    }

    return snapshot();
  }

  @Transactional
  public TimerSnapshotResponse finishDay() {
    snapshot();

    TimerState state = timerState();
    state.setActiveBlockId(null);
    pause(state);
    dayArchives.save(new DayArchive(currentDate()));

    return snapshot();
  }

  @Transactional
  public TimerSnapshotResponse reorder(List<Long> blockIds) {
    requireOpenDay();

    List<TimeBlock> blocks = currentDayBlocks();
    validateBlockIds(blocks, blockIds);

    Map<Long, TimeBlock> blocksById = blocks.stream()
        .collect(Collectors.toMap(TimeBlock::getId, block -> block));

    for (int position = 0; position < blockIds.size(); position++) {
      blocksById.get(blockIds.get(position)).setPosition(position);
    }

    timeBlocks.saveAll(blocks);
    return snapshot();
  }

  @Transactional
  public TimeBlock add(String name, int durationMinutes) {
    requireOpenDay();

    List<TimeBlock> blocks = currentDayBlocks();
    int totalMinutes = blocks.stream().mapToInt(TimeBlock::getDurationMinutes).sum();

    if (totalMinutes + durationMinutes > 24 * 60) {
      throw new IllegalArgumentException("O limite diário de 24 horas foi excedido");
    }

    return timeBlocks.save(new TimeBlock(name, durationMinutes, blocks.size(), currentDate()));
  }

  @Transactional
  public void delete(Long blockId) {
    requireOpenDay();

    TimerState state = timerState();
    if (blockId.equals(state.getActiveBlockId())) {
      state.setActiveBlockId(null);
      pause(state);
    }

    timeBlocks.delete(findCurrentDayBlock(blockId));
  }

  @Transactional
  public List<HistoryDayResponse> history() {
    snapshot();

    return timeBlocks.findAll().stream()
        .collect(Collectors.groupingBy(TimeBlock::getPlanDate))
        .entrySet().stream()
        .map(this::toHistoryDay)
        .sorted(Comparator.comparing(HistoryDayResponse::date).reversed())
        .toList();
  }

  private void synchronizeElapsedTime(TimerState state, List<TimeBlock> blocks) {
    if (!state.isRunning() || state.getActiveBlockId() == null || state.getResumedAtEpochMs() == null) {
      return;
    }

    TimeBlock activeBlock = blocks.stream()
        .filter(block -> block.getId().equals(state.getActiveBlockId()))
        .findFirst()
        .orElse(null);

    if (activeBlock == null) {
      pause(state);
      return;
    }

    int elapsedSeconds = (int) ((System.currentTimeMillis() - state.getResumedAtEpochMs()) / 1_000);
    if (elapsedSeconds <= 0) {
      return;
    }

    int consumedSeconds = Math.min(elapsedSeconds, activeBlock.getRemainingSeconds());
    activeBlock.setRemainingSeconds(activeBlock.getRemainingSeconds() - consumedSeconds);
    activeBlock.setSpentSeconds(activeBlock.getSpentSeconds() + consumedSeconds);

    if (activeBlock.getRemainingSeconds() > 0) {
      state.setResumedAtEpochMs(state.getResumedAtEpochMs() + elapsedSeconds * 1_000L);
      return;
    }

    activeBlock.setCompleted(true);
    pause(state);
    state.setActiveBlockId(nextIncompleteBlockId(blocks));
  }

  private void ensureActiveBlock(TimerState state, List<TimeBlock> blocks) {
    boolean hasValidActiveBlock = blocks.stream()
        .anyMatch(block -> block.getId().equals(state.getActiveBlockId()) && !block.isCompleted());

    if (!hasValidActiveBlock) {
      state.setActiveBlockId(nextIncompleteBlockId(blocks));
      pause(state);
    }
  }

  private void normalizeLegacyBlocks(List<TimeBlock> blocks) {
    for (TimeBlock block : blocks) {
      if (!block.isCompleted() && block.getRemainingSeconds() == 0) {
        block.setRemainingSeconds(block.getDurationMinutes() * 60);
      }

      if (block.getSpentSeconds() == 0 && block.getRemainingSeconds() < block.getDurationMinutes() * 60) {
        block.setSpentSeconds(block.getDurationMinutes() * 60 - block.getRemainingSeconds());
      }
    }
  }

  private TimeBlock findCurrentDayBlock(Long blockId) {
    TimeBlock block = timeBlocks.findById(blockId).orElseThrow();
    if (!currentDate().equals(block.getPlanDate())) {
      throw new IllegalArgumentException("Tarefa indisponível");
    }
    return block;
  }

  private void validateBlockIds(List<TimeBlock> blocks, List<Long> blockIds) {
    Set<Long> existingIds = blocks.stream().map(TimeBlock::getId).collect(Collectors.toSet());
    if (blocks.size() != blockIds.size() || !existingIds.equals(Set.copyOf(blockIds))) {
      throw new IllegalArgumentException("A lista de tarefas é inválida");
    }
  }

  private List<TimeBlock> currentDayBlocks() {
    return timeBlocks.findAll().stream()
        .filter(block -> currentDate().equals(block.getPlanDate()))
        .sorted(Comparator.comparing(TimeBlock::getPosition))
        .toList();
  }

  private Long nextIncompleteBlockId(List<TimeBlock> blocks) {
    return blocks.stream()
        .filter(block -> !block.isCompleted())
        .map(TimeBlock::getId)
        .findFirst()
        .orElse(null);
  }

  private TimerState timerState() {
    return timerStates.findById(TIMER_STATE_ID).orElseGet(() -> timerStates.save(new TimerState()));
  }

  private void pause(TimerState state) {
    state.setRunning(false);
    state.setResumedAtEpochMs(null);
    timerStates.save(state);
  }

  private void requireOpenDay() {
    if (isCurrentDayFinished()) {
      throw new IllegalStateException("O dia já foi finalizado");
    }
  }

  private boolean isCurrentDayFinished() {
    return dayArchives.existsById(currentDate());
  }

  private String currentDate() {
    return LocalDate.now(APPLICATION_ZONE).toString();
  }

  private TimerSnapshotResponse toSnapshot(List<TimeBlock> blocks, TimerState state) {
    return new TimerSnapshotResponse(
        blocks,
        state.getActiveBlockId(),
        state.isRunning(),
        currentDate(),
        isCurrentDayFinished());
  }

  private HistoryDayResponse toHistoryDay(Map.Entry<String, List<TimeBlock>> entry) {
    List<TimeBlock> blocks = entry.getValue();

    return new HistoryDayResponse(
        entry.getKey(),
        blocks.stream().mapToInt(TimeBlock::getDurationMinutes).sum(),
        blocks.stream().mapToInt(TimeBlock::getSpentSeconds).sum() / 60,
        (int) blocks.stream().filter(TimeBlock::isCompleted).count(),
        blocks.size(),
        dayArchives.existsById(entry.getKey()));
  }

}
