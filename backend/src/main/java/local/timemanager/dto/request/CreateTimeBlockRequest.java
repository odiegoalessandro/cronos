package local.timemanager.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTimeBlockRequest(
    @NotBlank @Size(max = 100) String name,
    @Min(1) @Max(1_440) int durationMinutes) {
}
