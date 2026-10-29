package local.timemanager.repository;

import local.timemanager.entity.TimerState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimerStateRepository extends JpaRepository<TimerState, Long> {
}
