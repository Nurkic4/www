package com.example.www.dto;

import lombok.Data;
import java.util.List;

@Data
public class ArticleUpdateRequest {
    private String title;
    private String content;
    private String coverImage;
    private String status;
    private List<ArticleImageRequest> images;
    
    @Data
    public static class ArticleImageRequest {
        private String imageData; // Base64字符串
        private String imageName;
        private Integer sortOrder = 0;
    }
} 