package com.delebarre.bookappbackend.service;

import com.delebarre.bookappbackend.dto.BookDTO;
import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.exception.BookNotFoundException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    private static final String OPEN_LIBRARY_API = "https://openlibrary.org/search.json";

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
    public Book createBook(BookDTO bookDTO) {
        // Check if a book with the same title and author already exists
        if (bookRepository.existsByTitleAndAuthor(bookDTO.getTitle(), bookDTO.getAuthor())) {
            throw new BookAlreadyExistsException("A book with the same title and author already exists");
        }

        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());

        // Fetch additional metadata from Open Library API
        String encodedTitle = URLEncoder.encode(bookDTO.getTitle(), StandardCharsets.UTF_8);
        String encodedAuthor = URLEncoder.encode(bookDTO.getAuthor(), StandardCharsets.UTF_8);
        String searchUrl = String.format("https://openlibrary.org/works/%s.json",
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

                    // Set cover image
                    String coverId = firstBook.path("cover_i").asText();
                    if (!coverId.isEmpty()) {
                        String coverUrl = String.format("https://covers.openlibrary.org/b/id/%s-L.jpg", coverId);
                        byte[] coverImage = restTemplate.getForObject(coverUrl, byte[].class);
                        book.setCoverImage(coverImage);
                    }

                    // Set other metadata fields
                    book.setGenre(firstBook.path("subject").asText());
                    book.setISBN(firstBook.path("isbn").get(0).asText());
                    book.setPublicationDate(firstBook.path("first_publish_year").asText());
                    book.setDescription(firstBook.path("subtitle").asText());
                    book.setPublisher(firstBook.path("publisher").get(0).asText());
                    book.setLanguage(firstBook.path("language").get(0).asText());
                    book.setPageCount(firstBook.path("number_of_pages_median").asInt());
                    book.setFormat(firstBook.path("format").asText());
                    book.setSubjects(objectMapper.convertValue(firstBook.path("subject"), List.class));
                    book.setOpenLibraryId(firstBook.path("key").asText());
                    book.setContributors(objectMapper.convertValue(firstBook.path("author_name"), List.class));
                }
            } catch (JsonProcessingException e) {
                // Handle JSON parsing exception
                e.printStackTrace();
            }
        }

        return bookRepository.save(book);
    }


    @Override
    public Book createBook(String openLibraryId) {
        String encodedOpenLibraryId = URLEncoder.encode(openLibraryId, StandardCharsets.UTF_8);
        String searchUrl = String.format("https://openlibrary.org/api/books?bibkeys=OLID:%s&format=json&jscmd=data",
                openLibraryId);

        ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);
        String responseBody = response.getBody();

        if (responseBody == null || responseBody.isEmpty()) {
            throw new BookNotFoundException("No book found with OpenLibrary ID: " + openLibraryId);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode bookNode = rootNode.path("OLID:" + openLibraryId);

            if (bookNode.isMissingNode()) {
                throw new BookNotFoundException("No book found with OpenLibrary ID: " + openLibraryId);
            }

            Book book = new Book();
            book.setTitle(bookNode.path("title").asText());
            book.setAuthor(bookNode.path("authors").get(0).path("name").asText());

            if (bookRepository.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())) {
                throw new BookAlreadyExistsException("A book with the same title and author already exists");
            }

            // Set cover image
            String coverId = bookNode.path("cover").path("id").asText();
            if (!coverId.isEmpty()) {
                String coverUrl = String.format("https://covers.openlibrary.org/b/id/%s-L.jpg", coverId);
                byte[] coverImage = restTemplate.getForObject(coverUrl, byte[].class);
                book.setCoverImage(coverImage);
            }

            // Set other metadata fields
            book.setGenre(bookNode.path("subjects").asText());
            book.setISBN(bookNode.path("identifiers").path("isbn_13").get(0).asText());
            book.setPublicationDate(bookNode.path("publish_date").asText());
            book.setDescription(bookNode.path("subtitle").asText());
            book.setPublisher(bookNode.path("publishers").get(0).asText());
            book.setLanguage(bookNode.path("languages").get(0).path("key").asText().replace("/languages/", ""));
            book.setPageCount(bookNode.path("number_of_pages").asInt());
            book.setFormat(bookNode.path("physical_format").asText());
            book.setSubjects(objectMapper.convertValue(bookNode.path("subjects"), List.class));
            book.setOpenLibraryId(bookNode.path("key").asText());
            book.setContributors(objectMapper.convertValue(bookNode.path("authors"), List.class));
            return bookRepository.save(book);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON response", e);
        }
    }

    @Override
    public Book updateBook(String id, Book book) {
        try {
            Book existingBook = getBookById(id);
            existingBook.setTitle(book.getTitle());
            existingBook.setAuthor(book.getAuthor());
            existingBook.setCoverImage(book.getCoverImage());
            existingBook.setGenre(book.getGenre());
            existingBook.setISBN(book.getISBN());
            existingBook.setPublicationDate(book.getPublicationDate());
            existingBook.setDescription(book.getDescription());
            existingBook.setPublisher(book.getPublisher());
            existingBook.setLanguage(book.getLanguage());
            existingBook.setPageCount(book.getPageCount());
            existingBook.setFormat(book.getFormat());
            existingBook.setSubjects(book.getSubjects());
            existingBook.setOpenLibraryId(book.getOpenLibraryId());
            existingBook.setContributors(book.getContributors());

            return bookRepository.save(existingBook);
        } catch (BookNotFoundException e) {
            throw new BookNotFoundException("Book not found with id: " + id);
        } catch (Exception e) {
            throw new RuntimeException("Error updating book", e);
        }
    }

    @Override
    public ResponseEntity<?> deleteBook(String id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            bookRepository.deleteById(id);
            return ResponseEntity.ok(book);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found with id: " + id);
        }
    }

    @Override
    public List<Book> searchBooks(String title, String author) {
        String searchUrl = OPEN_LIBRARY_API + "?title=" + title + "&author=" + author;
        ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);
        String responseBody = response.getBody();

        List<Book> books = new ArrayList<>();
        if (responseBody != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode docs = rootNode.path("docs");

                for (JsonNode doc : docs) {
                    books.add(mapJsonToBook(doc, objectMapper));
                }
            } catch (JsonProcessingException e) {
                // Handle JSON parsing exception
                e.printStackTrace();
            }
        }
        return books;
    }

    private Book mapJsonToBook(JsonNode doc, ObjectMapper objectMapper) {
        Book book = new Book();

        book.setTitle(getJsonNodeText(doc, "title"));
        book.setAuthor(getJsonNodeArrayText(doc, "author_name"));
        book.setGenre(getJsonNodeText(doc, "subject"));
        book.setISBN(getJsonNodeArrayText(doc, "isbn"));
        book.setPublicationDate(getJsonNodeText(doc, "first_publish_year"));
        book.setDescription(getJsonNodeText(doc, "subtitle"));
        book.setPublisher(getJsonNodeArrayText(doc, "publisher"));
        book.setLanguage(getJsonNodeArrayText(doc, "language"));
        book.setPageCount(getJsonNodeInt(doc, "number_of_pages_median"));
        book.setFormat(getJsonNodeText(doc, "format"));
        book.setSubjects(objectMapper.convertValue(doc.path("subject"), List.class));
        book.setOpenLibraryId(getJsonNodeText(doc, "key"));
        book.setContributors(objectMapper.convertValue(doc.path("author_name"), List.class));

        return book;
    }

    private String getJsonNodeText(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.path(fieldName).asText() : null;
    }

    private String getJsonNodeArrayText(JsonNode node, String fieldName) {
        return node.has(fieldName) && node.path(fieldName).isArray() && node.path(fieldName).size() > 0
                ? node.path(fieldName).get(0).asText()
                : null;
    }

    private int getJsonNodeInt(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.path(fieldName).asInt() : 0;
    }

    @Override
    public Optional<byte[]> searchCover(String openLibraryId) {
        String coverUrl = String.format("https://covers.openlibrary.org/b/id/%s-L.jpg", openLibraryId);
        try {
            byte[] coverImage = restTemplate.getForObject(coverUrl, byte[].class);
            return Optional.ofNullable(coverImage);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
