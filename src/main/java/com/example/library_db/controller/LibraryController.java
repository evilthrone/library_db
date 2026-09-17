package com.example.library_db.controller;

import com.example.library_db.dto.AuthorForm;
import com.example.library_db.dto.BookForm;
import com.example.library_db.dto.PublisherForm;
import com.example.library_db.dto.ReaderForm;
import com.example.library_db.dto.DatabaseTarget;
import com.example.library_db.dto.DebtorAggDto;
import com.example.library_db.dto.GenreAggDto;
import com.example.library_db.service.DualDatabaseLibraryService;
import com.example.library_db.service.LibraryService;
import com.example.library_db.service.MySqlLibraryService;
import jakarta.validation.Valid;
import com.example.library_db.dto.BookRowDto;
import com.example.library_db.dto.OptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.Period;

@Controller
@RequestMapping("/")
public class LibraryController {

    private final LibraryService libraryService;
    private final MySqlLibraryService mySqlLibraryService;
    private final DualDatabaseLibraryService dualDatabaseLibraryService;

    public LibraryController(
            LibraryService libraryService,
            MySqlLibraryService mySqlLibraryService,
            DualDatabaseLibraryService dualDatabaseLibraryService
    ) {
        this.libraryService = libraryService;
        this.mySqlLibraryService = mySqlLibraryService;
        this.dualDatabaseLibraryService = dualDatabaseLibraryService;
    }

    @ModelAttribute("databaseTargets")
    public DatabaseTarget[] databaseTargets() {
        return DatabaseTarget.values();
    }

    @ModelAttribute("searchDatabaseTargets")
    public DatabaseTarget[] searchDatabaseTargets() {
        return new DatabaseTarget[]{DatabaseTarget.POSTGRES, DatabaseTarget.MYSQL};
    }

    @GetMapping("/reader")
    public String readerMenu() {
        return "reader-menu";
    }

    @GetMapping("/staff")
    public String staffMenu() {
        return "staff-menu";
    }

    @GetMapping("/input")
    public String inputPage(Model model) {
        if (!model.containsAttribute("readerForm")) {
            model.addAttribute("readerForm", new ReaderForm());
        }
        return "input";
    }

    @PostMapping("/input")
    public String addReader(
            @Valid @ModelAttribute("readerForm") ReaderForm readerForm,
            BindingResult bindingResult,
            Model model
    ) {
        validateReaderBusinessRules(readerForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("readerForm", readerForm);
            return "input";
        }

        try {
            dualDatabaseLibraryService.addReader(readerForm);
            model.addAttribute("successMessage", "Читатель успешно добавлен");
            model.addAttribute("readerForm", new ReaderForm());
            return "input";
        } catch (Exception e) {
            model.addAttribute("readerForm", readerForm);
            model.addAttribute("formError", "Не удалось добавить читателя");
            return "input";
        }
    }

    @GetMapping("/input/book")
    public String inputBookPage(Model model) {
        if (!model.containsAttribute("bookForm")) {
            model.addAttribute("bookForm", new BookForm());
        }
        if (!model.containsAttribute("authorForm")) {
            model.addAttribute("authorForm", new AuthorForm());
        }
        if (!model.containsAttribute("publisherForm")) {
            model.addAttribute("publisherForm", new PublisherForm());
        }

        model.addAttribute("authors", libraryService.findAllAuthors());
        model.addAttribute("genres", libraryService.findAllGenres());
        model.addAttribute("publishers", libraryService.findAllPublishers());

        return "input-book";
    }

