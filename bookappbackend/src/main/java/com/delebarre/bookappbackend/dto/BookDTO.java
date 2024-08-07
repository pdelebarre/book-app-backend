package com.delebarre.bookappbackend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookDTO {    
    private String title;
    private String author;
    private byte[] coverImage;
    private String genre;
    private String ISBN;
    private String publicationDate; // Consider using Date type if dates are required
    private String description;
    private String publisher;
    private String language;
    private Integer pageCount;
    private String format;
    private List<String> subjects; // List of genres or categories
    private String openLibraryId; // Identifier from Open Library
    private String goodreadsId; // Identifier from Goodreads if available
    private List<String> contributors;

    public BookDTO(String title, String author, String ISBN) {
        this.title = title;
        this.author = author;
        this.ISBN = ISBN;
    }
}