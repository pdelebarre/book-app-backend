package com.delebarre.bookappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookCreateRequest {
    private String title;
    private String author;
    private String isbn;
}