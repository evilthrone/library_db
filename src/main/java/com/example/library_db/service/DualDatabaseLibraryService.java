package com.example.library_db.service;

import com.example.library_db.dto.AuthorForm;
import com.example.library_db.dto.BookForm;
import com.example.library_db.dto.OptionDto;
import com.example.library_db.dto.PublisherForm;
import com.example.library_db.dto.ReaderForm;
import org.springframework.stereotype.Service;

@Service
public class DualDatabaseLibraryService {

    private final LibraryService postgresService;
    private final MySqlLibraryService mySqlService;

    public DualDatabaseLibraryService(
            LibraryService postgresService,
            MySqlLibraryService mySqlService
    ) {
        this.postgresService = postgresService;
        this.mySqlService = mySqlService;
    }

    public void addReader(ReaderForm form) {
        if (form.getDatabaseTarget().writesPostgres()) {
            postgresService.addReader(form);
        }
        if (form.getDatabaseTarget().writesMySql()) {
            mySqlService.addReader(form);
        }
    }

    public OptionDto addAuthor(AuthorForm form) {
        OptionDto result = null;
        if (form.getDatabaseTarget().writesPostgres()) {
            result = postgresService.createAuthor(form);
        }
        if (form.getDatabaseTarget().writesMySql()) {
            OptionDto mySqlAuthor = mySqlService.createAuthor(form);
            if (result == null) {
                result = mySqlAuthor;
            }
        }
        return result;
    }

    public OptionDto addPublisher(PublisherForm form) {
        OptionDto result = null;
        if (form.getDatabaseTarget().writesPostgres()) {
            result = postgresService.createPublisher(form);
        }
        if (form.getDatabaseTarget().writesMySql()) {
            OptionDto mySqlPublisher = mySqlService.createPublisher(form);
            if (result == null) {
                result = mySqlPublisher;
            }
        }
        return result;
    }

    public void addBook(BookForm form) {
        if (form.getDatabaseTarget().writesPostgres()) {
            postgresService.addBook(form);
        }
        if (form.getDatabaseTarget().writesMySql()) {
            String authorFullName = form.getAuthorName();
            if (authorFullName == null || authorFullName.isBlank()) {
                authorFullName = postgresService.findAuthorNameById(form.getAuthorId());
            }

            String publisherName = form.getPublisherName();
            if (publisherName == null || publisherName.isBlank()) {
                publisherName = postgresService.findPublisherNameById(form.getPublisherId());
            }

            mySqlService.addBook(form, authorFullName, publisherName);
        }
    }
}
