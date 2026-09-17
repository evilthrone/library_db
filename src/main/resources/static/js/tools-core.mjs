export function integer(value, min, max) {
    if (String(value).trim() === "") throw new Error("Заполни все поля.");
    const number = Number(value);
    if (!Number.isSafeInteger(number) || number < min || number > max) {
        throw new Error(`Введи целое число от ${min} до ${max}.`);
    }
    return number;
}

export function validIsbn(value) {
    const code = value.replace(/[\s-]/g, "").toUpperCase();
    if (/^\d{9}[\dX]$/.test(code)) {
        return [...code].reduce((sum, char, i) => sum + (char === "X" ? 10 : Number(char)) * (10 - i), 0) % 11 === 0;
    }
    if (/^97[89]\d{10}$/.test(code)) {
        return [...code].reduce((sum, char, i) => sum + Number(char) * (i % 2 === 0 ? 1 : 3), 0) % 10 === 0;
    }
    return false;
}

export function readingPlan(pages, read, days) {
    const total = integer(pages, 1, 1000000);
    const done = integer(read, 0, 1000000);
    const remainingDays = integer(days, 1, 36500);
    if (done > total) throw new Error("Прочитанных страниц не может быть больше объёма книги.");
    return Math.ceil((total - done) / remainingDays);
}

export function returnDate(date, days) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date) || date < "1900-01-01" || date > "9998-12-31") {
        throw new Error("Укажи корректную дату выдачи от 1900 до 9998 года.");
    }
    const value = new Date(date + "T00:00:00Z");
    if (!Number.isFinite(value.getTime()) || value.toISOString().slice(0, 10) !== date) throw new Error("Такой даты не существует.");
    value.setUTCDate(value.getUTCDate() + integer(days, 1, 36500));
    if (value.getUTCFullYear() > 9999) throw new Error("Результат выходит за допустимый диапазон дат.");
    return value.toISOString().slice(0, 10);
}

export class ReadingTimer {
    constructor(now = () => Date.now()) { this.now = now; this.reset(20); }
    reset(minutes) {
        this.remaining = integer(minutes, 1, 180) * 60000;
        this.deadline = null;
    }
    start() {
        if (this.deadline === null && this.remaining > 0) this.deadline = this.now() + this.remaining;
    }
    pause() { this.remaining = this.left(); this.deadline = null; }
    left() { return Math.max(0, this.deadline === null ? this.remaining : this.deadline - this.now()); }
}

export async function fetchJson(url, fetcher = fetch, timeoutMs = 8000) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), timeoutMs);
    try {
        const response = await fetcher(url, {signal: controller.signal, credentials: "omit"});
        if (!response.ok) throw new Error("Источник вернул ошибку.");
        return await response.json();
    } finally { clearTimeout(timeout); }
}

export function weatherData(data) {
    const current = data?.current;
    if (!current || !Number.isFinite(current.temperature_2m) || !Number.isInteger(current.weather_code) ||
        !/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(current.time) || !Number.isFinite(Date.parse(current.time + "Z"))) {
        throw new Error("Источник вернул неполные данные погоды.");
    }
    return {temperature: current.temperature_2m, code: current.weather_code, time: current.time + "Z"};
}

export function exchangeData(data) {
    const time = new Date(data?.Date);
    if (!Number.isFinite(time.getTime())) throw new Error("Нет даты курса.");
    const currencies = ["USD", "EUR", "CNY"].map(code => {
        const item = data?.Valute?.[code];
        if (!item || !Number.isFinite(item.Value) || item.Value <= 0 ||
            !Number.isFinite(item.Nominal) || item.Nominal <= 0) throw new Error("Источник вернул неполные курсы.");
        return {code, value: item.Value / item.Nominal};
    });
    return {time, currencies};
}

