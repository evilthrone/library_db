package com.example.library_db.dto;

public class GenreAggDto {
    private String genreName;
    private Integer booksCount;
    private Integer totalCopies;

    public GenreAggDto() {
    }

    public GenreAggDto(String genreName, Integer booksCount, Integer totalCopies) {
        this.genreName = genreName;
        this.booksCount = booksCount;
        this.totalCopies = totalCopies;
    }

    public String getGenreName() {
        return genreName;
    }

    public Integer getBooksCount() {
        return booksCount;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }
}