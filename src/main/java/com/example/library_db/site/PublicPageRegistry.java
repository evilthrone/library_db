package com.example.library_db.site;

import com.example.library_db.config.SiteProperties;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PublicPageRegistry {
    public record Page(String path, String title, String text) {}
    private final List<Page> pages;

    public PublicPageRegistry(SitePageCatalog information, ToolsCatalog tools, SiteProperties site) {
        List<Page> result = new ArrayList<>();
        result.add(new Page("/", site.name(), templateText("site/home") + " " + site.tagline()));
        for (var page : information.getPages()) {
            String text = page.subtitle() + " " + page.description() + " " +
                String.join(" ", page.sections().stream().map(s -> s.heading() + " " + s.text()).toList()) + " " +
                String.join(" ", page.links().stream().map(l -> l.title() + " " + l.description()).toList());
            if (page.slug().equals("contacts")) {
                text += " " + site.city() + " " + site.address() + " " + site.openingHours() + " " + site.phone() + " " + site.email();
            }
            result.add(new Page(page.path(), page.title(), text));
        }
        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("/app", "app-menu");
        templates.put("/reader", "reader-menu");
        templates.put("/staff", "staff-menu");
        templates.put("/input", "input");
        templates.put("/input/book", "input-book");
        templates.put("/search/one", "search-one");
        templates.put("/search/two", "search-two");
        templates.put("/aggregation", "aggregation");
        Map<String, String> titles = Map.of(
            "/app", "Приложение библиотеки", "/reader", "Меню читателя", "/staff", "Меню сотрудника",
            "/input", "Регистрация читателя", "/input/book", "Добавление книги",
            "/search/one", "Поиск книг по жанру", "/search/two", "Поиск книг по автору", "/aggregation", "Агрегация данных");
        templates.forEach((path, template) -> result.add(new Page(path, titles.get(path), templateText(template))));
        result.add(new Page("/services", "Сервисы", String.join(" ", tools.getTools().stream().map(t -> t.title() + " " + t.description()).toList())));
        for (var tool : tools.getTools()) result.add(new Page(tool.path(), tool.title(), tool.description() + " " + toolText(tool.slug())));
        pages = List.copyOf(result);
    }

    // Only static template source is read, never rendered database results or reader records.
    private static String templateText(String name) {
        try (var stream = new ClassPathResource("templates/" + name + ".html").getInputStream()) {
            String html = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            var main = java.util.regex.Pattern.compile("(?s)<main\\b[^>]*>(.*?)</main>").matcher(html);
            if (!main.find()) throw new IllegalStateException("Missing main in " + name);
            return main.group(1).replaceAll("(?s)<script\\b.*?</script>|<!--.*?-->", " ")
                .replaceAll("<[^>]+>", " ").replace("&nbsp;", " ").replace("&amp;", "&")
                .replaceAll("\\s+", " ").trim();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Cannot read public template " + name, ex);
        }
    }

    public List<Page> getPages() { return pages; }
    private static String toolText(String slug) {
        try (var stream = new ClassPathResource("templates/services/tool.html").getInputStream()) {
            String html = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            String marker = "th:case=\"'" + slug + "'\"";
            int start = html.indexOf(marker);
            if (start < 0) throw new IllegalStateException("Missing service content: " + slug);
            int next = html.indexOf("th:case=", start + marker.length());
            int end = next < 0 ? html.indexOf("<noscript>", start) : next;
            return html.substring(start + marker.length(), end).replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Cannot read service content", ex);
        }
    }
    public Map<String, String> visitTitles() {
        Map<String, String> titles = new LinkedHashMap<>();
        pages.forEach(p -> titles.put(p.path(), p.title()));
        titles.put("/site-search", "Результаты поиска по сайту");
        titles.put("/search/one/result", "Результаты поиска по жанру");
        titles.put("/search/two/result", "Результаты поиска по автору");
        titles.put("/error", "Страница ошибки");
        return Collections.unmodifiableMap(titles);
    }
}
