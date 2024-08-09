package com.delebarre.bookappbackend.config;

import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
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
        // List of books to add
        String[] olids = {
                "OL51017267M", "OL33899062M", "OL24981637M"            
        };

        for (String olid : olids) { // for (BookDTO bookRequest : books) {
            try {
                bookService.createBook(olid);
            } catch (BookAlreadyExistsException e) {
                // Log or handle the case where the book already exists
                System.out.println("Book already exists: " + olid);
            }
        }
    }
}
