package com.delebarre.bookappbackend.controller;

import com.delebarre.bookappbackend.dto.BookDTO;
import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @CrossOrigin(origins = "*")
    @GetMapping("/all")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @CrossOrigin(origins = "*")
    @GetMapping
    public ResponseEntity<Book> getBookById(@RequestParam String id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @CrossOrigin(origins = "*")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createBook(
            @RequestBody(required = false) BookDTO bookDTO,
            @RequestParam(required = false) String openLibraryId) {
        try {
            Book book;
            if (bookDTO != null) {
                book = bookService.createBook(bookDTO);
            } else if (openLibraryId != null) {
                book = bookService.createBook(openLibraryId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request data");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(book);
        } catch (BookAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating book");
        }
    }

    @CrossOrigin(origins = "*")
    @PutMapping
    public ResponseEntity<Book> updateBook(@RequestParam String id, @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping
    public ResponseEntity<?> deleteBook(@RequestParam String id) {
        return bookService.deleteBook(id);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam String title,
            @RequestParam String author) {
        List<Book> books = bookService.searchBooks(title, author);
        return ResponseEntity.ok(books);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/searchCover")
    public ResponseEntity<Optional<byte[]>> searchBooks(
            @RequestParam String openLibraryId) {
        Optional<byte[]> cover = bookService.searchCover(openLibraryId);
        return ResponseEntity.ok(cover);
    }

}
