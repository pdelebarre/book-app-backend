package com.delebarre.bookappbackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.delebarre.bookappbackend.model.Contributor;

import java.util.Optional;

public interface ContributorRepository extends MongoRepository<Contributor, String> {
    Optional<Contributor> findByName(String name);

}
