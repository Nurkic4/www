package com.example.www.repository;

import com.example.www.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    /**
     * 根据作者ID查找文章
     */
    Page<Article> findByAuthorId(Long authorId, Pageable pageable);
    
    /**
     * 根据状态查找文章
     */
    Page<Article> findByStatus(String status, Pageable pageable);
    
    /**
     * 根据作者ID和状态查找文章
     */
    Page<Article> findByAuthorIdAndStatus(Long authorId, String status, Pageable pageable);
    
    /**
     * 根据标题或内容模糊搜索
     */
    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.content LIKE %:keyword%")
    Page<Article> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据状态和关键词搜索
     */
    @Query("SELECT a FROM Article a WHERE a.status = :status AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Article> findByStatusAndKeyword(@Param("status") String status, @Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据作者ID和关键词搜索
     */
    @Query("SELECT a FROM Article a WHERE a.authorId = :authorId AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Article> findByAuthorIdAndKeyword(@Param("authorId") Long authorId, @Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 查找已发布的文章（用于公开访问）
     */
    Page<Article> findByStatusOrderByPublishedAtDesc(String status, Pageable pageable);
    
    /**
     * 增加浏览次数
     */
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * 增加点赞次数
     */
    @Query("UPDATE Article a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    void incrementLikeCount(@Param("id") Long id);
    
    /**
     * 减少点赞次数
     */
    @Query("UPDATE Article a SET a.likeCount = a.likeCount - 1 WHERE a.id = :id AND a.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);
} 