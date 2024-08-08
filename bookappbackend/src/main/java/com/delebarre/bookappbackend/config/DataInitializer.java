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
                // new BookDTO("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565"),
                // new BookDTO("To Kill a Mockingbird", "Harper Lee", "9780446310789"),
                // new BookDTO("1984", "George Orwell", "9780451524935")
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
