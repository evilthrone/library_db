import test from "node:test";
import assert from "node:assert/strict";
import {validIsbn, readingPlan, returnDate, ReadingTimer, fetchJson, weatherData, exchangeData} from "../../main/resources/static/js/tools-core.mjs";

test("ISBN handles both lengths, X, formatting and wrong checksums", () => {
    for (const value of ["0-306-40615-2", "0 8044 2957 x", "978-0-306-40615-7"]) assert.equal(validIsbn(value), true, value);
    for (const value of ["9780306406158", "123", "", "978abcdefghij", "1234567890128"]) assert.equal(validIsbn(value), false, value);
});
test("reading plan rounds up and rejects invalid input", () => {
    assert.equal(readingPlan(101, 10, 10), 10);
    assert.equal(readingPlan(100, 100, 10), 0);
    for (const values of [[100,101,1],[0,0,1],[100,0,0],[100,-1,1],[100,0,1.5],["",0,1]]) {
        assert.throws(() => readingPlan(...values));
    }
});
test("return date uses calendar days across leap years and rejects impossible dates", () => {
    assert.equal(returnDate("2024-02-28", 2), "2024-03-01");
    assert.equal(returnDate("2026-12-31", 1), "2027-01-01");
    assert.equal(returnDate("2026-03-28", 2), "2026-03-30");
    assert.throws(() => returnDate("2025-02-29", 1));
    assert.throws(() => returnDate("2026-01-01", 0));
    assert.throws(() => returnDate("9998-12-31", 36500));
});
test("timer pauses, resumes, resets and survives delayed callbacks", () => {
    let now = 1000;
    const timer = new ReadingTimer(() => now);
    timer.reset(1);
    timer.start(); now += 10000;
    assert.equal(timer.left(), 50000);
    timer.pause(); now += 40000;
    assert.equal(timer.left(), 50000);
    timer.start(); now += 60000;
    assert.equal(timer.left(), 0);
    timer.reset(2);
    assert.equal(timer.left(), 120000);
    assert.throws(() => timer.reset(-1));
});
test("external data parsing rejects partial payloads and respects currency nominal", () => {
    assert.equal(weatherData({current:{temperature_2m:12,weather_code:3,time:"2026-09-17T12:00"}}).temperature, 12);
    assert.throws(() => weatherData({current:{temperature_2m:null}}));
    assert.throws(() => exchangeData({}));
    const item = {Value:100,Nominal:10};
    const rates = exchangeData({Date:"2026-09-17T00:00:00+03:00",Valute:{USD:item,EUR:item,CNY:item}});
    assert.equal(rates.currencies[0].value, 10);
});
test("external requests handle success, failure and timeout without live APIs", async () => {
    assert.deepEqual(await fetchJson("https://example.test", async () => ({ok:true,json:async()=>({ok:1})})), {ok:1});
    await assert.rejects(fetchJson("https://example.test", async () => ({ok:false})));
    await assert.rejects(fetchJson("https://example.test", async () => { throw new Error("offline"); }));
    await assert.rejects(fetchJson("https://example.test", (_url,{signal}) => new Promise((_resolve,reject) => {
        signal.addEventListener("abort", () => reject(new Error("timeout")));
    }), 10));
});
