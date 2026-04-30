package com.example.library_db.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PublisherForm {

    @NotNull(message = "РќСѓР¶РЅРѕ РІС‹Р±СЂР°С‚СЊ РЎРЈР‘Р” РґР»СЏ Р·Р°РїРёСЃРё")
    private DatabaseTarget databaseTarget = DatabaseTarget.BOTH;

    @NotBlank(message = "Название издательства обязательно")
    @Size(max = 255, message = "Название издательства не должно превышать 255 символов")
    private String name;

    @NotBlank(message = "Город обязателен")
    @Size(max = 100, message = "Город не должен превышать 100 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё\\-\\s]+$",
            message = "Город содержит недопустимые символы"
    )
    private String city;

    @NotBlank(message = "Страна обязательна")
    @Size(max = 100, message = "Страна не должна превышать 100 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё\\-\\s]+$",
            message = "Страна содержит недопустимые символы"
    )
    private String country;

    @NotBlank(message = "Сайт обязателен")
    @Size(max = 255, message = "Сайт не должен превышать 255 символов")
    @Pattern(
            regexp = "^(https?://).+$",
            message = "Сайт должен начинаться с http:// или https://"
    )
    private String website;

    public DatabaseTarget getDatabaseTarget() {
        return databaseTarget;
    }

    public void setDatabaseTarget(DatabaseTarget databaseTarget) {
        this.databaseTarget = databaseTarget;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalize(name);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = normalize(city);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = normalize(country);
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = normalize(website);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
