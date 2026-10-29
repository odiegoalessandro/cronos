package local.timemanager.dto.response;

import java.util.List;
import local.timemanager.entity.TimeBlock;

public record TimerSnapshotResponse(
    List<TimeBlock> blocks,
    Long activeBlockId,
    boolean running,
    String date,
    boolean dayFinished) {
}
