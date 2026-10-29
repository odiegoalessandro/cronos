package local.timemanager;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimerStateRepository extends JpaRepository<TimerState, Long> {}
