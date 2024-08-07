package com.delebarre.bookappbackend.config;

import com.delebarre.bookappbackend.dto.BookDTO;
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
        BookDTO[] books = {
                new BookDTO("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565"),
                new BookDTO("To Kill a Mockingbird", "Harper Lee", "9780446310789"),
                new BookDTO("1984", "George Orwell", "9780451524935")
        };

        for (BookDTO bookRequest : books) {
            try {
                bookService.createBook(bookRequest);
            } catch (BookAlreadyExistsException e) {
                // Log or handle the case where the book already exists
                System.out.println("Book already exists: " + bookRequest.getTitle());
            }
        }
    }
}