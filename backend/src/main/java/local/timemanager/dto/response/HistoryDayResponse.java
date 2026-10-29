package local.timemanager.dto.response;

public record HistoryDayResponse(
    String date,
    int plannedMinutes,
    int spentMinutes,
    int completedTasks,
    int totalTasks,
    boolean finished) {
}
