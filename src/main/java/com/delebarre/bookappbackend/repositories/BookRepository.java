package com.delebarre.bookappbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delebarre.bookappbackend.entities.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
