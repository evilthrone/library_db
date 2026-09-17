package com.example.library_db;

import com.example.library_db.service.DualDatabaseLibraryService;
import com.example.library_db.service.LibraryService;
import com.example.library_db.service.MySqlLibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"site.name=Тестовая библиотека", "site.visits-file="})
class SitePagesTests {
    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private LibraryService libraryService;
    @MockitoBean
    private MySqlLibraryService mySqlLibraryService;
    @MockitoBean
    private DualDatabaseLibraryService dualDatabaseLibraryService;

    private MockMvc mvc;

    @Test
    void tenInformationPagesHaveUniqueTitlesAndDoNotCallDatabase() throws Exception {
        var titles = new java.util.HashSet<String>();
        var headings = new java.util.HashSet<String>();
        for (String path : new String[]{"/", "/about", "/rules", "/membership", "/departments",
                "/library-services", "/achievements", "/resources", "/faq", "/contacts"}) {
            var result = mvc.perform(get(path));
            assertLayout(result);
            String html = result.andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            var title = java.util.regex.Pattern.compile("<title>(.*?)</title>").matcher(html);
            var heading = java.util.regex.Pattern.compile("<h1[^>]*>(.*?)</h1>").matcher(html);
            org.junit.jupiter.api.Assertions.assertTrue(title.find(), path);
            org.junit.jupiter.api.Assertions.assertTrue(heading.find(), path);
            org.junit.jupiter.api.Assertions.assertTrue(titles.add(title.group(1)), "Duplicate title: " + path);
            org.junit.jupiter.api.Assertions.assertTrue(headings.add(heading.group(1)), "Duplicate h1: " + path);
            org.junit.jupiter.api.Assertions.assertFalse(heading.find(), "Multiple h1: " + path);
            if (!path.equals("/")) {
                org.junit.jupiter.api.Assertions.assertTrue(html.contains("href=\"" + path + "\""), path);
                org.junit.jupiter.api.Assertions.assertTrue(html.contains("aria-current=\"page\""), path);
            }
        }
        verifyNoInteractions(libraryService, mySqlLibraryService, dualDatabaseLibraryService);
    }

    @Test
    void resourcesAndContactsRenderTheirContent() throws Exception {
        String resources = mvc.perform(get("/resources")).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        long externalLinks = java.util.regex.Pattern.compile("href=\"https://").matcher(resources).results().count();
        org.junit.jupiter.api.Assertions.assertEquals(5, externalLinks);
        mvc.perform(get("/contacts"))
                .andExpect(content().string(containsString("Адрес библиотеки пока не указан")))
                .andExpect(content().string(containsString("Время работы")));
        mvc.perform(get("/membership"))
                .andExpect(content().string(containsString("href=\"/input\"")));
        verifyNoInteractions(libraryService, mySqlLibraryService, dualDatabaseLibraryService);
    }

    private void assertLayout(org.springframework.test.web.servlet.ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"site-header\"")))
                .andExpect(content().string(containsString("class=\"site-logo\"")))
                .andExpect(content().string(containsString("class=\"site-nav\"")))
                .andExpect(content().string(containsString("<footer class=\"site-footer\">")))
                .andExpect(content().string(containsString("Тестовая библиотека")));
    }

    @Test
    void allDatabaseScreensRenderSharedLayout() throws Exception {
        for (String path : new String[]{"/reader", "/staff", "/input", "/input/book",
                "/search/one", "/search/two", "/aggregation"}) {
            assertLayout(mvc.perform(get(path)));
        }
        var book = new com.example.library_db.dto.BookRowDto(1L, "Тестовая книга", "Автор", "Издательство", 2020, 2);
        when(libraryService.findBooksByGenre(1L)).thenReturn(java.util.List.of(book));
        when(libraryService.findByAuthorAndBook(1L, 1L)).thenReturn(java.util.List.of(book));
        for (String path : new String[]{"/search/one/result?genreId=1", "/search/two/result?authorId=1&bookId=1"}) {
            assertLayout(mvc.perform(get(path))
                    .andExpect(content().string(containsString("Тестовая книга"))));
        }
    }

    @Test
    void validationAndDatabaseErrorsKeepSharedLayout() throws Exception {
        assertLayout(mvc.perform(post("/input"))
                .andExpect(view().name("input")));
        assertLayout(mvc.perform(post("/input/book"))
                .andExpect(view().name("input-book")));
        when(libraryService.findAllGenres())
                .thenThrow(new org.springframework.jdbc.CannotGetJdbcConnectionException("Test database unavailable"));
        assertLayout(mvc.perform(get("/search/one"))
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Ошибка подключения к базе данных"))));
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void homeAndAppRenderSharedLayoutWithoutDatabaseCalls() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("site/home"))
                .andExpect(content().string(containsString("<h1 id=\"home-title\">Тестовая библиотека</h1>")))
                .andExpect(content().string(containsString("href=\"/\" aria-current=\"page\"")))
                .andExpect(content().string(containsString("<footer class=\"site-footer\">")));

        mvc.perform(get("/app"))
                .andExpect(status().isOk())
                .andExpect(view().name("app-menu"))
                .andExpect(content().string(containsString("class=\"site-card\" href=\"/reader\"")))
                .andExpect(content().string(containsString("class=\"site-card\" href=\"/staff\"")))
                .andExpect(content().string(containsString("href=\"/app\" aria-current=\"page\"")))
                .andExpect(content().string(containsString("<footer class=\"site-footer\">")));

        verifyNoInteractions(libraryService, mySqlLibraryService, dualDatabaseLibraryService);
    }

    @Test
    void existingMenusAndReaderFormKeepTheirRoutesAndReturnToApp() throws Exception {
        for (String path : new String[]{"/reader", "/staff", "/input"}) {
            mvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("href=\"/app\" aria-current=\"page\">Приложение БД</a>")));
        }
        mvc.perform(get("/css/site.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".site-page")));
        verifyNoInteractions(libraryService, mySqlLibraryService, dualDatabaseLibraryService);
    }
}
