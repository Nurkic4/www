package com.example.www.repository;

import com.example.www.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    
    /**
     * 根据文章和用户查找点赞记录
     */
    Optional<ArticleLike> findByArticleAndUser(com.example.www.entity.Article article, com.example.www.entity.User user);
    
    /**
     * 根据文章查找所有点赞记录
     */
    List<ArticleLike> findByArticle(com.example.www.entity.Article article);
    
    /**
     * 根据用户查找点赞记录
     */
    List<ArticleLike> findByUser(com.example.www.entity.User user);
    
    /**
     * 检查用户是否已点赞文章
     */
    boolean existsByArticleAndUser(com.example.www.entity.Article article, com.example.www.entity.User user);
    
    /**
     * 根据文章和用户删除点赞记录
     */
    void deleteByArticleAndUser(com.example.www.entity.Article article, com.example.www.entity.User user);
} 