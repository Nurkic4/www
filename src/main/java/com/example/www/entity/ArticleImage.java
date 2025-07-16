package com.example.www.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * 文章图片实体类，对应数据库中的article_image表
 */
@Data
@Entity
@Table(name = "article_image")
public class ArticleImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的文章
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    @JsonIgnore
    private Article article;

    /**
     * 图片URL
     */
    @Column(nullable = true, length = 500) // 允许为 null
    private String imageUrl;

    /**
     * 图片名称
     */
    @Column(length = 200)
    private String imageName;

    /**
     * 排序
     */
    @Column(nullable = false)
    private Integer sortOrder = 0;

    /**
     * 图片数据 (Base64)
     */
    @Column(columnDefinition = "LONGTEXT")
    private String imageData;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 