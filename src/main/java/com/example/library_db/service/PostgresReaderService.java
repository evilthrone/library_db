package com.example.library_db.service;

import com.example.library_db.dto.ReaderForm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PostgresReaderService {

    private final JdbcTemplate postgresJdbcTemplate;

    public PostgresReaderService(@Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbcTemplate) {
        this.postgresJdbcTemplate = postgresJdbcTemplate;
    }

    public void addReader(ReaderForm form) {
        String sql = """
                INSERT INTO readers (
                    last_name,
                    first_name,
                    middle_name,
                    birth_date,
                    phone,
                    email,
                    address,
                    registration_date,
                    reader_status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
                """;

        postgresJdbcTemplate.update(
                sql,
                form.getLastName(),
                form.getFirstName(),
                form.getMiddleName(),
                form.getBirthDate(),
                form.getPhone(),
                form.getEmail(),
                form.getAddress(),
                LocalDate.now()
        );
    }
}