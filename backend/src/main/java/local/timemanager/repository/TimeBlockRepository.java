package local.timemanager.repository;

import local.timemanager.entity.TimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {
}
