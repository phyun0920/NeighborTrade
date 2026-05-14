package com.study.neighbortrade.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * PostgreSQL 호환 DB에서 SERIAL/IDENTITY 시퀀스가 테이블의 MAX(pk)보다 작을 때 나는 PK 중복 오류를
 * 기동 시 한 번씩 맞춥니다.
 * <p>
 * Supabase는 호스팅된 PostgreSQL이며, 일반적으로 {@code jdbc:postgresql://...supabase.com...} 형태의
 * URL을 쓰므로 로컬 PostgreSQL과 동일하게 동작합니다. (별도의 Supabase 전용 API가 필요하지 않음)
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Integer.MAX_VALUE)
@ConditionalOnProperty(name = "app.db.postgres-sequence-sync", havingValue = "true", matchIfMissing = true)
public class PostgreSqlIdentitySequenceSync implements CommandLineRunner {

    private final Environment environment;
    private final JdbcTemplate jdbcTemplate;

    /** 물리 테이블명과 PK 컬럼 (코드에 정의된 엔티티와 동일해야 함). */
    private static final String[][] TABLES = {
            {"member", "id"},
            {"neighborhood", "id"},
            {"location_verification", "id"},
            {"product_post", "id"},
            {"product_image", "id"},
            {"trade", "id"},
            {"review", "id"},
            {"community_post", "id"},
            {"community_comment", "id"},
            {"report", "id"},
            {"chat_room", "id"},
            {"chat_message", "id"},
            {"manner_score_history", "id"},
    };

    @Override
    public void run(String... args) {
        String url = environment.getProperty("spring.datasource.url", "");
        if (!isPostgresqlBackedDatasource(url)) {
            return;
        }

        for (String[] row : TABLES) {
            syncTable(row[0], row[1]);
        }
    }

    private void syncTable(String table, String pkColumn) {
        try {
            sync(table, pkColumn);
        } catch (Exception e) {
            log.debug("PostgreSQL 시퀀스 동기화 스킵: {} — {}", table, e.getMessage());
        }
    }

    private void sync(String table, String pkColumn) {
        String sequenceName =
                jdbcTemplate.queryForObject(
                        "SELECT pg_get_serial_sequence(?, ?)", String.class, table, pkColumn);
        if (sequenceName == null || sequenceName.isBlank()) {
            return;
        }

        Long max =
                jdbcTemplate.queryForObject(
                        "SELECT MAX(" + pkColumn + ") FROM " + table, Long.class);

        String escapedSeq = sequenceName.replace("'", "''");
        if (max == null) {
            jdbcTemplate.execute("SELECT setval('" + escapedSeq + "', 1, false)");
            log.trace("시퀀스 '{}' 초기화 (테이블 '{}' 비어 있음)", sequenceName, table);
        } else {
            jdbcTemplate.execute("SELECT setval('" + escapedSeq + "', " + max + ")");
            log.trace("시퀀스 '{}' ← MAX({}.{}) = {}", sequenceName, table, pkColumn, max);
        }
    }

    /**
     * 실제 PostgreSQL(Supabase 포함)에 붙었을 때만 true.
     * H2를 PostgreSQL 호환 모드로 쓰는 경우 등은 제외합니다.
     */
    private static boolean isPostgresqlBackedDatasource(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String u = url.toLowerCase(Locale.ROOT);
        if (u.startsWith("jdbc:h2:") || u.contains(":h2:")) {
            return false;
        }
        return u.contains("postgresql")
                || u.contains("pgsql")
                || u.contains("supabase.co");
    }
}
