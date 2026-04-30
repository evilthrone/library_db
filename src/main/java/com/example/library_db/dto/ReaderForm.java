package com.example.library_db.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ReaderForm {

    @NotNull(message = "РќСѓР¶РЅРѕ РІС‹Р±СЂР°С‚СЊ РЎРЈР‘Р” РґР»СЏ Р·Р°РїРёСЃРё")
    private DatabaseTarget databaseTarget = DatabaseTarget.BOTH;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    @Pattern(
            regexp = "^$|^[A-Za-zА-Яа-яЁё\\-\\s]+$",
            message = "Фамилия может содержать только буквы, пробелы и дефис"
    )
    private String lastName;

    @NotBlank(message = "Имя обязательно")
    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    @Pattern(
            regexp = "^$|^[A-Za-zА-Яа-яЁё\\-\\s]+$",
            message = "Имя может содержать только буквы, пробелы и дефис"
    )
    private String firstName;

    @Size(max = 100, message = "Отчество не должно превышать 100 символов")
    @Pattern(
            regexp = "^$|^[A-Za-zА-Яа-яЁё\\-\\s]+$",
            message = "Отчество может содержать только буквы, пробелы и дефис"
    )
    private String middleName;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank(message = "Телефон обязателен")
    @Size(max = 30, message = "Телефон не должен превышать 30 символов")
    @Pattern(
            regexp = "^$|^\\+?[0-9()\\-\\s]{7,30}$",
            message = "Некорректный формат телефона"
    )
    private String phone;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    @Size(max = 150, message = "Email не должен превышать 150 символов")
    private String email;

    @NotBlank(message = "Адрес обязателен")
    @Size(max = 255, message = "Адрес не должен превышать 255 символов")
    private String address;

    public DatabaseTarget getDatabaseTarget() {
        return databaseTarget;
    }

    public void setDatabaseTarget(DatabaseTarget databaseTarget) {
        this.databaseTarget = databaseTarget;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = normalize(lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = normalize(firstName);
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = normalizeNullable(middleName);
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = normalize(phone);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalize(email);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = normalize(address);
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
