package com.delebarre.bookappbackend.repository;

import com.delebarre.bookappbackend.model.Book;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, String> {
    boolean existsByTitleAndAuthor(String title, String author);

    boolean existsByOpenLibraryId(String openLibraryId);

    boolean existsByIsbn(String isbn);

    List<Book> findBySubjectsId(String subjectId);

    List<Book> findByContributorsId(String contributorId);

}