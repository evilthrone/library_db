document.addEventListener("DOMContentLoaded", () => {
    const dataStore = {
        author: Array.isArray(window.lookupData?.authors) ? [...window.lookupData.authors] : [],
        genre: Array.isArray(window.lookupData?.genres) ? [...window.lookupData.genres] : [],
        publisher: Array.isArray(window.lookupData?.publishers) ? [...window.lookupData.publishers] : []
    };

    function normalizeItems(items) {
        return items.map(item => ({
            id: String(item.id),
            name: item.name
        }));
    }

    dataStore.author = normalizeItems(dataStore.author);
    dataStore.genre = normalizeItems(dataStore.genre);
    dataStore.publisher = normalizeItems(dataStore.publisher);

    function createLookup(type, inputId, hiddenId, dropdownId) {
        const input = document.getElementById(inputId);
        const hidden = hiddenId ? document.getElementById(hiddenId) : null;
        const dropdown = document.getElementById(dropdownId);

        if (!input || !dropdown) return null;

        const state = {
            type,
            input,
            hidden,
            dropdown,
            items: dataStore[type]
        };

        function render(items) {
            dropdown.innerHTML = "";

            if (!items.length) {
                const empty = document.createElement("div");
                empty.className = "lookup-empty";
                empty.textContent = "Ничего не найдено";
                dropdown.appendChild(empty);
                dropdown.classList.remove("hidden");
                return;
            }

            items.forEach(item => {
                const option = document.createElement("button");
                option.type = "button";
                option.className = "lookup-option";
                option.textContent = item.name;

                option.addEventListener("click", () => {
                    selectItem(item);
                });

                dropdown.appendChild(option);
            });

            dropdown.classList.remove("hidden");
        }

        function filterItems(query) {
            const q = query.trim().toLowerCase();
            if (!q) return state.items.slice(0, 8);
            return state.items
                .filter(item => item.name.toLowerCase().includes(q))
                .slice(0, 8);
        }

        function selectItem(item) {
            input.value = item.name;
            if (hidden) {
                hidden.value = item.id;
            }
            dropdown.classList.add("hidden");
        }

        function clearSelectionWhenMismatch() {
            if (!hidden) return;
            const currentText = input.value.trim();
            const matched = state.items.find(item => item.name === currentText);
            if (!matched) {
                hidden.value = "";
            }
        }

        input.addEventListener("focus", () => {
            render(filterItems(input.value));
        });

        input.addEventListener("input", () => {
            clearSelectionWhenMismatch();
            render(filterItems(input.value));
        });

        input.addEventListener("blur", () => {
            setTimeout(() => {
                dropdown.classList.add("hidden");
            }, 150);
        });

        return {
            ...state,
            selectItem,
            addItem(item) {
                const normalized = { id: String(item.id), name: item.name };
                const exists = state.items.some(x => x.id === normalized.id);
                if (!exists) {
                    state.items.push(normalized);
                    state.items.sort((a, b) => a.name.localeCompare(b.name, "ru"));
                    dataStore[type] = state.items;
                }
                selectItem(normalized);
            }
        };
    }

    const authorLookup = createLookup("author", "authorLookup", "authorId", "authorDropdown");
    const genreLookup = createLookup("genre", "genreLookup", null, "genreDropdown");
    const publisherLookup = createLookup("publisher", "publisherLookup", "publisherId", "publisherDropdown");

    document.addEventListener("click", (e) => {
        const isInsideLookup = e.target.closest(".lookup-box");
        if (!isInsideLookup) {
            document.querySelectorAll(".lookup-dropdown").forEach(el => el.classList.add("hidden"));
        }
    });

    const authorDialog = document.getElementById("authorDialog");
    const publisherDialog = document.getElementById("publisherDialog");

    const openAuthorDialog = document.getElementById("openAuthorDialog");
    const openPublisherDialog = document.getElementById("openPublisherDialog");

    const closeAuthorDialog = document.getElementById("closeAuthorDialog");
    const cancelAuthorDialog = document.getElementById("cancelAuthorDialog");

    const closePublisherDialog = document.getElementById("closePublisherDialog");
    const cancelPublisherDialog = document.getElementById("cancelPublisherDialog");

    const authorCreateForm = document.getElementById("authorCreateForm");
    const publisherCreateForm = document.getElementById("publisherCreateForm");

    const authorModalError = document.getElementById("authorModalError");
    const publisherModalError = document.getElementById("publisherModalError");
    const bookDatabaseTarget = document.getElementById("bookDatabaseTarget");
    const authorDatabaseTarget = document.getElementById("authorDatabaseTarget");
    const publisherDatabaseTarget = document.getElementById("publisherDatabaseTarget");

    function syncBookTarget(target) {
        if (bookDatabaseTarget && target) {
            bookDatabaseTarget.value = target;
        }
    }

    function setupDialog(dialog, openBtn, closeBtns) {
        if (!dialog) return;

        if (openBtn) {
            openBtn.addEventListener("click", () => dialog.showModal());
        }

        closeBtns.forEach(btn => {
            if (btn) btn.addEventListener("click", () => dialog.close());
        });

        dialog.addEventListener("click", (e) => {
            const rect = dialog.getBoundingClientRect();
            const inside =
                e.clientX >= rect.left &&
                e.clientX <= rect.right &&
                e.clientY >= rect.top &&
                e.clientY <= rect.bottom;

            if (!inside) {
                dialog.close();
            }
        });
    }

    setupDialog(authorDialog, openAuthorDialog, [closeAuthorDialog, cancelAuthorDialog]);
    setupDialog(publisherDialog, openPublisherDialog, [closePublisherDialog, cancelPublisherDialog]);

    if (authorCreateForm) {
        authorCreateForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            authorModalError.textContent = "";

            const payload = {
                databaseTarget: authorDatabaseTarget?.value || "BOTH",
                fullName: document.getElementById("authorFullName").value.trim(),
                birthDate: document.getElementById("authorBirthDate").value,
                country: document.getElementById("authorCountry").value.trim(),
                notes: document.getElementById("authorNotes").value.trim()
            };

            if (!payload.fullName || !payload.birthDate || !payload.country || !payload.notes) {
                authorModalError.textContent = "Заполните все поля автора.";
                return;
            }

            try {
                const response = await fetch("/api/authors", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    authorModalError.textContent = "Не удалось добавить автора.";
                    return;
                }

                const created = await response.json();
                authorLookup.addItem(created);
                syncBookTarget(payload.databaseTarget);
                authorDialog.close();
                authorCreateForm.reset();
            } catch (err) {
                authorModalError.textContent = "Ошибка сети или сервера.";
            }
        });
    }

    if (publisherCreateForm) {
        publisherCreateForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            publisherModalError.textContent = "";

            const payload = {
                databaseTarget: publisherDatabaseTarget?.value || "BOTH",
                name: document.getElementById("publisherName").value.trim(),
                city: document.getElementById("publisherCity").value.trim(),
                country: document.getElementById("publisherCountry").value.trim(),
                website: document.getElementById("publisherWebsite").value.trim()
            };

            if (!payload.name || !payload.city || !payload.country || !payload.website) {
                publisherModalError.textContent = "Заполните все поля издательства.";
                return;
            }

            if (!/^https?:\/\/.+/i.test(payload.website)) {
                publisherModalError.textContent = "Сайт должен начинаться с http:// или https://";
                return;
            }

            try {
                const response = await fetch("/api/publishers", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    publisherModalError.textContent = "Не удалось добавить издательство.";
                    return;
                }

                const created = await response.json();
                publisherLookup.addItem(created);
                syncBookTarget(payload.databaseTarget);
                publisherDialog.close();
                publisherCreateForm.reset();
            } catch (err) {
                publisherModalError.textContent = "Ошибка сети или сервера.";
            }
        });
    }
});
