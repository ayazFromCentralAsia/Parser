package com.example.Parser.dto;


import lombok.Data;

@Data
public class ScarpedDataResponse {
    private String url;
    private String title;
    private String description;
    private String content;
    private String language;
    private String category;
    private String license;
}
