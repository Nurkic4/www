package com.example.www.repository;

import com.example.www.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 用户仓库接口，继承JpaRepository，提供基本的CRUD和自定义查询方法
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象的Optional包装
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户对象的Optional包装
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象的Optional包装
     */
    Optional<User> findByEmail(String email);
} 