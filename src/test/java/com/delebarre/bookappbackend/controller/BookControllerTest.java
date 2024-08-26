package com.delebarre.bookappbackend.controller;

import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Annotate this class to specify that it's a web layer test
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;
    
    @MockBean
    private RestTemplateBuilder restTemplateBuilder;     

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId("1");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        // Other initializations if necessary
    }

    @Test
    void testGetAllBooks() throws Exception {
        List<Book> books = Collections.singletonList(book);
        when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/api/books/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(book.getId())))
                .andExpect(jsonPath("$[0].title", is(book.getTitle())))
                .andExpect(jsonPath("$[0].author", is(book.getAuthor())))
                .andExpect(jsonPath("$[0].isbn", is(book.getIsbn())));
    }

    @Test
    void testGetBookById() throws Exception {
        when(bookService.getBookById(anyString())).thenReturn(book);

        mockMvc.perform(get("/api/books")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book.getId())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.author", is(book.getAuthor())))
                .andExpect(jsonPath("$.isbn", is(book.getIsbn())));
    }

    @Test
    void testCreateBook() throws Exception {
        when(bookService.createBook(anyString())).thenReturn(book);

        mockMvc.perform(post("/api/books")
                .param("olid", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(book.getId())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.author", is(book.getAuthor())))
                .andExpect(jsonPath("$.isbn", is(book.getIsbn())));
    }

    @Test
    void testCreateBookAlreadyExists() throws Exception {
        when(bookService.createBook(anyString())).thenThrow(new BookAlreadyExistsException("Book already exists"));

        mockMvc.perform(post("/api/books")
                .param("olid", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("Book already exists"));
    }

    @Test
    void testUpdateBook() throws Exception {
        when(bookService.updateBook(anyString(), any(Book.class))).thenReturn(book);

        mockMvc.perform(put("/api/books")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Updated Book\", \"author\": \"Updated Author\", \"isbn\": \"0987654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book.getId())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.author", is(book.getAuthor())))
                .andExpect(jsonPath("$.isbn", is(book.getIsbn())));
    }

    // @Test
    // void testDeleteBook() throws Exception {
    //     doNothing().when(bookService).deleteBook(anyString());

    //     mockMvc.perform(delete("/api/books")
    //             .param("id", "1")
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isOk());
    // }

    @Test
    void testDeleteAllBooks() throws Exception {
        doNothing().when(bookService).deleteAll();

        mockMvc.perform(delete("/api/books/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // @Test
    // void testSearchBooks() throws Exception {
    //     List<Book> books = Collections.singletonList(book);
    //     when(bookService.searchBooks(anyString(), anyString(), anyString())).thenReturn(books);

    //     mockMvc.perform(get("/api/books/search")
    //             .param("title", "Test")
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$", hasSize(1)))
    //             .andExpect(jsonPath("$[0].id", is(book.getId())))
    //             .andExpect(jsonPath("$[0].title", is(book.getTitle())))
    //             .andExpect(jsonPath("$[0].author", is(book.getAuthor())))
    //             .andExpect(jsonPath("$[0].isbn", is(book.getIsbn())));
    // }

    // @Test
    // void testSearchCover() throws Exception {
    //     Optional<byte[]> cover = Optional.of("coverImage".getBytes());
    //     when(bookService.searchCover(anyString())).thenReturn(cover);

    //     mockMvc.perform(get("/api/books/searchCover")
    //             .param("olid", "1")
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isOk())
    //             .andExpect(content().string("coverImage"));
    // }
}
