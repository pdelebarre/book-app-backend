package com.delebarre.bookappbackend.service;

import com.delebarre.bookappbackend.dto.BookDTO;
import com.delebarre.bookappbackend.model.Book;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

public interface BookService {
    List<Book> getAllBooks();

    Book getBookById(String id);

    Book createBook(BookDTO bookCreateRequest);
    
    public Book createBook(String openLibraryId);

    Book updateBook(String id, Book book);

    ResponseEntity<?> deleteBook(String id);

    List<Book> searchBooks(String title, String author);

    public Optional<byte[]> searchCover(String openLibraryId);

}