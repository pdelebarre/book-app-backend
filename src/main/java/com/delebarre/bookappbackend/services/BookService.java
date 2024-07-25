package com.delebarre.bookappbackend.services;

import java.io.IOException;
import java.util.List;

import com.delebarre.bookappbackend.entities.Book;

public interface BookService {
    List<Book> getAllBooks();

    Book createBook(Book book) throws IOException;

    Book updateBook(Long id, Book bookDetails) throws IOException;

    void deleteBook(Long id);
}
