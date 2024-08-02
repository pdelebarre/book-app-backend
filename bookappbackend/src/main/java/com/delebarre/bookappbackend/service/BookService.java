package com.delebarre.bookappbackend.service;

import com.delebarre.bookappbackend.dto.BookCreateRequest;
import com.delebarre.bookappbackend.model.Book;

import java.util.List;

public interface BookService {
    List<Book> getAllBooks();

    Book getBookById(String id);

    Book createBook(BookCreateRequest bookCreateRequest);

    Book updateBook(String id, Book book);

    void deleteBook(String id);

    List<Book> searchBooks(String title, String author);

}