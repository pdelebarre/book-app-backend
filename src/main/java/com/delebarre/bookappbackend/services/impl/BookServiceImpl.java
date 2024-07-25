package com.delebarre.bookappbackend.services.impl;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.delebarre.bookappbackend.entities.Book;
import com.delebarre.bookappbackend.repositories.BookRepository;
import com.delebarre.bookappbackend.services.BookService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;
        
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    // Method example with logging
    public void someMethod() {
        logger.info("Method someMethod called");
        try {
            // Business logic
            logger.debug("Processing data...");
        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage());
        }
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book createBook(Book book) throws IOException {
        book.setCoverImage(fetchBookCoverImage(fetchBookCoverUrl(book.getTitle())));
        logger.debug("saving book...");
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Long id, Book bookDetails) throws IOException {
        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setCoverImage(fetchBookCoverImage(fetchBookCoverUrl(book.getTitle())));
        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(Long id) {
        logger.debug("deleting book " + id);
        bookRepository.deleteById(id);
    }

    private String fetchBookCoverUrl(String title) throws IOException {
        String url = "https://openlibrary.org/search.json?title=" + title;
        Document doc = Jsoup.connect(url).ignoreContentType(true).get();
        Element cover = doc.select("cover_i").first();
        if (cover != null) {
            String coverId = cover.text();
            return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
        }
        return null;
    }

    private String fetchBookCoverImage(String coverUrl) throws IOException {
        if (coverUrl == null)
            return null;
        URL url = new URL(coverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        logger.debug("fetching cover...");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        inputStream.close();
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }
}
