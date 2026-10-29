package local.timemanager;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class SqliteMigration implements ApplicationRunner {
  private final JdbcTemplate jdbc;

  public SqliteMigration(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Override
  public void run(ApplicationArguments args) {
    var columns = Set.copyOf(jdbc.queryForList("PRAGMA table_info(time_block)").stream()
      .map(row -> (String) row.get("name"))
      .toList());

    if (!columns.contains("remaining_seconds")) jdbc.execute("ALTER TABLE time_block ADD COLUMN remaining_seconds INTEGER NOT NULL DEFAULT 0");
    if (!columns.contains("completed")) jdbc.execute("ALTER TABLE time_block ADD COLUMN completed BOOLEAN NOT NULL DEFAULT 0");
    if (!columns.contains("spent_seconds")) jdbc.execute("ALTER TABLE time_block ADD COLUMN spent_seconds INTEGER NOT NULL DEFAULT 0");
    if (!columns.contains("plan_date")) jdbc.execute("ALTER TABLE time_block ADD COLUMN plan_date VARCHAR(10)");
    jdbc.update("UPDATE time_block SET plan_date = ? WHERE plan_date IS NULL OR plan_date = ''", java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString());
  }
}
