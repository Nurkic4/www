package com.example.www.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * 文章审核记录实体类，对应数据库中的article_review表
 */
@Data
@Entity
@Table(name = "article_review")
public class ArticleReview {
    
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
     * 关联的审核员
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonIgnore
    private User reviewer;

    /**
     * 审核动作：APPROVE-通过，REJECT-拒绝
     */
    @Column(nullable = false, length = 20)
    private String action;

    /**
     * 审核意见
     */
    @Column(length = 500)
    private String comment;

    /**
     * 审核时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 审核员姓名（非数据库字段）
     */
    @Transient
    private String reviewerName;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 