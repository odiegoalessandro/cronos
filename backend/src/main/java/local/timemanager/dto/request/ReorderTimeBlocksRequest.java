package local.timemanager.dto.request;

import java.util.List;

public record ReorderTimeBlocksRequest(List<Long> blockIds) {
}
