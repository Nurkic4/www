package com.example.www.repository;

import com.example.www.entity.ArticleReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleReviewRepository extends JpaRepository<ArticleReview, Long> {
    
    /**
     * 根据文章ID查找审核记录
     */
    List<ArticleReview> findByArticleOrderByCreatedAtDesc(com.example.www.entity.Article article);
    
    /**
     * 根据审核员ID查找审核记录
     */
    List<ArticleReview> findByReviewerOrderByCreatedAtDesc(com.example.www.entity.User reviewer);
} 