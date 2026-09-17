package com.example.library_db.site;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ToolsCatalog {
    public record Tool(String slug, String title, String description) {
        public String path() { return "/services/" + slug; }
    }
    private final List<Tool> tools = List.of(
        new Tool("visits", "Счётчик посещений", "Число открытий каждой страницы. Повторное открытие и обновление добавляют посещение."),
        new Tool("popular", "Топ страниц", "Рейтинг страниц по общему числу посещений."),
        new Tool("search", "Поиск по сайту", "Поиск слов в заголовках и текстах страниц с переходом к найденным материалам."),
        new Tool("map", "Карта библиотеки", "Интерактивная карта с отметкой местоположения, также доступная в контактах."),
        new Tool("weather", "Погода", "Температура и погодные условия для города библиотеки."),
        new Tool("exchange-rates", "Курсы валют", "Курсы доллара, евро и юаня к рублю с датой данных."),
        new Tool("reading-timer", "Таймер чтения", "Время для спокойного чтения: старт, пауза, сброс и уведомление о завершении."),
        new Tool("isbn", "Проверка ISBN", "Проверка контрольной цифры ISBN-10 и ISBN-13, включая номера с пробелами и дефисами."),
        new Tool("reading-plan", "Планировщик чтения", "Сколько страниц в день читать, чтобы закончить книгу к намеченному сроку."),
        new Tool("return-date", "Дата возврата", "Расчёт даты возврата по дате выдачи и сроку в календарных днях.")
    );
    public List<Tool> getTools() { return tools; }
    public Tool find(String slug) {
        return tools.stream().filter(t -> t.slug().equals(slug)).findFirst()
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
    }
}

