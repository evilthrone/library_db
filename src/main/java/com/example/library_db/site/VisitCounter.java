package com.example.library_db.site;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;

@Service
public class VisitCounter {
    public record Row(String path, String title, long count) {}
    private final Map<String, String> titles;
    private final Map<String, Long> counts = new HashMap<>();
    private final Path file;
    private boolean storageAvailable = true;

    public VisitCounter(PublicPageRegistry registry, @Value("${site.visits-file:./data/page-visits.properties}") String fileName) {
        titles = registry.visitTitles();
        file = fileName.isBlank() ? null : Path.of(fileName).toAbsolutePath().normalize();
        if (file != null && Files.exists(file)) {
            try (var input = Files.newInputStream(file)) {
                Properties saved = new Properties();
                saved.load(input);
                for (String path : titles.keySet()) {
                    String value = saved.getProperty(path);
                    if (value != null) counts.put(path, Math.max(0, Long.parseLong(value)));
                }
            } catch (IOException | NumberFormatException ex) {
                storageAvailable = false;
                LoggerFactory.getLogger(getClass()).warn("Visit statistics could not be loaded; existing file will not be overwritten", ex);
            }
        }
    }
    public boolean tracks(String path) { return titles.containsKey(path); }
    public synchronized long recordVisit(String path) {
        if (!tracks(path)) return 0;
        long count = counts.merge(path, 1L, Long::sum);
        save();
        return count;
    }
    public synchronized long count(String path) { return counts.getOrDefault(path, 0L); }
    public synchronized long total() { return counts.values().stream().mapToLong(Long::longValue).sum(); }
    public synchronized boolean isStorageAvailable() { return storageAvailable; }
    public synchronized List<Row> rows() {
        return titles.entrySet().stream().map(e -> new Row(e.getKey(), e.getValue(), count(e.getKey())))
            .sorted(Comparator.comparingLong(Row::count).reversed().thenComparing(Row::title)).toList();
    }
    private void save() {
        if (file == null || !storageAvailable) return;
        try {
            Files.createDirectories(file.getParent());
            Properties saved = new Properties();
            counts.forEach((key, value) -> saved.setProperty(key, value.toString()));
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            try (var output = Files.newOutputStream(temporary)) { saved.store(output, "Page visits; no visitor identifiers"); }
            try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException ex) {
            storageAvailable = false;
            LoggerFactory.getLogger(getClass()).warn("Visit statistics will remain in memory: file is unavailable", ex);
        }
    }
}

