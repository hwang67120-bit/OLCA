package com.example.olca.user.service;


import com.example.olca.user.domain.User;
import com.example.olca.user.dto.request.UserCreateRequest;
import com.example.olca.user.dto.response.UserResponse;
import com.example.olca.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse create(UserCreateRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("중복된 이름입니다");
        }

        User user = User.builder()
                .username(request.username())
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자 이름을 찿을 수 없습니다"));

        return UserResponse.from(user);
    }


}

