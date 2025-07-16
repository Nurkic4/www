package com.example.www.controller;

import com.example.www.dto.ArticleCreateRequest;
import com.example.www.dto.ArticleUpdateRequest;
import com.example.www.dto.ArticleReviewRequest;
import com.example.www.dto.PageResponse;
import com.example.www.entity.Article;
import com.example.www.entity.ArticleImage;
import com.example.www.entity.User;
import com.example.www.repository.ArticleRepository;
import com.example.www.repository.ArticleImageRepository;
import com.example.www.repository.UserRepository;
import com.example.www.service.ArticleLikeService;
import com.example.www.service.ArticleReviewService;
import com.example.www.service.ArticleService;
import com.example.www.util.JwtUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/article")
@CrossOrigin(origins = {
    "http://localhost:3000", "http://127.0.0.1:3000",
    "http://localhost:5173", "http://127.0.0.1:5173"
})
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleImageRepository articleImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private ArticleReviewService articleReviewService;

    @Autowired
    private ArticleService articleService;

    /**
     * 创建文章
     */
    @PostMapping("/create")
    public ResponseEntity<?> createArticle(@RequestBody ArticleCreateRequest request, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            // 创建文章
            Article article = new Article();
            article.setTitle(request.getTitle());
            article.setContent(request.getContent());
            article.setCoverImage(request.getCoverImage());
            article.setAuthorId(userId);
            article.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");

            article = articleRepository.save(article);

            // 保存文章图片
            if (request.getImages() != null) {
                for (ArticleCreateRequest.ArticleImageRequest imageRequest : request.getImages()) {
                    ArticleImage image = new ArticleImage();
                    image.setArticle(article);
                    image.setImageData(imageRequest.getImageData());
                    image.setImageName(imageRequest.getImageName());
                    image.setSortOrder(imageRequest.getSortOrder() != null ? imageRequest.getSortOrder() : 0);
                    image.setImageUrl(null); // 明确设为 null，兼容只用 base64
                    articleImageRepository.save(image);
                }
            }

            return ResponseEntity.ok(new CreateResponse(article.getId(), article.getTitle(), article.getStatus(), "文章创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建文章失败: " + e.getMessage());
        }
    }

    /**
     * 获取文章列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getArticleList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String keyword) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Article> articlePage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                if (status != null) {
                    articlePage = articleRepository.findByStatusAndKeyword(status, keyword, pageable);
                } else if (authorId != null) {
                    articlePage = articleRepository.findByAuthorIdAndKeyword(authorId, keyword, pageable);
                } else {
                    articlePage = articleRepository.findByKeyword(keyword, pageable);
                }
            } else {
                if (status != null && authorId != null) {
                    articlePage = articleRepository.findByAuthorIdAndStatus(authorId, status, pageable);
                } else if (status != null) {
                    articlePage = articleRepository.findByStatus(status, pageable);
                } else if (authorId != null) {
                    articlePage = articleRepository.findByAuthorId(authorId, pageable);
                } else {
                    articlePage = articleRepository.findAll(pageable);
                }
            }

            // 填充作者信息
            List<Article> articles = articlePage.getContent();
            for (Article article : articles) {
                Optional<User> author = userRepository.findById(article.getAuthorId());
                if (author.isPresent()) {
                    article.setAuthorName(author.get().getUsername());
                    article.setAuthorAvatar(author.get().getAvatar()); // 新增：设置作者头像
                }
            }

            PageResponse<Article> response = new PageResponse<>(
                articlePage.getTotalElements(),
                articlePage.getTotalPages(),
                page,
                size,
                articles
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取文章列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleDetail(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Optional<Article> optionalArticle = articleRepository.findById(id);
            if (optionalArticle.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Article article = optionalArticle.get();

            // 填充作者信息
            Optional<User> author = userRepository.findById(article.getAuthorId());
            if (author.isPresent()) {
                article.setAuthorName(author.get().getUsername());
                article.setAuthorAvatar(author.get().getAvatar());
            }

            // 填充图片信息
            List<ArticleImage> images = articleImageRepository.findByArticleOrderBySortOrderAsc(article);
            article.setImages(images);

            // 检查当前用户是否点赞
            Long currentUserId = getCurrentUserId(httpRequest);
            if (currentUserId != null) {
                article.setIsLiked(articleLikeService.isLiked(id, currentUserId));
            }

            return ResponseEntity.ok(article);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取文章详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Long id, @RequestBody ArticleUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            Optional<Article> optionalArticle = articleRepository.findById(id);
            if (optionalArticle.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Article article = optionalArticle.get();

            // 检查权限
            if (!article.getAuthorId().equals(userId)) {
                return ResponseEntity.status(403).body("无权限修改此文章");
            }

            // 迁移到 Service 层，保证事务
            articleService.updateArticleAndImages(article, request);

            return ResponseEntity.ok("文章更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新文章失败: " + e.getMessage());
        }
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            Optional<Article> optionalArticle = articleRepository.findById(id);
            if (optionalArticle.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Article article = optionalArticle.get();

            // 检查权限
            if (!article.getAuthorId().equals(userId)) {
                return ResponseEntity.status(403).body("无权限删除此文章");
            }

            articleRepository.delete(article);
            return ResponseEntity.ok("文章删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除文章失败: " + e.getMessage());
        }
    }

    /**
     * 提交审核
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitArticle(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            Optional<Article> optionalArticle = articleRepository.findById(id);
            if (optionalArticle.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Article article = optionalArticle.get();

            // 检查权限
            if (!article.getAuthorId().equals(userId)) {
                return ResponseEntity.status(403).body("无权限提交此文章");
            }

            // 检查状态
            if (!"DRAFT".equals(article.getStatus())) {
                return ResponseEntity.badRequest().body("只有草稿状态的文章才能提交审核");
            }

            article.setStatus("PENDING");
            articleRepository.save(article);

            return ResponseEntity.ok("文章已提交审核");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("提交审核失败: " + e.getMessage());
        }
    }

    /**
     * 获取待审核文章列表
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        try {
            // 检查管理员权限
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty() || !"ADMIN".equals(user.get().getUserType())) {
                return ResponseEntity.status(403).body("无权限访问");
            }

            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Article> articlePage = articleRepository.findByStatus("PENDING", pageable);

            // 填充作者信息
            List<Article> articles = articlePage.getContent();
            for (Article article : articles) {
                Optional<User> author = userRepository.findById(article.getAuthorId());
                if (author.isPresent()) {
                    article.setAuthorName(author.get().getUsername());
                }
            }

            PageResponse<Article> response = new PageResponse<>(
                articlePage.getTotalElements(),
                articlePage.getTotalPages(),
                page,
                size,
                articles
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取待审核文章失败: " + e.getMessage());
        }
    }

    /**
     * 审核文章
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<?> reviewArticle(@PathVariable Long id, @RequestBody ArticleReviewRequest request, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            articleReviewService.reviewArticle(id, request, userId);
            return ResponseEntity.ok("审核完成");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("审核失败: " + e.getMessage());
        }
    }

    /**
     * 点赞文章
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeArticle(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            articleLikeService.likeArticle(id, userId);
            return ResponseEntity.ok("点赞成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("点赞失败: " + e.getMessage());
        }
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikeArticle(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            articleLikeService.unlikeArticle(id, userId);
            return ResponseEntity.ok("取消点赞成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("取消点赞失败: " + e.getMessage());
        }
    }

    /**
     * 增加浏览次数
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<?> incrementViewCount(@PathVariable Long id) {
        try {
            Optional<Article> optionalArticle = articleRepository.findById(id);
            if (optionalArticle.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Article article = optionalArticle.get();
            article.setViewCount(article.getViewCount() + 1);
            articleRepository.save(article);

            return ResponseEntity.ok("浏览次数已更新");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新浏览次数失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的文章列表
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }

            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Article> articlePage;

            if (status != null) {
                articlePage = articleRepository.findByAuthorIdAndStatus(userId, status, pageable);
            } else {
                articlePage = articleRepository.findByAuthorId(userId, pageable);
            }

            PageResponse<Article> response = new PageResponse<>(
                articlePage.getTotalElements(),
                articlePage.getTotalPages(),
                page,
                size,
                articlePage.getContent()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取我的文章失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.replace("Bearer ", "");
        return JwtUtil.getUserIdFromToken(token);
    }

    @Data
    public static class CreateResponse {
        private Long id;
        private String title;
        private String status;
        private String message;

        public CreateResponse(Long id, String title, String status, String message) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.message = message;
        }
    }
} 