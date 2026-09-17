import {integer, validIsbn, readingPlan, returnDate, ReadingTimer, fetchJson, weatherData, exchangeData} from "./tools-core.mjs";

const russianDate = value => new Date(value).toLocaleString("ru-RU");

for (const form of document.querySelectorAll(".calculator")) {
    form.addEventListener("submit", event => {
        event.preventDefault();
        const result = form.querySelector(".tool-result");
        const values = new FormData(form);
        try {
            switch (form.dataset.calculator) {
                case "isbn":
                    result.textContent = validIsbn(values.get("isbn")) ? "Контрольная цифра ISBN верна." : "ISBN некорректен: проверь длину, символы и контрольную цифру.";
                    break;
                case "reading-plan": {
                    const pages = readingPlan(values.get("pages"), values.get("read"), values.get("days"));
                    result.textContent = pages === 0 ? "Книга уже прочитана!" : `Читай по ${pages} стр. в день.`;
                    break;
                }
                case "return-date": {
                    const date = returnDate(values.get("date"), values.get("days"));
                    result.textContent = "Дата возврата: " + date.split("-").reverse().join(".");
                    break;
                }
            }
        } catch (error) { result.textContent = error.message; }
    });
}

for (const widget of document.querySelectorAll(".reading-timer")) {
    const input = widget.querySelector("input");
    const display = widget.querySelector(".timer-display");
    const status = widget.querySelector(".timer-status");
    const start = widget.querySelector('[data-timer-action="start"]');
    const pause = widget.querySelector('[data-timer-action="pause"]');
    const reset = widget.querySelector('[data-timer-action="reset"]');
    const timer = new ReadingTimer();
    let started = false;
    let finished = false;
    function render() {
        const seconds = Math.ceil(timer.left() / 1000);
        display.textContent = String(Math.floor(seconds / 60)).padStart(2, "0") + ":" + String(seconds % 60).padStart(2, "0");
        if (started && seconds === 0 && !finished) {
            timer.pause();
            finished = true;
            status.textContent = "Время чтения завершено. Можно сделать перерыв!";
        }
        input.disabled = started;
        start.disabled = timer.deadline !== null || finished;
        pause.disabled = timer.deadline === null;
    }
    start.addEventListener("click", () => {
        try {
            if (!started) timer.reset(integer(input.value, 1, 180));
            started = true;
            timer.start();
            status.textContent = "Таймер запущен.";
            render();
        } catch (error) { status.textContent = error.message; }
    });
    pause.addEventListener("click", () => { timer.pause(); status.textContent = "Пауза."; render(); });
    reset.addEventListener("click", () => {
        try {
            timer.reset(input.value);
            started = false;
            finished = false;
            status.textContent = "Таймер сброшен. Можно изменить длительность.";
            render();
        } catch (error) { status.textContent = error.message; }
    });
    input.addEventListener("change", () => {
        try { timer.reset(input.value); render(); } catch (error) { status.textContent = error.message; }
    });
    setInterval(render, 250);
    document.addEventListener("visibilitychange", render);
}

const weatherCodes = {
    0: "Ясно", 1: "Преимущественно ясно", 2: "Переменная облачность", 3: "Пасмурно",
    45: "Туман", 48: "Туман с изморозью", 51: "Слабая морось", 53: "Морось", 55: "Сильная морось",
    56: "Ледяная морось", 57: "Сильная ледяная морось", 61: "Небольшой дождь", 63: "Дождь", 65: "Сильный дождь",
    66: "Ледяной дождь", 67: "Сильный ледяной дождь", 71: "Небольшой снег", 73: "Снег", 75: "Сильный снег",
    77: "Снежные зёрна", 80: "Небольшой ливень", 81: "Ливень", 82: "Сильный ливень",
    85: "Снежный ливень", 86: "Сильный снежный ливень", 95: "Гроза", 96: "Гроза с градом", 99: "Гроза с сильным градом"
};
function paragraph(parent, text, className = "") {
    const p = document.createElement("p");
    p.textContent = text;
    if (className) p.className = className;
    parent.append(p);
}
for (const widget of document.querySelectorAll(".weather-widget, .exchange-widget")) {
    const status = widget.querySelector(".external-status");
    const result = widget.querySelector(".external-result");
    const button = widget.querySelector(".external-retry");
    async function load() {
        button.disabled = true;
        result.replaceChildren();
        status.textContent = "Загрузка данных…";
        try {
            if (widget.classList.contains("weather-widget")) {
                const lat = Number(widget.dataset.latitude), lon = Number(widget.dataset.longitude);
                if (!Number.isFinite(lat) || Math.abs(lat) > 90 || !Number.isFinite(lon) || Math.abs(lon) > 180) throw new Error("Invalid coordinates");
                const url = new URL("https://api.open-meteo.com/v1/forecast");
                url.search = new URLSearchParams({latitude: lat, longitude: lon, current: "temperature_2m,weather_code", timezone: "GMT"});
                const data = weatherData(await fetchJson(url));
                paragraph(result, `${data.temperature.toLocaleString("ru-RU")} °C`, "weather-temperature");
                paragraph(result, weatherCodes[data.code] ?? `Погодный код WMO: ${data.code}`);
                paragraph(result, "Данные на " + russianDate(data.time) + " (время твоего устройства).");
            } else {
                const data = exchangeData(await fetchJson("https://www.cbr-xml-daily.ru/daily_json.js"));
                for (const item of data.currencies) paragraph(result, `1 ${item.code} = ${item.value.toLocaleString("ru-RU", {maximumFractionDigits: 4})} ₽`);
                paragraph(result, "Дата курсов: " + data.time.toLocaleDateString("ru-RU", {timeZone: "Europe/Moscow"}) + ".");
            }
            status.textContent = "Данные получены. Актуальность указана ниже.";
        } catch {
            status.textContent = "Не удалось получить данные. Источник недоступен, ответ неполный или время ожидания истекло. Попробуй позже.";
        } finally { button.disabled = false; }
    }
    button.addEventListener("click", load);
    load();
}

