package com.delebarre.bookappbackend.controller;

import com.delebarre.bookappbackend.dto.BookCreateRequest;
import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081")
public class BookController {

    private final BookService bookService;

    @GetMapping("/all")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping
    public ResponseEntity<Book> getBookById(@RequestBody String id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createBook(@RequestBody BookCreateRequest request) {
        try {
            Book book = bookService.createBook(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(book);
        } catch (BookAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating book");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam String title,
            @RequestParam String author) {
        List<Book> books = bookService.searchBooks(title, author);
        return ResponseEntity.ok(books);
    }

}
