package com.example.www.service;

import com.example.www.entity.Article;
import com.example.www.entity.ArticleLike;
import com.example.www.entity.User;
import com.example.www.repository.ArticleLikeRepository;
import com.example.www.repository.ArticleRepository;
import com.example.www.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ArticleLikeService {
    
    @Autowired
    private ArticleLikeRepository articleLikeRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 点赞文章
     */
    @Transactional
    public void likeArticle(Long articleId, Long userId) {
        // 获取文章和用户
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (!optionalArticle.isPresent()) {
            throw new IllegalArgumentException("文章不存在");
        }
        Article article = optionalArticle.get();
        
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException("用户不存在");
        }
        User user = optionalUser.get();
        
        // 检查是否已经点赞
        if (articleLikeRepository.existsByArticleAndUser(article, user)) {
            throw new IllegalArgumentException("已经点赞过此文章");
        }
        
        // 创建点赞记录
        ArticleLike like = new ArticleLike();
        like.setArticle(article);
        like.setUser(user);
        articleLikeRepository.save(like);
        
        // 更新文章点赞数
        article.setLikeCount(article.getLikeCount() + 1);
        articleRepository.save(article);
    }
    
    /**
     * 取消点赞
     */
    @Transactional
    public void unlikeArticle(Long articleId, Long userId) {
        // 获取文章和用户
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (!optionalArticle.isPresent()) {
            throw new IllegalArgumentException("文章不存在");
        }
        Article article = optionalArticle.get();
        
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException("用户不存在");
        }
        User user = optionalUser.get();
        
        // 删除点赞记录
        articleLikeRepository.deleteByArticleAndUser(article, user);
        
        // 更新文章点赞数
        if (article.getLikeCount() > 0) {
            article.setLikeCount(article.getLikeCount() - 1);
            articleRepository.save(article);
        }
    }
    
    /**
     * 检查用户是否已点赞文章
     */
    public boolean isLiked(Long articleId, Long userId) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (!optionalArticle.isPresent()) {
            return false;
        }
        
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            return false;
        }
        
        return articleLikeRepository.existsByArticleAndUser(optionalArticle.get(), optionalUser.get());
    }
    
    /**
     * 获取文章的点赞用户列表
     */
    public java.util.List<ArticleLike> getArticleLikes(Long articleId) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (!optionalArticle.isPresent()) {
            return new java.util.ArrayList<>();
        }
        return articleLikeRepository.findByArticle(optionalArticle.get());
    }
    
    /**
     * 获取用户的点赞文章列表
     */
    public java.util.List<ArticleLike> getUserLikes(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            return new java.util.ArrayList<>();
        }
        return articleLikeRepository.findByUser(optionalUser.get());
    }
} 