package com.delebarre.bookappbackend.service;

import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.exception.BookNotFoundException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.model.Contributor;
import com.delebarre.bookappbackend.model.Subject;
import com.delebarre.bookappbackend.repository.BookRepository;
import com.delebarre.bookappbackend.repository.ContributorRepository;
import com.delebarre.bookappbackend.repository.SubjectRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final SubjectRepository subjectRepository;
    private final ContributorRepository contributorRepository;
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
    public List<Book> getBooksBySubjectName(String subjectName) {
        return subjectRepository.findByName(subjectName)
                .map(subject -> bookRepository.findBySubjectsId(subject.getId()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Book> getBooksByContributorName(String contributorName) {
        return contributorRepository.findByName(contributorName)
                .map(contributor -> bookRepository.findByContributorsId(contributor.getId()))
                .orElse(Collections.emptyList());
    }

    // @Override
    // public Book createBook(BookDTO bookDTO) {
    // // Check if a book with the same title and author already exists
    // if (bookRepository.existsByTitleAndAuthor(bookDTO.getTitle(),
    // bookDTO.getAuthor())) {
    // throw new BookAlreadyExistsException("A book with the same title and author
    // already exists");
    // }

    // Book book = new Book();
    // book.setTitle(bookDTO.getTitle());
    // book.setAuthor(bookDTO.getAuthor());

    // // Fetch additional metadata from Open Library API
    // String encodedTitle = URLEncoder.encode(bookDTO.getTitle(),
    // StandardCharsets.UTF_8);
    // String encodedAuthor = URLEncoder.encode(bookDTO.getAuthor(),
    // StandardCharsets.UTF_8);
    // String searchUrl = String.format("https://openlibrary.org/works/%s.json",
    // encodedAuthor);

    // ResponseEntity<String> response = restTemplate.getForEntity(searchUrl,
    // String.class);
    // String responseBody = response.getBody();

    // if (responseBody != null) {
    // try {
    // ObjectMapper objectMapper = new ObjectMapper();
    // JsonNode rootNode = objectMapper.readTree(responseBody);
    // JsonNode docs = rootNode.path("docs");

    // if (docs.isArray() && docs.size() > 0) {
    // JsonNode firstBook = docs.get(0);

    // // Set cover image
    // String coverId = firstBook.path("cover_i").asText();
    // if (!coverId.isEmpty()) {
    // String coverUrl =
    // String.format("https://covers.openlibrary.org/b/id/%s-L.jpg", coverId);
    // byte[] coverImage = restTemplate.getForObject(coverUrl, byte[].class);
    // book.setCoverImage(coverImage);
    // }

    // // Set other metadata fields
    // book.setGenre(firstBook.path("subject").asText());
    // book.setISBN(firstBook.path("isbn").get(0).asText());
    // book.setPublicationDate(firstBook.path("first_publish_year").asText());
    // book.setDescription(firstBook.path("subtitle").asText());
    // book.setPublisher(firstBook.path("publisher").get(0).asText());
    // book.setLanguage(firstBook.path("language").get(0).asText());
    // book.setPageCount(firstBook.path("number_of_pages_median").asInt());
    // book.setFormat(firstBook.path("format").asText());
    // book.setSubjects(objectMapper.convertValue(firstBook.path("subject"),
    // List.class));
    // book.setOpenLibraryId(firstBook.path("key").asText());
    // book.setContributors(objectMapper.convertValue(firstBook.path("author_name"),
    // List.class));
    // }
    // } catch (JsonProcessingException e) {
    // // Handle JSON parsing exception
    // throw new BookNotFoundException("Book not found in OpenLibrary " +
    // bookDTO.getTitle());
    // }
    // }

    // return bookRepository.save(book);
    // }

    @Override
    public Book createBook(String olid) {
        if (bookRepository.existsByOpenLibraryId(olid)) {
            throw new BookAlreadyExistsException("A book with the same title and author already exists");
        }

        String searchUrl = String.format("https://openlibrary.org/api/books?bibkeys=OLID:%s&format=json&jscmd=data",
                olid);
        ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);
        String responseBody = response.getBody();

        if (responseBody == null || responseBody.isEmpty()) {
            throw new BookNotFoundException("No book found with OLID: " + olid);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bookNode = objectMapper.readTree(responseBody).path("OLID:" + olid);
            if (bookNode.isMissingNode()) {
                throw new BookNotFoundException("No book found with OLID: " + olid);
            }

            Book book = new Book();
            book.setTitle(getTextNodeValue(bookNode, "title"));
            book.setAuthor(getTextNodeValue(bookNode.path("authors").get(0), "name"));
            book.setCoverImage(fetchCoverImage(bookNode));
            book.setGenre(getArrayNodeFirstValue(bookNode, "subjects"));
            book.setPublisher(getArrayNodeFirstValue(bookNode, "publishers"));
            book.setPublicationDate(getTextNodeValue(bookNode, "publish_date"));
            book.setDescription(getTextNodeValue(bookNode, "subtitle"));
            book.setLanguage(getLanguageCode(bookNode));
            book.setPageCount(getIntNodeValue(bookNode, "number_of_pages"));
            book.setFormat(getTextNodeValue(bookNode, "physical_format"));
            List<Subject> subjects = parseSubjects(bookNode.path("subjects"));
            book.setSubjects(subjects);

            List<Contributor> contributors = parseContributors(bookNode.path("contributors"));
            book.setContributors(contributors);

            book.setContributors(objectMapper.convertValue(bookNode.path("authors"), List.class));
            book.setOpenLibraryId(olid);
            // Set ISBN from the identifiers node
            String isbn = getArrayNodeFirstValue(bookNode.path("identifiers"), "isbn_13");
            book.setIsbn(isbn);
            return bookRepository.save(book);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON response", e);
        }
    }

    private String getTextNodeValue(JsonNode node, String fieldName) {
        return node.path(fieldName).asText(null);
    }

    private Integer getIntNodeValue(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asInt() : null;
    }

    private String getArrayNodeFirstValue(JsonNode node, String fieldName) {
        JsonNode arrayNode = node.path(fieldName);
        return arrayNode.isArray() && arrayNode.size() > 0 ? arrayNode.get(0).asText(null) : null;
    }

    private String getLanguageCode(JsonNode bookNode) {
        return bookNode.path("languages").isArray() && bookNode.path("languages").size() > 0
                ? bookNode.path("languages").get(0).path("key").asText(null).replace("/languages/", "")
                : null;
    }

    private byte[] fetchCoverImage(JsonNode bookNode) {
        String coverUrl = getTextNodeValue(bookNode.path("cover"), "medium");
        if (coverUrl != null) {
            return restTemplate.getForObject(coverUrl, byte[].class);
        }
        return new byte[0];
    }

    private List<Subject> parseSubjects(JsonNode subjectsNode) {
        List<Subject> subjects = new ArrayList<>();
        if (subjectsNode.isArray()) {
            for (JsonNode subjectNode : subjectsNode) {
                String name = getTextNodeValue(subjectNode, "name");
                String url = getTextNodeValue(subjectNode, "url");
                if (name != null && url != null) {
                    Subject subject = subjectRepository.findByName(name).orElse(new Subject(name, url));
                    subjectRepository.save(subject);
                    subjects.add(subject);
                }
            }
        }
        return subjects;
    }

    private List<Contributor> parseContributors(JsonNode contributorsNode) {
        List<Contributor> contributors = new ArrayList<>();
        if (contributorsNode.isArray()) {
            for (JsonNode contributorNode : contributorsNode) {
                String name = getTextNodeValue(contributorNode, "name");
                String url = getTextNodeValue(contributorNode, "url");
                if (name != null && url != null) {
                    Contributor contributor = contributorRepository.findByName(name).orElse(new Contributor(name, url));
                    contributorRepository.save(contributor);
                    contributors.add(contributor);
                }
            }
        }
        return contributors;
    }

    @Override
    public Book updateBook(String id, Book book) {
        try {
            Book existingBook = getBookById(id);
            existingBook.setTitle(book.getTitle());
            existingBook.setAuthor(book.getAuthor());
            existingBook.setCoverImage(book.getCoverImage());
            existingBook.setGenre(book.getGenre());
            existingBook.setIsbn(book.getIsbn());
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
    public List<Book> searchBooks(String title, String author, String isbn) {

        if (!StringUtils.hasText(title) && !StringUtils.hasText(author) && !StringUtils.hasText(isbn)) {
            throw new IllegalArgumentException("At least one search parameter must be provided");
        }
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(OPEN_LIBRARY_API);

        if (StringUtils.hasText(title)) {
            uriBuilder.queryParam("title", title);
        }
        if (StringUtils.hasText(author)) {
            uriBuilder.queryParam("author", author);
        }
        if (StringUtils.hasText(isbn)) {
            uriBuilder.queryParam("isbn", isbn);
        }

        String responseBody = restTemplate.getForObject(uriBuilder.toUriString(), String.class);

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
        book.setIsbn(getJsonNodeArrayText(doc, "isbn"));
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

    @Override
    public void deleteAll() {
        bookRepository.deleteAll();
    }

}
