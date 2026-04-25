package com.cheng.common.repository;

import com.cheng.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    /**
     * 按用户名或昵称模糊搜索
     */
    Page<User> findByUsernameContainingOrNicknameContaining(
        String username, String nickname, Pageable pageable
    );
}