    @PostMapping("/input/book")
    public String addBook(
            @Valid @ModelAttribute("bookForm") BookForm bookForm,
            BindingResult bindingResult,
            Model model
    ) {
        validateBookBusinessRules(bookForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("authorForm", new AuthorForm());
            model.addAttribute("publisherForm", new PublisherForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        }

        try {
            dualDatabaseLibraryService.addBook(bookForm);
            model.addAttribute("successMessage", "Книга успешно добавлена");
            model.addAttribute("bookForm", new BookForm());
            model.addAttribute("authorForm", new AuthorForm());
            model.addAttribute("publisherForm", new PublisherForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        } catch (Exception e) {
            bindingResult.reject("book.add.failed", buildBookAddErrorMessage(e));
            model.addAttribute("authorForm", new AuthorForm());
            model.addAttribute("publisherForm", new PublisherForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        }
    }

    private String buildBookAddErrorMessage(Exception e) {
        String detail = e.getMessage();
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                detail = cause.getMessage();
            }
            cause = cause.getCause();
        }

        if (detail == null || detail.isBlank()) {
            return "Не удалось добавить книгу.";
        }
        return "Не удалось добавить книгу: " + detail;
    }

    @PostMapping("/input/author")
    public String addAuthor(
            @Valid @ModelAttribute("authorForm") AuthorForm authorForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookForm", new BookForm());
            model.addAttribute("publisherForm", new PublisherForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        }

        try {
            dualDatabaseLibraryService.addAuthor(authorForm);
            model.addAttribute("successMessage", "Автор успешно добавлен");
            model.addAttribute("bookForm", new BookForm());
            model.addAttribute("authorForm", new AuthorForm());
            model.addAttribute("publisherForm", new PublisherForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        } catch (Exception e) {
            bindingResult.reject("author.add.failed", "Не удалось добавить автора");
            model.addAttribute("bookForm", new BookForm());
            model.addAttribute("publisherForm", new PublisherForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        }
    }

    @PostMapping("/input/publisher")
    public String addPublisher(
            @Valid @ModelAttribute("publisherForm") PublisherForm publisherForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookForm", new BookForm());
            model.addAttribute("authorForm", new AuthorForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        }

        try {
            dualDatabaseLibraryService.addPublisher(publisherForm);
            model.addAttribute("successMessage", "Издательство успешно добавлено");
            model.addAttribute("bookForm", new BookForm());
            model.addAttribute("authorForm", new AuthorForm());
            model.addAttribute("publisherForm", new PublisherForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        } catch (Exception e) {
            bindingResult.reject("publisher.add.failed", "Не удалось добавить издательство");
            model.addAttribute("bookForm", new BookForm());
            model.addAttribute("authorForm", new AuthorForm());
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("publishers", libraryService.findAllPublishers());
            return "input-book";
        }
    }

    @GetMapping("/search/one")
    public String searchOne(
            @RequestParam(value = "databaseTarget", required = false) DatabaseTarget databaseTarget,
            Model model
    ) {
        DatabaseTarget target = normalizeSearchTarget(databaseTarget);
        model.addAttribute("selectedDatabaseTarget", target);
        model.addAttribute("genres", findAllGenres(target));
        return "search-one";
    }

    @GetMapping("/search/two")
    public String searchTwo(
            @RequestParam(value = "databaseTarget", required = false) DatabaseTarget databaseTarget,
            Model model
    ) {
        DatabaseTarget target = normalizeSearchTarget(databaseTarget);
        model.addAttribute("selectedDatabaseTarget", target);
        model.addAttribute("authors", findAllAuthors(target));
        return "search-two";
    }

    @GetMapping("/aggregation")
    public String aggregation(
            @RequestParam(value = "databaseTarget", required = false) DatabaseTarget databaseTarget,
            Model model
    ) {
        DatabaseTarget target = normalizeSearchTarget(databaseTarget);
        model.addAttribute("selectedDatabaseTarget", target);
        model.addAttribute("genreStatistics", getGenreStatistics(target));
        model.addAttribute("fineStatistics", getDebtorStatistics(target));
        return "aggregation";
    }

    @GetMapping("/search/one/result")
    public String searchOneResult(
            @RequestParam(value = "genreId", required = false) Long genreId,
            @RequestParam(value = "databaseTarget", required = false) DatabaseTarget databaseTarget,
            Model model
    ) {
        DatabaseTarget target = normalizeSearchTarget(databaseTarget);
        model.addAttribute("selectedDatabaseTarget", target);
        model.addAttribute("genres", findAllGenres(target));
        model.addAttribute("selectedGenreId", genreId);

        if (genreId == null) {
            model.addAttribute("formError", "Выберите жанр для поиска.");
            return "search-one";
        }

        model.addAttribute("books", findBooksByGenre(target, genreId));
        return "search-one";
    }

    @GetMapping("/api/books/by-author")
    public ResponseEntity<List<OptionDto>> getBooksByAuthor(
            @RequestParam("authorId") Long authorId,
            @RequestParam(value = "databaseTarget", required = false) DatabaseTarget databaseTarget
    ) {
        DatabaseTarget target = normalizeSearchTarget(databaseTarget);
        return ResponseEntity.ok(findBooksForAuthor(target, authorId));
    }

    @PostMapping("/api/authors")
    public ResponseEntity<OptionDto> addAuthorApi(
            @Valid @RequestBody AuthorForm authorForm
    ) {
        return ResponseEntity.ok(dualDatabaseLibraryService.addAuthor(authorForm));
    }

    @PostMapping("/api/publishers")
    public ResponseEntity<OptionDto> addPublisherApi(
            @Valid @RequestBody PublisherForm publisherForm
    ) {
        return ResponseEntity.ok(dualDatabaseLibraryService.addPublisher(publisherForm));
    }

    @GetMapping("/search/two/result")
    public String searchTwoResult(
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "bookId", required = false) Long bookId,
            @RequestParam(value = "databaseTarget", required = false) DatabaseTarget databaseTarget,
            Model model
    ) {
        DatabaseTarget target = normalizeSearchTarget(databaseTarget);
        model.addAttribute("selectedDatabaseTarget", target);
        model.addAttribute("authors", findAllAuthors(target));
        model.addAttribute("selectedAuthorId", authorId);
        model.addAttribute("selectedBookId", bookId);

        if (authorId == null || bookId == null) {
            if (authorId != null) {
                model.addAttribute("booksForAuthor", findBooksForAuthor(target, authorId));
            }
            model.addAttribute("formError", "Выберите автора и книгу для поиска.");
            return "search-two";
        }

        model.addAttribute("booksForAuthor", findBooksForAuthor(target, authorId));
        model.addAttribute("results", findByAuthorAndBook(target, authorId, bookId));
        return "search-two";
    }

    private DatabaseTarget normalizeSearchTarget(DatabaseTarget databaseTarget) {
        return databaseTarget == DatabaseTarget.MYSQL ? DatabaseTarget.MYSQL : DatabaseTarget.POSTGRES;
    }

    private List<OptionDto> findAllGenres(DatabaseTarget target) {
        return target == DatabaseTarget.MYSQL
                ? mySqlLibraryService.findAllGenres()
                : libraryService.findAllGenres();
    }

    private List<OptionDto> findAllAuthors(DatabaseTarget target) {
        return target == DatabaseTarget.MYSQL
                ? mySqlLibraryService.findAllAuthors()
                : libraryService.findAllAuthors();
    }

    private List<OptionDto> findBooksForAuthor(DatabaseTarget target, Long authorId) {
        return target == DatabaseTarget.MYSQL
                ? mySqlLibraryService.findBooksForAuthor(authorId)
                : libraryService.findBooksForAuthor(authorId);
    }

    private List<BookRowDto> findBooksByGenre(DatabaseTarget target, Long genreId) {
        return target == DatabaseTarget.MYSQL
                ? mySqlLibraryService.findBooksByGenre(genreId)
                : libraryService.findBooksByGenre(genreId);
    }

    private List<BookRowDto> findByAuthorAndBook(DatabaseTarget target, Long authorId, Long bookId) {
        return target == DatabaseTarget.MYSQL
                ? mySqlLibraryService.findByAuthorAndBook(authorId, bookId)
                : libraryService.findByAuthorAndBook(authorId, bookId);
    }

    private List<GenreAggDto> getGenreStatistics(DatabaseTarget target) {
        return target == DatabaseTarget.MYSQL
                ? mySqlLibraryService.getGenreStatistics()
                : libraryService.getGenreStatistics();
    }

    private List<DebtorAggDto> getDebtorStatistics(DatabaseTarget target) {
        return target == DatabaseTarget.MYSQL
                ? mySqlLibraryService.getDebtorStatistics()
                : libraryService.getDebtorStatistics();
    }

    private void validateReaderBusinessRules(ReaderForm form, BindingResult bindingResult) {
        if (form.getBirthDate() != null) {
            int age = Period.between(form.getBirthDate(), LocalDate.now()).getYears();
            if (age < 5 || age > 120) {
                bindingResult.rejectValue(
                        "birthDate",
                        "reader.birthDate.range",
                        "Возраст читателя должен быть в диапазоне от 5 до 120 лет"
                );
            }
        }

        if (form.getAddress() != null && !form.getAddress().isBlank()) {
            String address = form.getAddress().trim();
            if (address.length() < 3) {
                bindingResult.rejectValue(
                        "address",
                        "reader.address.tooShort",
                        "Адрес должен содержать не менее 3 символов"
                );
            }
        }
    }

    private void validateBookBusinessRules(BookForm form, BindingResult bindingResult) {
        if (form.getPublicationYear() != null && form.getPublicationYear() > LocalDate.now().getYear()) {
            bindingResult.rejectValue(
                    "publicationYear",
                    "book.publicationYear.future",
                    "Год издания не может быть больше текущего года"
            );
        }

        if (form.getAgeLimit() != null &&
                !(form.getAgeLimit() == 0 ||
                        form.getAgeLimit() == 6 ||
                        form.getAgeLimit() == 12 ||
                        form.getAgeLimit() == 16 ||
                        form.getAgeLimit() == 18)) {
            bindingResult.rejectValue(
                    "ageLimit",
                    "book.ageLimit.invalid",
                    "Допустимые возрастные ограничения: 0, 6, 12, 16, 18"
            );
        }
    }
}
