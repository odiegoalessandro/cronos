package local.timemanager.config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SqliteMigration implements ApplicationRunner {

  private static final ZoneId APPLICATION_ZONE = ZoneId.of("America/Sao_Paulo");

  private final JdbcTemplate jdbcTemplate;

  public SqliteMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    Set<String> columns = readTimeBlockColumns();

    addColumnIfMissing(columns, "remaining_seconds", "INTEGER NOT NULL DEFAULT 0");
    addColumnIfMissing(columns, "completed", "BOOLEAN NOT NULL DEFAULT 0");
    addColumnIfMissing(columns, "spent_seconds", "INTEGER NOT NULL DEFAULT 0");
    addColumnIfMissing(columns, "plan_date", "VARCHAR(10)");

    jdbcTemplate.update(
        "UPDATE time_block SET plan_date = ? WHERE plan_date IS NULL OR plan_date = ''",
        LocalDate.now(APPLICATION_ZONE).toString());
  }

  private Set<String> readTimeBlockColumns() {
    return Set.copyOf(jdbcTemplate.queryForList("PRAGMA table_info(time_block)").stream()
        .map(row -> (String) row.get("name"))
        .toList());
  }

  private void addColumnIfMissing(Set<String> columns, String name, String definition) {
    if (!columns.contains(name)) {
      jdbcTemplate.execute("ALTER TABLE time_block ADD COLUMN " + name + " " + definition);
    }
  }
}
