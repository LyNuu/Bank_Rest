package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.model.dto.UserDto;
import org.example.model.dto.mapper.UserMapper;
import org.example.model.role.Role;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/security")
@RequiredArgsConstructor
public class MainController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(
            summary = "Get current user info"
    )
    @GetMapping("/user/info")
    public ResponseEntity<?> userAccess(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("{\"error\":\"Not authenticated\"}");
        }
        return ResponseEntity.ok(principal);
    }

    @Operation(
            summary = "Get all authenticated users "
    )
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllAuthenticatedUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(user -> new UserDto(
                        user.getLogin(),
                        user.getEmail(),
                        user.getPassword()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }

    @Operation(
            summary = "Get user by ID (ADMIN and PREMIUM_USER)"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM_USER')")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(
            summary = "Update user role (ADMIN only)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/user/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role newRole) {

        User updatedUser = userService.updateUserRole(id, newRole);
        UserDto userDto = userMapper.toDto(updatedUser);
        return ResponseEntity.ok(userDto);
    }

}
