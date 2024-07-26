package com.delebarre.bookappbackend.service;

import com.delebarre.bookappbackend.dto.BookCreateRequest;
import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.exception.BookNotFoundException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        // Check if a book with the same title and author already exists
        if (bookRepository.existsByTitleAndAuthor(bookCreateRequest.getTitle(), bookCreateRequest.getAuthor())) {
            throw new BookAlreadyExistsException("A book with the same title and author already exists");
        }

        
        Book book = new Book();
        book.setTitle(bookCreateRequest.getTitle());
        book.setAuthor(bookCreateRequest.getAuthor());

        // Fetch cover image from Open Library API based on title and author
        String encodedTitle = URLEncoder.encode(bookCreateRequest.getTitle(), StandardCharsets.UTF_8);
        String encodedAuthor = URLEncoder.encode(bookCreateRequest.getAuthor(), StandardCharsets.UTF_8);
        String searchUrl = String.format("https://openlibrary.org/search.json?title=%s&author=%s", encodedTitle,
                encodedAuthor);

        ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);
        String responseBody = response.getBody();

        if (responseBody != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode docs = rootNode.path("docs");

                if (docs.isArray() && docs.size() > 0) {
                    JsonNode firstBook = docs.get(0);
                    String coverId = firstBook.path("cover_i").asText();

                    if (!coverId.isEmpty()) {
                        String coverUrl = String.format("https://covers.openlibrary.org/b/id/%s-L.jpg", coverId);
                        byte[] coverImage = restTemplate.getForObject(coverUrl, byte[].class);
                        book.setCoverImage(coverImage);
                    }
                }
            } catch (JsonProcessingException e) {
                // Handle JSON parsing exception
                e.printStackTrace();
            }
        }

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