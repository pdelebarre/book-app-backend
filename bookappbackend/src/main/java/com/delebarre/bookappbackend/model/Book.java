package com.delebarre.bookappbackend.model;

import lombok.Data;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "books")
public class Book {
    @Id
    private String id;
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
    private List<String> contributors; // Other contributors like editors or illustrators
}