let leafletPromise;
function loadLeaflet() {
    if (!leafletPromise) leafletPromise = new Promise((resolve, reject) => {
        const css = document.createElement("link");
        css.rel = "stylesheet";
        css.href = new URL("../vendor/leaflet/leaflet.css", import.meta.url).href;
        document.head.append(css);
        const script = document.createElement("script");
        script.src = new URL("../vendor/leaflet/leaflet.js", import.meta.url).href;
        const timeout = setTimeout(() => reject(new Error("Map library timeout")), 8000);
        script.onload = () => { clearTimeout(timeout); window.L ? resolve(window.L) : reject(new Error("Map missing")); };
        script.onerror = () => { clearTimeout(timeout); reject(new Error("Map unavailable")); };
        document.head.append(script);
    });
    return leafletPromise;
}
for (const widget of document.querySelectorAll(".map-widget")) {
    const status = widget.querySelector(".map-status");
    const locate = widget.querySelector(".map-locate");
    const reset = widget.querySelector(".map-reset");
    locate.disabled = true;
    reset.disabled = true;
    loadLeaflet().then(L => {
        const point = [Number(widget.dataset.latitude), Number(widget.dataset.longitude)];
        if (!Number.isFinite(point[0]) || !Number.isFinite(point[1]) || Math.abs(point[0]) > 90 || Math.abs(point[1]) > 180) throw new Error("Invalid coordinates");
        const map = L.map(widget.querySelector(".library-map"), {scrollWheelZoom: false}).setView(point, 12);
        const tiles = L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);
        let tileFailed = false, tileLoaded = false;
        const tileTimeout = setTimeout(() => {
            if (!tileLoaded) status.textContent = "Карта не загрузилась вовремя. Проверь интернет; координаты точки указаны выше.";
        }, 10000);
        tiles.on("tileerror", () => {
            tileFailed = true;
            status.textContent = "Часть карты недоступна. Проверь интернет или повтори позже.";
        });
        tiles.on("tileload", () => {
            tileLoaded = true;
            clearTimeout(tileTimeout);
            if (!tileFailed) status.textContent = "Карта загружена. Масштаб меняется кнопками + и −.";
        });
        const label = document.createElement("span");
        label.textContent = widget.dataset.demo === "true" ? "Демонстрационная точка библиотеки" : widget.dataset.name;
        L.circleMarker(point, {radius: 10, color: "#2558b7", fillOpacity: 0.8}).addTo(map).bindPopup(label).openPopup();
        let userMarker, accuracyCircle;
        locate.disabled = false;
        reset.disabled = false;
        reset.addEventListener("click", () => { map.setView(point, 12); });
        locate.addEventListener("click", () => {
            if (!navigator.geolocation || !window.isSecureContext) {
                status.textContent = "Геолокация недоступна. Открой сайт по HTTPS или на localhost.";
                return;
            }
            locate.disabled = true;
            status.textContent = "Ожидаем разрешение браузера и координаты…";
            navigator.geolocation.getCurrentPosition(position => {
                locate.disabled = false;
                const {latitude, longitude, accuracy} = position.coords;
                const userPoint = [latitude, longitude];
                if (userMarker) map.removeLayer(userMarker);
                if (accuracyCircle) map.removeLayer(accuracyCircle);
                userMarker = L.circleMarker(userPoint, {radius: 9, color: "#147447", fillOpacity: 0.9}).addTo(map).bindPopup("Твоё местоположение").openPopup();
                accuracyCircle = L.circle(userPoint, {radius: accuracy, color: "#147447", weight: 1}).addTo(map);
                map.setView(userPoint, 14);
                status.textContent = `Твоё местоположение: ${latitude.toFixed(5)}, ${longitude.toFixed(5)}. Точность около ${Math.round(accuracy)} м.`;
            }, error => {
                locate.disabled = false;
                status.textContent = error.code === 1 ? "Доступ к местоположению отклонён. Демонстрационная точка остаётся доступной."
                    : "Не удалось определить местоположение. Попробуй позже.";
            }, {enableHighAccuracy: false, timeout: 10000, maximumAge: 60000});
        });
    }).catch(() => { status.textContent = "Не удалось загрузить карту. Координаты точки указаны выше."; });
}

