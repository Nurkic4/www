package com.example.www.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体类，对应数据库中的article表
 */
@Data
@Entity
@Table(name = "article")
public class Article {
    
    /**
     * 主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 文章标题
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 文章内容
     */
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    
    /**
     * 封面图片Base64数据
     */
    @Column(columnDefinition = "LONGTEXT")
    private String coverImage;
    
    /**
     * 作者ID
     */
    @Column(nullable = false)
    private Long authorId;
    
    /**
     * 文章状态：DRAFT-草稿，PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝
     */
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";
    
    /**
     * 浏览次数
     */
    @Column(nullable = false)
    private Integer viewCount = 0;
    
    /**
     * 点赞次数
     */
    @Column(nullable = false)
    private Integer likeCount = 0;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;
    
    /**
     * 审核意见
     */
    @Column(length = 500)
    private String reviewComment;
    
    /**
     * 文章图片列表
     */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ArticleImage> images;
    
    /**
     * 审核记录列表
     */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ArticleReview> reviews;
    
    /**
     * 点赞记录列表
     */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ArticleLike> likes;
    
    /**
     * 作者信息（非持久化字段）
     */
    @Transient
    private String authorName;
    
    /**
     * 作者头像（非持久化字段）
     */
    @Transient
    private String authorAvatar;
    
    /**
     * 当前用户是否点赞（非持久化字段）
     */
    @Transient
    private Boolean isLiked = false;
    
    /**
     * 在保存前设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 在更新前设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 