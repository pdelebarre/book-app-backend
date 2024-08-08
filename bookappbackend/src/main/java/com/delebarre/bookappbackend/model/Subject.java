package com.delebarre.bookappbackend.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "subjects")
@AllArgsConstructor
@NoArgsConstructor
public class Subject {
    @MongoId
    private String id;
    private String name;
    private String url;

    public Subject(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
