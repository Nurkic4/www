package com.example.www.service;

import com.example.www.dto.ArticleReviewRequest;
import com.example.www.entity.Article;
import com.example.www.entity.ArticleReview;
import com.example.www.entity.User;
import com.example.www.repository.ArticleRepository;
import com.example.www.repository.ArticleReviewRepository;
import com.example.www.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleReviewService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private ArticleReviewRepository articleReviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 审核文章
     */
    @Transactional
    public void reviewArticle(Long articleId, ArticleReviewRequest request, Long reviewerId) {
        // 验证审核员权限
        Optional<User> reviewer = userRepository.findById(reviewerId);
        if (reviewer.isEmpty() || !"ADMIN".equals(reviewer.get().getUserType())) {
            throw new IllegalArgumentException("无权限进行审核");
        }
        
        // 获取文章
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        Article article = optionalArticle.get();
        
        // 检查文章状态
        if (!"PENDING".equals(article.getStatus())) {
            throw new IllegalArgumentException("文章状态不允许审核");
        }
        
        // 验证审核动作
        if (!"APPROVE".equals(request.getAction()) && !"REJECT".equals(request.getAction())) {
            throw new IllegalArgumentException("无效的审核动作");
        }
        
        // 更新文章状态
        if ("APPROVE".equals(request.getAction())) {
            article.setStatus("APPROVED");
            article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setStatus("REJECTED");
            article.setReviewComment(request.getComment());
        }
        
        articleRepository.save(article);
        
        // 创建审核记录
        ArticleReview review = new ArticleReview();
        review.setArticle(article);
        review.setReviewer(reviewer.get());
        review.setAction(request.getAction());
        review.setComment(request.getComment());
        
        articleReviewRepository.save(review);
    }
    
    /**
     * 获取文章的审核记录
     */
    public List<ArticleReview> getArticleReviewHistory(Long articleId) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        List<ArticleReview> reviews = articleReviewRepository.findByArticleOrderByCreatedAtDesc(optionalArticle.get());
        
        // 填充审核员信息
        for (ArticleReview review : reviews) {
            if (review.getReviewer() != null) {
                review.setReviewerName(review.getReviewer().getUsername());
            }
        }
        
        return reviews;
    }
    
    /**
     * 获取审核员的审核记录
     */
    public List<ArticleReview> getReviewerHistory(Long reviewerId) {
        Optional<User> optionalUser = userRepository.findById(reviewerId);
        if (optionalUser.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return articleReviewRepository.findByReviewerOrderByCreatedAtDesc(optionalUser.get());
    }
} 