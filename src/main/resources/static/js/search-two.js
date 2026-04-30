document.addEventListener("DOMContentLoaded", () => {
    const authorSelect = document.getElementById("authorId");
    const bookSelect = document.getElementById("bookId");
    const databaseSelect = document.getElementById("databaseTarget");

    if (!authorSelect || !bookSelect) {
        return;
    }

    const selectedAuthor = authorSelect.dataset.selectedAuthor || "";
    const selectedBook = bookSelect.dataset.selectedBook || "";

    async function loadBooks(authorId, selectedBookId = "") {
        bookSelect.innerHTML = "";

        if (!authorId) {
            const option = document.createElement("option");
            option.value = "";
            option.textContent = "-- сначала выберите автора --";
            bookSelect.appendChild(option);
            return;
        }

        try {
            const databaseTarget = databaseSelect ? databaseSelect.value : "POSTGRES";
            const response = await fetch(`/api/books/by-author?authorId=${encodeURIComponent(authorId)}&databaseTarget=${encodeURIComponent(databaseTarget)}`, {
                headers: {
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const books = await response.json();

            bookSelect.innerHTML = "";

            if (!Array.isArray(books) || books.length === 0) {
                const option = document.createElement("option");
                option.value = "";
                option.textContent = "-- у автора нет книг --";
                bookSelect.appendChild(option);
                return;
            }

            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.textContent = "-- выберите книгу --";
            bookSelect.appendChild(defaultOption);

            books.forEach(book => {
                const option = document.createElement("option");
                option.value = book.id;
                option.textContent = book.name;
                if (String(book.id) === String(selectedBookId)) {
                    option.selected = true;
                }
                bookSelect.appendChild(option);
            });
        } catch (error) {
            console.error("Ошибка загрузки списка книг:", error);
            bookSelect.innerHTML = "";

            const option = document.createElement("option");
            option.value = "";
            option.textContent = "-- ошибка загрузки списка книг --";
            bookSelect.appendChild(option);
        }
    }

    authorSelect.addEventListener("change", () => {
        loadBooks(authorSelect.value, "");
    });

    if (selectedAuthor) {
        loadBooks(selectedAuthor, selectedBook);
    } else {
        loadBooks("", "");
    }
});
