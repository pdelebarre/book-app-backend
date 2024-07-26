package com.delebarre.bookappbackend.config;

import com.delebarre.bookappbackend.dto.BookCreateRequest;
import com.delebarre.bookappbackend.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BookService bookService;

    @Override
    public void run(String... args) {
        // Add test data
        bookService.createBook(new BookCreateRequest("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565"));
        bookService.createBook(new BookCreateRequest("To Kill a Mockingbird", "Harper Lee", "9780446310789"));
        bookService.createBook(new BookCreateRequest("1984", "George Orwell", "9780451524935"));
    }
}