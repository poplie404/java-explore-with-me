package ru.practicum.ewm.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateColumnTypes() {
        try {
            Integer tableExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_name = 'events'",
                    Integer.class
            );

            if (tableExists == null || tableExists == 0) {
                log.debug("Events table does not exist yet, skipping migration");
                return;
            }

            Integer annotationExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                            "WHERE table_name = 'events' AND column_name = 'annotation'",
                    Integer.class
            );

            if (annotationExists != null && annotationExists > 0) {
                String annotationType = jdbcTemplate.queryForObject(
                        "SELECT data_type FROM information_schema.columns " +
                                "WHERE table_name = 'events' AND column_name = 'annotation'",
                        String.class
                );

                if ("bytea".equals(annotationType)) {
                    log.info("Converting annotation column from bytea to text");
                    jdbcTemplate.execute("ALTER TABLE events ALTER COLUMN annotation TYPE text USING annotation::text");
                    log.info("Annotation column converted successfully");
                }
            }

            Integer descriptionExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                            "WHERE table_name = 'events' AND column_name = 'description'",
                    Integer.class
            );

            if (descriptionExists != null && descriptionExists > 0) {
                String descriptionType = jdbcTemplate.queryForObject(
                        "SELECT data_type FROM information_schema.columns " +
                                "WHERE table_name = 'events' AND column_name = 'description'",
                        String.class
                );

                if ("bytea".equals(descriptionType)) {
                    log.info("Converting description column from bytea to text");
                    jdbcTemplate.execute("ALTER TABLE events ALTER COLUMN description TYPE text USING description::text");
                    log.info("Description column converted successfully");
                }
            }
        } catch (Exception e) {
            log.warn("Could not migrate column types: {}", e.getMessage());
        }
    }
}

