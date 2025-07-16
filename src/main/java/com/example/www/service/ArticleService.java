package com.example.www.service;

import com.example.www.entity.Article;
import com.example.www.entity.ArticleImage;
import com.example.www.repository.ArticleImageRepository;
import com.example.www.repository.ArticleRepository;
import com.example.www.dto.ArticleUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ArticleService {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleImageRepository articleImageRepository;

    @Transactional
    public void updateArticleAndImages(Article article, ArticleUpdateRequest request) {
        // 更新文章基本信息
        if (request.getTitle() != null) article.setTitle(request.getTitle());
        if (request.getContent() != null) article.setContent(request.getContent());
        if (request.getCoverImage() != null) article.setCoverImage(request.getCoverImage());
        if (request.getStatus() != null) article.setStatus(request.getStatus());
        articleRepository.save(article);

        // 更新图片信息
        if (request.getImages() != null) {
            // 删除旧图片
            articleImageRepository.deleteByArticle(article);
            // 添加新图片
            for (ArticleUpdateRequest.ArticleImageRequest imageRequest : request.getImages()) {
                ArticleImage image = new ArticleImage();
                image.setArticle(article);
                image.setImageData(imageRequest.getImageData());
                image.setImageName(imageRequest.getImageName());
                image.setSortOrder(imageRequest.getSortOrder() != null ? imageRequest.getSortOrder() : 0);
                image.setImageUrl(null);
                articleImageRepository.save(image);
            }
        }
    }
} 