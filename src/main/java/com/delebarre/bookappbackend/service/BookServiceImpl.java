package com.delebarre.bookappbackend.service;

import com.delebarre.bookappbackend.dto.BookCreateRequest;
import com.delebarre.bookappbackend.exception.BookNotFoundException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    @Override
    public Book createBook(BookCreateRequest bookCreateRequest) {
        Book book = new Book();
        book.setTitle(bookCreateRequest.getTitle());
        book.setAuthor(bookCreateRequest.getAuthor());

        // Fetch cover image from an external API (example using OpenLibrary API)
        // TODO should fetch based on title
        String coverUrl = "https://covers.openlibrary.org/b/isbn/" + bookCreateRequest.getIsbn() + "-L.jpg";
        byte[] coverImage = restTemplate.getForObject(coverUrl, byte[].class);
        book.setCoverImage(coverImage);

        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(String id, Book book) {
        Book existingBook = getBookById(id);
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setCoverImage(book.getCoverImage());
        return bookRepository.save(existingBook);
    }

    @Override
    public void deleteBook(String id) {
        bookRepository.deleteById(id);
    }
}