package com.delebarre.bookappbackend.model;

import lombok.Data;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "books")
public class Book {
    @MongoId
    private String id;
    private String title;
    private String author;
    private byte[] coverImage;
    private String genre;
    private String isbn;
    private String publicationDate; // Consider using Date type if dates are required
    private String description;
    private String publisher;
    private String language;
    private Integer pageCount;
    private String format;
    @DBRef
    private List<Subject> subjects;// List of genres or categories
    private String openLibraryId; // Identifier from Open Library
    private String goodreadsId; // Identifier from Goodreads if available
    private List<Contributor> contributors; // Other contributors like editors or illustrators
}
