package com.example.library_db.service;

import com.example.library_db.dto.AuthorForm;
import com.example.library_db.dto.BookForm;
import com.example.library_db.dto.BookRowDto;
import com.example.library_db.dto.DebtorAggDto;
import com.example.library_db.dto.GenreAggDto;
import com.example.library_db.dto.OptionDto;
import com.example.library_db.dto.PublisherForm;
import com.example.library_db.dto.ReaderForm;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibraryService {

    private final JdbcTemplate jdbcTemplate;

    public LibraryService(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        jdbcTemplate.update(
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

    @Transactional
    public void addBook(BookForm form) {
        long genreId = findOrCreateGenre(form.getGenreName());

        SimpleJdbcInsert bookInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("books")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> bookValues = new HashMap<>();
        bookValues.put("title", form.getTitle());
        bookValues.put("isbn", form.getIsbn());
        bookValues.put("publication_year", form.getPublicationYear());
        bookValues.put("language", form.getLanguage());
        bookValues.put("page_count", form.getPageCount());
        bookValues.put("publisher_id", form.getPublisherId());
        bookValues.put("udc_code", generateUdcCodeIfBlank(form.getUdcCode()));
        bookValues.put("shelf_code", generateShelfCodeIfBlank(form.getShelfCode()));
        bookValues.put("age_limit", form.getAgeLimit());
        bookValues.put("description", form.getDescription());
        bookValues.put("copies_count", form.getCopiesCount());
        bookValues.put("unit_price", form.getUnitPrice());

        Number bookIdNumber = bookInsert.executeAndReturnKey(bookValues);
        long bookId = bookIdNumber.longValue();

        jdbcTemplate.update(
                """
                INSERT INTO book_authors (book_id, author_id, author_order)
                VALUES (?, ?, 1)
                """,
                bookId,
                form.getAuthorId()
        );

        jdbcTemplate.update(
                """
                INSERT INTO book_genres (book_id, genre_id)
                VALUES (?, ?)
                """,
                bookId,
                genreId
        );
    }

    private String generateUdcCodeIfBlank(String value) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return "UDC-" + System.currentTimeMillis();
    }

    private String generateShelfCodeIfBlank(String value) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return "SHELF-" + System.nanoTime();
    }

    private long findOrCreateGenre(String genreName) {
        String findSql = """
                SELECT id
                FROM genres
                WHERE LOWER(name) = LOWER(?)
                LIMIT 1
                """;

        try {
            Long id = jdbcTemplate.queryForObject(findSql, Long.class, genreName);
            if (id != null) {
                return id;
            }
        } catch (EmptyResultDataAccessException ignored) {
        }

        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("genres")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> values = new HashMap<>();
        values.put("name", genreName);
        values.put("description", "Автоматически добавленный жанр");

        Number id = insert.executeAndReturnKey(values);
        return id.longValue();
    }

    public List<OptionDto> findAllGenres() {
        String sql = """
                SELECT id, name
                FROM genres
                ORDER BY name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new OptionDto(rs.getLong("id"), rs.getString("name"))
        );
    }

    public List<OptionDto> findAllAuthors() {
        String sql = """
                SELECT id, full_name
                FROM authors
                ORDER BY full_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new OptionDto(rs.getLong("id"), rs.getString("full_name"))
        );
    }

    public List<OptionDto> findAllPublishers() {
        String sql = """
                SELECT id, name
                FROM publishers
                ORDER BY name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new OptionDto(rs.getLong("id"), rs.getString("name"))
        );
    }

    public List<OptionDto> searchAuthors(String query) {
        String sql = """
                SELECT id, full_name
                FROM authors
                WHERE LOWER(full_name) LIKE LOWER(?)
                ORDER BY full_name
                LIMIT 20
                """;

        String prepared = prepareLikeQuery(query);
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new OptionDto(rs.getLong("id"), rs.getString("full_name")), prepared
        );
    }

    public List<OptionDto> searchPublishers(String query) {
        String sql = """
                SELECT id, name
                FROM publishers
                WHERE LOWER(name) LIKE LOWER(?)
                ORDER BY name
                LIMIT 20
                """;

        String prepared = prepareLikeQuery(query);
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new OptionDto(rs.getLong("id"), rs.getString("name")), prepared
        );
    }

    public List<OptionDto> searchGenres(String query) {
        String sql = """
                SELECT id, name
                FROM genres
                WHERE LOWER(name) LIKE LOWER(?)
                ORDER BY name
                LIMIT 20
                """;

        String prepared = prepareLikeQuery(query);
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new OptionDto(rs.getLong("id"), rs.getString("name")), prepared
        );
    }

    @Transactional
    public OptionDto createAuthor(AuthorForm form) {
        String findSql = """
                SELECT id, full_name
                FROM authors
                WHERE LOWER(full_name) = LOWER(?)
                LIMIT 1
                """;

        try {
            return jdbcTemplate.queryForObject(
                    findSql,
                    (rs, rowNum) -> new OptionDto(rs.getLong("id"), rs.getString("full_name")),
                    form.getFullName()
            );
        } catch (EmptyResultDataAccessException ignored) {
        }

        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("authors")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> values = new HashMap<>();
        values.put("full_name", form.getFullName());
        values.put("birth_date", form.getBirthDate());
        values.put("country", form.getCountry());
        values.put("notes", form.getNotes());

        Number id = insert.executeAndReturnKey(values);
        return new OptionDto(id.longValue(), form.getFullName());
    }

    @Transactional
    public OptionDto createPublisher(PublisherForm form) {
        String findSql = """
                SELECT id, name
                FROM publishers
                WHERE LOWER(name) = LOWER(?)
                LIMIT 1
                """;

        try {
            return jdbcTemplate.queryForObject(
                    findSql,
                    (rs, rowNum) -> new OptionDto(rs.getLong("id"), rs.getString("name")),
                    form.getName()
            );
        } catch (EmptyResultDataAccessException ignored) {
        }

        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("publishers")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> values = new HashMap<>();
        values.put("name", form.getName());
        values.put("city", form.getCity());
        values.put("country", form.getCountry());
        values.put("website", form.getWebsite());

        Number id = insert.executeAndReturnKey(values);
        return new OptionDto(id.longValue(), form.getName());
    }

    public List<BookRowDto> findBooksByGenre(Long genreId) {
        String sql = """
                SELECT
                    b.id,
                    b.title,
                    COALESCE(string_agg(DISTINCT a.full_name, ', '), '—') AS author_name,
                    p.name AS publisher_name,
                    b.publication_year,
                    b.copies_count
                FROM books b
                JOIN book_genres bg ON bg.book_id = b.id
                JOIN publishers p ON p.id = b.publisher_id
                LEFT JOIN book_authors ba ON ba.book_id = b.id
                LEFT JOIN authors a ON a.id = ba.author_id
                WHERE bg.genre_id = ?
                GROUP BY b.id, b.title, p.name, b.publication_year, b.copies_count
                ORDER BY b.title
                """;

        return jdbcTemplate.query(sql, this::mapBookRow, genreId);
    }

    public List<OptionDto> findBooksForAuthor(Long authorId) {
        String sql = """
                SELECT b.id, b.title
                FROM book_authors ba
                JOIN books b ON b.id = ba.book_id
                WHERE ba.author_id = ?
                ORDER BY b.title
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new OptionDto(rs.getLong("id"), rs.getString("title")), authorId
        );
    }

    public List<BookRowDto> findByAuthorAndBook(Long authorId, Long bookId) {
        String sql = """
                SELECT
                    b.id,
                    b.title,
                    COALESCE(string_agg(DISTINCT a.full_name, ', '), '—') AS author_name,
                    p.name AS publisher_name,
                    b.publication_year,
                    b.copies_count
                FROM books b
                JOIN publishers p ON p.id = b.publisher_id
                JOIN book_authors ba_filter ON ba_filter.book_id = b.id
                LEFT JOIN book_authors ba ON ba.book_id = b.id
                LEFT JOIN authors a ON a.id = ba.author_id
                WHERE ba_filter.author_id = ?
                  AND b.id = ?
                GROUP BY b.id, b.title, p.name, b.publication_year, b.copies_count
                ORDER BY b.title
                """;

        return jdbcTemplate.query(sql, this::mapBookRow, authorId, bookId);
    }

    public List<GenreAggDto> getGenreStatistics() {
        String sql = """
                SELECT
                    g.name AS genre_name,
                    COUNT(DISTINCT b.id) AS books_count,
                    COALESCE(SUM(DISTINCT b.copies_count), 0) AS total_copies
                FROM genres g
                LEFT JOIN book_genres bg ON bg.genre_id = g.id
                LEFT JOIN books b ON b.id = bg.book_id
                GROUP BY g.name
                ORDER BY g.name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new GenreAggDto(
                        rs.getString("genre_name"),
                        rs.getInt("books_count"),
                        rs.getInt("total_copies")
                )
        );
    }

    public List<DebtorAggDto> getDebtorStatistics() {
        String sql = """
                SELECT
                    r.last_name || ' ' || r.first_name || COALESCE(' ' || r.middle_name, '') AS full_name,
                    COUNT(f.id) AS fines_count,
                    COALESCE(SUM(f.amount), 0) AS total_debt
                FROM fines f
                JOIN loans l ON l.id = f.loan_id
                JOIN readers r ON r.id = l.reader_id
                WHERE f.fine_status = 'OPEN'
                GROUP BY r.id, r.last_name, r.first_name, r.middle_name
                ORDER BY total_debt DESC, full_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new DebtorAggDto(
                        rs.getString("full_name"),
                        rs.getInt("fines_count"),
                        rs.getBigDecimal("total_debt")
                )
        );
    }

    private String prepareLikeQuery(String query) {
        String q = query == null ? "" : query.trim();
        return "%" + q + "%";
    }

    private BookRowDto mapBookRow(ResultSet rs, int rowNum) throws SQLException {
        return new BookRowDto(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("author_name"),
                rs.getString("publisher_name"),
                rs.getInt("publication_year"),
                rs.getInt("copies_count")
        );
    }

    public String findAuthorNameById(Long authorId) {
        return jdbcTemplate.queryForObject(
                """
                SELECT full_name
                FROM authors
                WHERE id = ?
                """,
                String.class,
                authorId
        );
    }

    public String findPublisherNameById(Long publisherId) {
        return jdbcTemplate.queryForObject(
                """
                SELECT name
                FROM publishers
                WHERE id = ?
                """,
                String.class,
                publisherId
        );
    }
}