document.addEventListener("DOMContentLoaded", () => {
    const autocompleteInputs = [
        {
            input: document.querySelector('input[name="authorName"]'),
            datalist: document.getElementById("authorsSuggestions")
        },
        {
            input: document.querySelector('input[name="genreName"]'),
            datalist: document.getElementById("genresSuggestions")
        },
        {
            input: document.querySelector('input[name="publisherName"]'),
            datalist: document.getElementById("publishersSuggestions")
        }
    ];

    autocompleteInputs.forEach(({ input, datalist }) => {
        if (!input || !datalist) return;

        let timer = null;

        input.addEventListener("input", () => {
            const value = input.value.trim();
            const url = input.dataset.autocompleteUrl;

            clearTimeout(timer);

            if (!value || value.length < 1) {
                datalist.innerHTML = "";
                return;
            }

            timer = setTimeout(async () => {
                try {
                    const response = await fetch(`${url}?q=${encodeURIComponent(value)}`);
                    if (!response.ok) {
                        datalist.innerHTML = "";
                        return;
                    }

                    const suggestions = await response.json();
                    datalist.innerHTML = "";

                    suggestions.forEach(item => {
                        const option = document.createElement("option");
                        option.value = item;
                        datalist.appendChild(option);
                    });
                } catch (e) {
                    datalist.innerHTML = "";
                }
            }, 250);
        });
    });
});