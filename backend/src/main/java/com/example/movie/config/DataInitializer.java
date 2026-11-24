package com.example.movie.config;

import com.example.movie.entity.Role;
import com.example.movie.entity.User;
import com.example.movie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 首次启动时初始化演示账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfAbsent("admin", "admin123", "系统管理员", "13800000000", Role.ADMIN);
        createIfAbsent("demo", "demo123", "观影达人", "13900000000", Role.USER);
    }

    private void createIfAbsent(String username, String rawPassword, String nickname,
                                String phone, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(1);
        userRepository.save(user);
        log.info("已初始化账号：{} / {}（{}）", username, rawPassword, role);
    }
}
