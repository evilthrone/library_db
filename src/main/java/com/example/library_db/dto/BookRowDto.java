package com.example.library_db.dto;

public class BookRowDto {
    private Long id;
    private String title;
    private String authorName;
    private String publisherName;
    private Integer publicationYear;
    private Integer copiesCount;

    public BookRowDto() {
    }

    public BookRowDto(Long id, String title, String authorName, String publisherName, Integer publicationYear, Integer copiesCount) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.publisherName = publisherName;
        this.publicationYear = publicationYear;
        this.copiesCount = copiesCount;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public Integer getCopiesCount() {
        return copiesCount;
    }
}