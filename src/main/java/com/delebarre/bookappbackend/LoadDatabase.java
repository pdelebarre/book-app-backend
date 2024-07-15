package com.delebarre.bookappbackend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.delebarre.bookappbackend.entities.Book;
import com.delebarre.bookappbackend.repositories.BookRepository;

@Configuration
public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(BookRepository repository) {
        return args -> {
            repository.save(new Book(null, "The Great Gatsby", "F. Scott Fitzgerald"));
            repository.save(new Book(null, "1984", "George Orwell"));
            repository.save(new Book(null, "To Kill a Mockingbird", "Harper Lee"));
        };
    }
}
