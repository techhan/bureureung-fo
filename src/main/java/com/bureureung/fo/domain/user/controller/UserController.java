package com.bureureung.fo.domain.user.controller;

import com.bureureung.fo.domain.user.dto.RegisterRequest;
import com.bureureung.fo.domain.user.dto.UserResponse;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.service.UserService;
import com.bureureung.fo.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/v1/users/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@RequestBody @Valid RegisterRequest request) {
        FoUser savedUser = userService.register(request);
        return ApiResponse.success(HttpStatus.CREATED, UserResponse.from(savedUser));
    }
}
