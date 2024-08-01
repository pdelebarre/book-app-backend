package com.delebarre.bookappbackend.controller;

import com.delebarre.bookappbackend.dto.BookCreateRequest;
import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Api(value = "Book Management System", description = "Operations pertaining to books in the library")
public class BookController {

    private final BookService bookService;

    @ApiOperation(value = "View a list of available books", response = List.class)
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @ApiOperation(value = "Get a book by Id", response = Book.class)
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable String id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @ApiOperation(value = "Add a new book")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Book successfully created"),
            @ApiResponse(code = 409, message = "Book already exists"),
            @ApiResponse(code = 400, message = "Error creating book")
    })
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

    @ApiOperation(value = "Update an existing book")
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }

    @ApiOperation(value = "Delete a book")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Book successfully deleted"),
            @ApiResponse(code = 404, message = "Book not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
    }

    @ApiOperation(value = "Search books by title and author", response = List.class)
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam String title,
            @RequestParam String author) {
        List<Book> books = bookService.searchBooks(title, author);
        return ResponseEntity.ok(books);
    }

    @ApiOperation(value = "Handle BookAlreadyExistsException")
    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<String> handleBookAlreadyExistsException(BookAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
