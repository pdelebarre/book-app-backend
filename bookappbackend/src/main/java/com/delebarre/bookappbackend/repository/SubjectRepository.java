package com.delebarre.bookappbackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.delebarre.bookappbackend.model.Subject;

import java.util.Optional;

public interface SubjectRepository extends MongoRepository<Subject, String> {
    Optional<Subject> findByName(String name);

}
