package com.example.movie.service;

import com.example.movie.common.BusinessException;
import com.example.movie.dto.LoginRequest;
import com.example.movie.dto.LoginResponse;
import com.example.movie.dto.RegisterRequest;
import com.example.movie.dto.UserVO;
import com.example.movie.entity.Role;
import com.example.movie.entity.User;
import com.example.movie.repository.UserRepository;
import com.example.movie.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已被注册");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException("手机号已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setNickname(request.nickname() == null || request.nickname().isBlank()
                ? username : request.nickname().trim());
        user.setRole(Role.USER);
        user.setStatus(1);
        userRepository.save(user);

        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public UserVO currentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        return UserVO.from(user);
    }

    private LoginResponse buildResponse(User user) {
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole().name());
        return new LoginResponse(token, UserVO.from(user));
    }
}
