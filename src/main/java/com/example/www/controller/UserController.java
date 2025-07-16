package com.example.www.controller;

import com.example.www.entity.User;
import com.example.www.repository.UserRepository;
import com.example.www.util.JwtUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户相关接口控制器，提供注册和登录功能
 */
@RestController // 标记为REST风格控制器，返回JSON数据
@RequestMapping("/api/user") // 路由前缀
public class UserController {
    @Autowired // 自动注入用户仓库
    private UserRepository userRepository;
    // 密码加密器，用于加密和校验密码
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册接口
     * @param request 注册请求体
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        // 检查手机号是否已注册
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            return ResponseEntity.badRequest().body("手机号已注册");
        }
        // 检查邮箱是否已注册
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("邮箱已注册");
        }
        // 创建新用户并设置属性
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 密码加密存储
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        // 设置用户类型，默认USER
        String userType = request.getUserType();
        if (userType == null || (!userType.equals("ADMIN") && !userType.equals("USER"))) {
            userType = "USER";
        }
        user.setUserType(userType);
        // 设置头像，若未传则赋默认头像
        String defaultAvatar = "https://img1.baidu.com/it/u=1618033988,3141592653&fm=253&fmt=auto&app=138&f=JPEG?w=200&h=200"; // 可替换为base64
        if (request.getAvatar() == null || request.getAvatar().isEmpty()) {
            user.setAvatar(defaultAvatar);
        } else {
            user.setAvatar(request.getAvatar());
        }
        // 保存用户到数据库
        userRepository.save(user);
        return ResponseEntity.ok("注册成功");
    }

    /**
     * 用户登录接口
     * @param request 登录请求体
     * @return 登录结果，成功返回JWT Token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 根据用户名查找用户
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        // 校验密码
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            // 生成包含用户ID的JWT Token
            String token = JwtUtil.generateToken(user.getId(), request.getUsername());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }

    /**
     * 获取用户信息接口（从token获取当前用户）
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        System.out.println("=== 开始处理获取用户信息请求 ===");
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("错误: Authorization头缺失或格式错误");
            return ResponseEntity.status(401).body("未登录或token缺失");
        }
        
        String token = authHeader.replace("Bearer ", "");
        System.out.println("获取到token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // 优先使用用户ID查找用户，兼容旧版本token
        Long userId = JwtUtil.getUserIdFromToken(token);
        Optional<User> userOpt;
        
        if (userId != null) {
            // 新版本token，使用用户ID查找
            System.out.println("使用用户ID查找用户: " + userId);
            userOpt = userRepository.findById(userId);
        } else {
            // 旧版本token，使用用户名查找
            String username = JwtUtil.getUsernameFromToken(token);
            System.out.println("使用用户名查找用户: " + username);
            userOpt = userRepository.findByUsername(username);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("找到用户，ID: " + user.getId() + ", 用户名: " + user.getUsername());
            System.out.println("=== 获取用户信息完成 ===");
            return ResponseEntity.ok(user);
        } else {
            System.out.println("错误: 用户不存在");
            return ResponseEntity.badRequest().body("用户不存在");
        }
    }

    /**
     * 更新头像接口
     */
    @PostMapping({"/avatar", "/updateAvatar"})
    public ResponseEntity<?> updateAvatar(@RequestBody Map<String, String> body, HttpServletRequest request) {
        System.out.println("=== 开始处理头像更新请求 ===");
        
        String avatar = body.get("avatar");
        if (avatar == null || avatar.trim().isEmpty()) {
            System.out.println("错误: 头像数据为空");
            return ResponseEntity.badRequest().body("头像数据不能为空");
        }
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("错误: Authorization头缺失或格式错误");
            return ResponseEntity.status(401).body("未登录或token缺失");
        }
        
        String token = authHeader.replace("Bearer ", "");
        System.out.println("获取到token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // 优先使用用户ID查找用户，兼容旧版本token
        Long userId = JwtUtil.getUserIdFromToken(token);
        Optional<User> userOpt;
        
        if (userId != null) {
            // 新版本token，使用用户ID查找
            System.out.println("使用用户ID查找用户: " + userId);
            userOpt = userRepository.findById(userId);
        } else {
            // 旧版本token，使用用户名查找
            String username = JwtUtil.getUsernameFromToken(token);
            System.out.println("使用用户名查找用户: " + username);
            userOpt = userRepository.findByUsername(username);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("找到用户，ID: " + user.getId() + ", 用户名: " + user.getUsername());
            user.setAvatar(avatar);
            userRepository.save(user);
            System.out.println("头像更新成功");
            System.out.println("=== 头像更新完成 ===");
            return ResponseEntity.ok("头像更新成功");
        } else {
            System.out.println("错误: 用户不存在");
            return ResponseEntity.badRequest().body("用户不存在");
        }
    }

    /**
     * 更新用户信息接口
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateUserRequest request, HttpServletRequest httpRequest) {
        System.out.println("=== 开始处理用户信息更新请求 ===");
        
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("错误: Authorization头缺失或格式错误");
            return ResponseEntity.status(401).body("未登录或token缺失");
        }
        
        String token = authHeader.replace("Bearer ", "");
        System.out.println("获取到token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // 优先使用用户ID查找用户，兼容旧版本token
        Long userId = JwtUtil.getUserIdFromToken(token);
        Optional<User> userOpt;
        String currentUsername = null;
        
        if (userId != null) {
            // 新版本token，使用用户ID查找
            System.out.println("使用用户ID查找用户: " + userId);
            userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                currentUsername = userOpt.get().getUsername();
            }
        } else {
            // 旧版本token，使用用户名查找
            currentUsername = JwtUtil.getUsernameFromToken(token);
            System.out.println("使用用户名查找用户: " + currentUsername);
            userOpt = userRepository.findByUsername(currentUsername);
        }
        
        if (!userOpt.isPresent()) {
            System.out.println("错误: 用户不存在");
            return ResponseEntity.badRequest().body("用户不存在");
        }
        
        User user = userOpt.get();
        System.out.println("找到用户，ID: " + user.getId() + ", 当前用户名: " + user.getUsername());
        
        // 检查用户名是否已被其他用户使用
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            System.out.println("尝试修改用户名: " + user.getUsername() + " -> " + request.getUsername());
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                System.out.println("错误: 新用户名已存在: " + request.getUsername());
                return ResponseEntity.badRequest().body("用户名已存在");
            }
            user.setUsername(request.getUsername());
            System.out.println("用户名修改成功");
        }
        
        // 检查手机号是否已被其他用户使用
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            System.out.println("尝试修改手机号: " + user.getPhone() + " -> " + request.getPhone());
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                System.out.println("错误: 新手机号已注册: " + request.getPhone());
                return ResponseEntity.badRequest().body("手机号已注册");
            }
            user.setPhone(request.getPhone());
            System.out.println("手机号修改成功");
        }
        
        // 检查邮箱是否已被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            System.out.println("尝试修改邮箱: " + user.getEmail() + " -> " + request.getEmail());
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                System.out.println("错误: 新邮箱已注册: " + request.getEmail());
                return ResponseEntity.badRequest().body("邮箱已注册");
            }
            user.setEmail(request.getEmail());
            System.out.println("邮箱修改成功");
        }
        
        // 更新用户类型
        if (request.getUserType() != null) {
            System.out.println("尝试修改用户类型: " + user.getUserType() + " -> " + request.getUserType());
            if (request.getUserType().equals("ADMIN") || request.getUserType().equals("USER")) {
                user.setUserType(request.getUserType());
                System.out.println("用户类型修改成功");
            } else {
                System.out.println("错误: 无效的用户类型: " + request.getUserType());
                return ResponseEntity.badRequest().body("用户类型只能是ADMIN或USER");
            }
        }
        
        // 更新头像
        if (request.getAvatar() != null) {
            System.out.println("更新头像");
            user.setAvatar(request.getAvatar());
        }
        
        try {
            userRepository.save(user);
            System.out.println("用户信息保存成功，最终用户名: " + user.getUsername());
            System.out.println("=== 用户信息更新完成 ===");
            return ResponseEntity.ok("用户信息更新成功");
        } catch (Exception e) {
            System.out.println("保存用户信息时发生错误: " + e.getMessage());
            return ResponseEntity.status(500).body("保存失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码接口
     */
    @PostMapping("/changePwd")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        System.out.println("=== 开始处理密码修改请求 ===");
        System.out.println("请求参数 - 原密码长度: " + (request.getOldPwd() != null ? request.getOldPwd().length() : "null"));
        System.out.println("请求参数 - 新密码长度: " + (request.getNewPwd() != null ? request.getNewPwd().length() : "null"));
        
        // 参数验证
        if (request.getOldPwd() == null || request.getOldPwd().trim().isEmpty()) {
            System.out.println("错误: 原密码为空");
            return ResponseEntity.badRequest().body("原密码不能为空");
        }
        
        if (request.getNewPwd() == null || request.getNewPwd().trim().isEmpty()) {
            System.out.println("错误: 新密码为空");
            return ResponseEntity.badRequest().body("新密码不能为空");
        }
        
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("错误: Authorization头缺失或格式错误");
            return ResponseEntity.status(401).body("未登录或token缺失");
        }
        
        String token = authHeader.replace("Bearer ", "");
        System.out.println("获取到token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // 优先使用用户ID查找用户，兼容旧版本token
        Long userId = JwtUtil.getUserIdFromToken(token);
        Optional<User> userOpt;
        String currentUsername = null;
        
        if (userId != null) {
            // 新版本token，使用用户ID查找
            System.out.println("使用用户ID查找用户: " + userId);
            userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                currentUsername = userOpt.get().getUsername();
                System.out.println("通过用户ID找到用户，用户名: " + currentUsername);
            }
        } else {
            // 旧版本token，使用用户名查找
            currentUsername = JwtUtil.getUsernameFromToken(token);
            System.out.println("使用用户名查找用户: " + currentUsername);
            userOpt = userRepository.findByUsername(currentUsername);
        }
        
        if (!userOpt.isPresent()) {
            System.out.println("错误: 用户不存在");
            return ResponseEntity.badRequest().body("用户不存在");
        }
        
        User user = userOpt.get();
        System.out.println("找到用户，ID: " + user.getId() + ", 用户名: " + user.getUsername());
        
        // 检查数据库密码格式
        String dbPassword = user.getPassword();
        System.out.println("数据库中密码哈希完整长度: " + (dbPassword != null ? dbPassword.length() : "null"));
        System.out.println("数据库中密码哈希前20位: " + (dbPassword != null ? dbPassword.substring(0, Math.min(20, dbPassword.length())) + "..." : "null"));
        
        // 验证数据库密码是否为BCrypt格式
        if (dbPassword == null || !dbPassword.startsWith("$2a$") || dbPassword.length() != 60) {
            System.out.println("错误: 数据库密码格式异常，不是有效的BCrypt哈希");
            System.out.println("数据库密码内容: [" + dbPassword + "]");
            return ResponseEntity.status(500).body("数据库密码格式异常，请联系管理员");
        }
        
        // 验证旧密码
        System.out.println("=== 开始密码验证 ===");
        System.out.println("前端传入的原密码: [" + request.getOldPwd() + "]");
        System.out.println("数据库中存储的密码哈希: [" + dbPassword + "]");
        
        // 使用BCrypt验证密码
        boolean passwordMatches = passwordEncoder.matches(request.getOldPwd(), dbPassword);
        System.out.println("BCrypt验证结果: " + (passwordMatches ? "通过" : "失败"));
        
        // 如果验证失败，提供详细的调试信息
        if (!passwordMatches) {
            System.out.println("=== 密码验证失败调试信息 ===");
            System.out.println("原密码是否为空: " + (request.getOldPwd() == null || request.getOldPwd().trim().isEmpty()));
            System.out.println("数据库密码是否为空: " + (dbPassword == null || dbPassword.trim().isEmpty()));
            System.out.println("原密码长度: " + request.getOldPwd().length());
            System.out.println("数据库密码长度: " + dbPassword.length());
            
            // 尝试重新编码原密码进行对比
            String testHash = passwordEncoder.encode(request.getOldPwd());
            System.out.println("重新编码原密码的哈希: " + testHash.substring(0, Math.min(20, testHash.length())) + "...");
            boolean testMatch = passwordEncoder.matches(request.getOldPwd(), testHash);
            System.out.println("重新编码后的验证结果: " + (testMatch ? "通过" : "失败"));
            
            // 检查是否有特殊字符或编码问题
            System.out.println("原密码字节长度: " + request.getOldPwd().getBytes().length);
            System.out.println("原密码是否包含特殊字符: " + !request.getOldPwd().matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$"));
            System.out.println("=== 调试信息结束 ===");
            
            return ResponseEntity.badRequest().body("原密码错误");
        }
        
        // 更新新密码
        System.out.println("原密码验证通过，开始更新新密码...");
        String newPasswordHash = passwordEncoder.encode(request.getNewPwd());
        user.setPassword(newPasswordHash);
        
        try {
            userRepository.save(user);
            System.out.println("密码修改成功，新密码哈希: " + newPasswordHash.substring(0, Math.min(20, newPasswordHash.length())) + "...");
            System.out.println("=== 密码修改完成 ===");
            return ResponseEntity.ok("密码修改成功");
        } catch (Exception e) {
            System.out.println("保存新密码时发生错误: " + e.getMessage());
            return ResponseEntity.status(500).body("密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 注册请求体类
     */
    @Data // 自动生成getter/setter等方法
    public static class RegisterRequest {
        private String username; // 用户名
        private String password; // 密码
        private String phone;    // 手机号
        private String email;    // 邮箱
        private String userType; // 用户类型
        private String avatar;   // 头像（可选）
    }

    /**
     * 登录请求体类
     */
    @Data // 自动生成getter/setter等方法
    public static class LoginRequest {
        private String username; // 用户名
        private String password; // 密码
    }

    /**
     * 更新用户信息请求体类
     */
    @Data
    public static class UpdateUserRequest {
        private String username; // 用户名（可选）
        private String phone;    // 手机号（可选）
        private String email;    // 邮箱（可选）
        private String userType; // 用户类型（可选）
        private String avatar;   // 头像（可选）
    }

    /**
     * 修改密码请求体类
     */
    @Data
    public static class ChangePasswordRequest {
        private String oldPwd; // 原密码
        private String newPwd; // 新密码
    }
} 