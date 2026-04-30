package com.example.library_db.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AuthorForm {

    @NotNull(message = "РќСѓР¶РЅРѕ РІС‹Р±СЂР°С‚СЊ РЎРЈР‘Р” РґР»СЏ Р·Р°РїРёСЃРё")
    private DatabaseTarget databaseTarget = DatabaseTarget.BOTH;

    @NotBlank(message = "ФИО автора обязательно")
    @Size(max = 255, message = "ФИО автора не должно превышать 255 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё0-9\\-\\s.]+$",
            message = "ФИО автора содержит недопустимые символы"
    )
    private String fullName;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения автора должна быть в прошлом")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank(message = "Страна обязательна")
    @Size(max = 100, message = "Страна не должна превышать 100 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё\\-\\s]+$",
            message = "Страна содержит недопустимые символы"
    )
    private String country;

    @NotBlank(message = "Примечание обязательно")
    @Size(max = 500, message = "Примечание не должно превышать 500 символов")
    private String notes;

    public DatabaseTarget getDatabaseTarget() {
        return databaseTarget;
    }

    public void setDatabaseTarget(DatabaseTarget databaseTarget) {
        this.databaseTarget = databaseTarget;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = normalize(fullName);
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = normalize(country);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = normalize(notes);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
