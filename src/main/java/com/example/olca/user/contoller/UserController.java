package com.example.olca.user.contoller;

import com.example.olca.user.dto.request.UserCreateRequest;
import com.example.olca.user.dto.response.UserResponse;
import com.example.olca.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {

        UserResponse userResponse = userService.create(request);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {

        UserResponse response = userService.findById(id);
        return ResponseEntity.ok(response);
    }
}
