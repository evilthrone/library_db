package com.example.library_db.controller;

import com.example.library_db.dto.DatabaseTarget;
import com.example.library_db.service.LibraryService;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final LibraryService libraryService;

    public GlobalExceptionHandler(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public String handleDbConnectionError(CannotGetJdbcConnectionException ex, Model model) {
        model.addAttribute("errorTitle", "Ошибка подключения к базе данных");
        model.addAttribute("errorMessage", "Приложение не смогло подключиться к PostgreSQL. Проверь, что сервер базы данных запущен.");
        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, Model model) {
        if ("genreId".equals(ex.getName())) {
            model.addAttribute("searchDatabaseTargets", new DatabaseTarget[]{DatabaseTarget.POSTGRES, DatabaseTarget.MYSQL});
            model.addAttribute("selectedDatabaseTarget", DatabaseTarget.POSTGRES);
            model.addAttribute("genres", libraryService.findAllGenres());
            model.addAttribute("formError", "Выберите жанр для поиска.");
            return "search-one";
        }

        if ("bookId".equals(ex.getName()) || "authorId".equals(ex.getName())) {
            model.addAttribute("searchDatabaseTargets", new DatabaseTarget[]{DatabaseTarget.POSTGRES, DatabaseTarget.MYSQL});
            model.addAttribute("selectedDatabaseTarget", DatabaseTarget.POSTGRES);
            model.addAttribute("authors", libraryService.findAllAuthors());
            model.addAttribute("formError", "Выберите автора и книгу для поиска.");
            return "search-two";
        }

        model.addAttribute("errorTitle", "Некорректные параметры запроса");
        model.addAttribute("errorMessage", "Проверьте введенные значения и повторите действие.");
        return "error";
    }
}
