package local.timemanager.repository;

import local.timemanager.entity.DayArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayArchiveRepository extends JpaRepository<DayArchive, String> {
}
