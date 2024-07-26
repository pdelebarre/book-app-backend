package com.delebarre.bookappbackend.repository;

import com.delebarre.bookappbackend.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, String> {
}