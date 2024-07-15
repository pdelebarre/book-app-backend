package com.delebarre.bookcollection.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delebarre.bookcollection.entities.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
