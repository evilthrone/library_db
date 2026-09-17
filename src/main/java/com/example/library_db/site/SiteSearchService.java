package com.example.library_db.site;

import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class SiteSearchService {
    public record Result(String path, String title, String snippet, int score) {}
    private final PublicPageRegistry registry;
    public SiteSearchService(PublicPageRegistry registry) { this.registry = registry; }

    public static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('ё', 'е').replaceAll("\\s+", " ").trim();
    }

    public List<Result> search(String query) {
        if (query == null || query.length() > 200) return List.of();
        List<String> terms = Arrays.stream(normalize(query).split("[^\\p{L}\\p{N}]+")).filter(s -> !s.isBlank()).distinct().toList();
        if (terms.isEmpty()) return List.of();
        List<Result> results = new ArrayList<>();
        for (var page : registry.getPages()) {
            String title = normalize(page.title());
            String text = normalize(page.text());
            int score = 0;
            boolean found = true;
            for (String term : terms) {
                boolean inTitle = matches(title, term);
                boolean inText = matches(text, term);
                if (!inTitle && !inText) { found = false; break; }
                score += inTitle ? 20 : 1;
            }
            if (found) results.add(new Result(page.path(), page.title(), snippet(page.text(), terms), score));
        }
        return results.stream().sorted(Comparator.comparingInt(Result::score).reversed().thenComparing(Result::title)).toList();
    }

    private static boolean matches(String text, String term) {
        return Arrays.stream(text.split("[^\\p{L}\\p{N}]+")).anyMatch(word ->
            term.length() >= 3 ? word.startsWith(term) : word.equals(term));
    }

    private static String snippet(String text, List<String> terms) {
        String compact = text.replaceAll("\\s+", " ").trim();
        String normalized = normalize(compact);
        int match = terms.stream().mapToInt(normalized::indexOf).filter(i -> i >= 0).min().orElse(0);
        int start = Math.max(0, match - 45);
        int end = Math.min(compact.length(), start + 200);
        return (start > 0 ? "…" : "") + compact.substring(start, end) + (end < compact.length() ? "…" : "");
    }
}

