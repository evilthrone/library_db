package com.example.library_db.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class BookForm {

    private static final Set<Integer> ALLOWED_AGE_LIMITS = Set.of(0, 6, 12, 16, 18);

    @NotNull(message = "РќСѓР¶РЅРѕ РІС‹Р±СЂР°С‚СЊ РЎРЈР‘Р” РґР»СЏ Р·Р°РїРёСЃРё")
    private DatabaseTarget databaseTarget = DatabaseTarget.BOTH;

    @NotBlank(message = "Название обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String title;

    @NotBlank(message = "ISBN обязателен")
    @Pattern(regexp = "^$|^\\d{13}$", message = "ISBN должен содержать ровно 13 цифр")
    private String isbn;

    @NotNull(message = "Год издания обязателен")
    @Min(value = 1800, message = "Год издания должен быть не меньше 1800")
    @Max(value = 2100, message = "Некорректный год издания")
    private Integer publicationYear;

    @NotBlank(message = "Язык обязателен")
    @Pattern(regexp = "^(ru|en)$", message = "Допустимые значения языка: ru или en")
    private String language;

    @NotNull(message = "Количество страниц обязательно")
    @Min(value = 50, message = "Количество страниц должно быть не меньше 50")
    @Max(value = 5000, message = "Количество страниц должно быть не больше 5000")
    private Integer pageCount;

    @NotNull(message = "Нужно выбрать издательство")
    private Long publisherId;

    @Size(max = 50, message = "Код УДК не должен превышать 50 символов")
    private String udcCode;

    @Size(max = 50, message = "Шифр полки не должен превышать 50 символов")
    private String shelfCode;

    @NotNull(message = "Возрастное ограничение обязательно")
    private Integer ageLimit;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    @NotNull(message = "Количество экземпляров обязательно")
    @Min(value = 1, message = "Количество экземпляров должно быть не меньше 1")
    @Max(value = 1000, message = "Количество экземпляров должно быть не больше 1000")
    private Integer copiesCount;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    @DecimalMax(value = "1000000.00", message = "Цена слишком большая")
    private BigDecimal unitPrice;

    @NotNull(message = "Нужно выбрать автора")
    private Long authorId;

    private String authorName;

    private String publisherName;

    @NotBlank(message = "Нужно указать жанр")
    @Size(max = 100, message = "Жанр не должен превышать 100 символов")
    private String genreName;

    @AssertTrue(message = "Год издания не может быть больше текущего года")
    public boolean isPublicationYearValid() {
        if (publicationYear == null) {
            return true;
        }
        return publicationYear <= LocalDate.now().getYear();
    }

    @AssertTrue(message = "Допустимые возрастные ограничения: 0, 6, 12, 16, 18")
    public boolean isAgeLimitAllowed() {
        if (ageLimit == null) {
            return true;
        }
        return ALLOWED_AGE_LIMITS.contains(ageLimit);
    }

    public DatabaseTarget getDatabaseTarget() {
        return databaseTarget;
    }

    public void setDatabaseTarget(DatabaseTarget databaseTarget) {
        this.databaseTarget = databaseTarget;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = normalize(title);
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = normalize(isbn);
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = normalize(language);
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getUdcCode() {
        return udcCode;
    }

    public void setUdcCode(String udcCode) {
        this.udcCode = normalizeNullable(udcCode);
    }

    public String getShelfCode() {
        return shelfCode;
    }

    public void setShelfCode(String shelfCode) {
        this.shelfCode = normalizeNullable(shelfCode);
    }

    public Integer getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(Integer ageLimit) {
        this.ageLimit = ageLimit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = normalizeNullable(description);
    }

    public Integer getCopiesCount() {
        return copiesCount;
    }

    public void setCopiesCount(Integer copiesCount) {
        this.copiesCount = copiesCount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = normalizeNullable(authorName);
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = normalizeNullable(publisherName);
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = normalize(genreName);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
