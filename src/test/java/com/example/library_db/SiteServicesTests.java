package com.example.library_db;

import com.example.library_db.site.*;
import com.example.library_db.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"site.name=Тестовая библиотека", "site.visits-file="})
class SiteServicesTests {
    @Autowired WebApplicationContext context;
    @Autowired SiteSearchService search;
    @Autowired PublicPageRegistry pages;
    @Autowired VisitCounter visits;
    @Autowired ToolsCatalog tools;
    @MockitoBean LibraryService libraryService;
    @MockitoBean MySqlLibraryService mySqlLibraryService;
    @MockitoBean DualDatabaseLibraryService dualDatabaseLibraryService;
    MockMvc mvc;

    @BeforeEach void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).build(); }

    @Test void searchFindsContentWithNormalizationAndRanksTitles() {
        assertTrue(search.search("  ЧИТАТЕЛЬСКИЙ   БИЛЕТ ").stream().anyMatch(r -> r.path().equals("/membership")));
        assertTrue(search.search("возврат").stream().anyMatch(r -> r.path().equals("/rules")));
        assertTrue(search.search("время работы").stream().anyMatch(r -> r.path().equals("/contacts")));
        assertEquals(search.search("учёт"), search.search("учет"));
        assertEquals("/rules", search.search("правила пользования").getFirst().path());
        assertTrue(search.search("").isEmpty());
        assertTrue(search.search("!?").isEmpty());
        assertTrue(search.search("неттакойстраницы123").isEmpty());
        assertTrue(search.search("а".repeat(201)).isEmpty());
        assertTrue(search.search("выходные").stream().anyMatch(r -> r.path().equals("/services/return-date")));
    }

    @Test void searchEscapesInputAndAllToolsRenderWithoutDatabase() throws Exception {
        mvc.perform(get("/site-search").param("q", "<script>alert(1)</script>"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
        assertEquals(10, tools.getTools().size());
        for (var tool : tools.getTools()) {
            mvc.perform(get(tool.path())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Поиск по сайту")))
                .andExpect(content().string(containsString("Посещений этой страницы:")))
                .andExpect(content().string(containsString("<footer class=\"site-footer\">")));
        }
        mvc.perform(get("/services")).andExpect(status().isOk());
        mvc.perform(get("/services/unknown")).andExpect(status().isNotFound());
        mvc.perform(get("/services/popular")).andExpect(content().string(containsString("<table class=\"visit-table\">")));
        verifyNoInteractions(libraryService, mySqlLibraryService, dualDatabaseLibraryService);
    }

    @Test void refreshCountsButAssetsHeadAndQueryVariantsDoNotCreateExtraPages() throws Exception {
        long before = visits.count("/about");
        mvc.perform(get("/about")).andExpect(content().string(containsString("Посещений этой страницы: <strong>" + (before + 1))));
        mvc.perform(get("/about").param("anything", "private-value")).andExpect(status().isOk());
        mvc.perform(head("/about")).andExpect(status().isOk());
        mvc.perform(get("/css/site.css")).andExpect(status().isOk());
        assertEquals(before + 2, visits.count("/about"));
        assertFalse(visits.tracks("/about?anything=private-value"));
        assertFalse(visits.tracks("/api/books/by-author"));
        assertFalse(visits.tracks("/unknown"));
    }

    @Test void errorPageRetainsSearchAndVisitCount() throws Exception {
        when(libraryService.findAllGenres()).thenThrow(new org.springframework.jdbc.CannotGetJdbcConnectionException("offline"));
        mvc.perform(get("/search/one")).andExpect(view().name("error"))
            .andExpect(content().string(containsString("Поиск по сайту")))
            .andExpect(content().string(containsString("Посещений этой страницы:")));
    }

    @Test void countsSurviveRestartAndConcurrentUpdates(@TempDir Path directory) throws Exception {
        String file = directory.resolve("visits.properties").toString();
        VisitCounter counter = new VisitCounter(pages, file);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(4)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < 20; i++) futures.add(executor.submit(() -> counter.recordVisit("/about")));
            for (var future : futures) future.get();
        }
        assertEquals(20, counter.count("/about"));
        assertEquals(20, new VisitCounter(pages, file).count("/about"));
        assertEquals(0, counter.recordVisit("/unknown"));
        assertEquals("/about", counter.rows().getFirst().path());
    }
}
