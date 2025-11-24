package com.boot.ict05_final_admin.web.auth;

import com.boot.ict05_final_admin.domain.auth.dto.JoinRequest;
import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.auth.repository.JoinRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthApiController {

    private final JoinRepository joinRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping(value = "/exist", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Boolean> exist(@RequestParam String email) {
        boolean exists = joinRepository.existsByEmail(email);
        return Map.of("exists", exists);
    }

    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> register(@Valid @RequestBody JoinRequest req) {
        if (joinRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EMAIL_IN_USE");
        }
        Member m = Member.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();
        joinRepository.save(m);

        // ⚠ 여기 게터는 프로젝트에 맞게: getMemberId() 또는 getId()
        long id = m.getId(); // 또는 m.getId()

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("memberId", id));
    }
}
