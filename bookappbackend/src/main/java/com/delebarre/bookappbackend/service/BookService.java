package com.delebarre.bookappbackend.service;

import com.delebarre.bookappbackend.model.Book;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

public interface BookService {
    List<Book> getAllBooks();

    Book getBookById(String id);

    public List<Book> getBooksBySubjectName(String subjectName);

    public List<Book> getBooksByContributorName(String contributorName);

    public Book createBook(String olid);

    Book updateBook(String id, Book book);

    ResponseEntity<?> deleteBook(String id);

    List<Book> searchBooks(String title, String author, String isbn);

    public Optional<byte[]> searchCover(String olid);

    void deleteAll();

}