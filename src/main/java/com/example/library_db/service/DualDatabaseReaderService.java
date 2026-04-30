package com.example.library_db.service;

import com.example.library_db.dto.ReaderForm;
import org.springframework.stereotype.Service;

@Service
public class DualDatabaseReaderService {

    private final PostgresReaderService postgresReaderService;
    private final MySqlReaderService mySqlReaderService;

    public DualDatabaseReaderService(
            PostgresReaderService postgresReaderService,
            MySqlReaderService mySqlReaderService
    ) {
        this.postgresReaderService = postgresReaderService;
        this.mySqlReaderService = mySqlReaderService;
    }

    public void addReaderToBothDatabases(ReaderForm form) {
        postgresReaderService.addReader(form);
        mySqlReaderService.addReader(form);
    }
}