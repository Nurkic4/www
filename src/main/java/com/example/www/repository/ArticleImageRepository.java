package com.example.www.repository;

import com.example.www.entity.ArticleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleImageRepository extends JpaRepository<ArticleImage, Long> {
    
    /**
     * 根据文章查找图片
     */
    List<ArticleImage> findByArticleOrderBySortOrderAsc(com.example.www.entity.Article article);
    
    /**
     * 根据文章删除图片
     */
    void deleteByArticle(com.example.www.entity.Article article);
} 