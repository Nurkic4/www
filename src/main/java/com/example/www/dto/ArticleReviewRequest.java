package com.example.www.dto;

import lombok.Data;

@Data
public class ArticleReviewRequest {
    private String action; // APPROVE 或 REJECT
    private String comment;
} 