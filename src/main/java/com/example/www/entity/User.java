package com.example.www.entity;

// 引入lombok的@Data注解，自动生成getter/setter等方法
import lombok.Data;
// 引入JPA相关注解
import jakarta.persistence.*;

/**
 * 用户实体类，对应数据库中的user表
 */
@Data // 自动生成常用方法
@Entity // 标记为JPA实体
@Table(name = "user") // 指定表名为user
public class User {
    /**
     * 主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名，唯一且不能为空
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 密码，不能为空
     */
    @Column(nullable = false)
    private String password;

    /**
     * 手机号，唯一且不能为空
     */
    @Column(nullable = false, unique = true)
    private String phone;

    /**
     * 邮箱，唯一且不能为空
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 用户类型，只能为'ADMIN'或'USER'，默认为'USER'
     */
    @Column(nullable = false)
    private String userType = "USER"; // 只能为'ADMIN'或'USER'

    /**
     * 头像，base64字符串或图片url
     */
    @Column(columnDefinition = "LONGTEXT")
    private String avatar;
} 