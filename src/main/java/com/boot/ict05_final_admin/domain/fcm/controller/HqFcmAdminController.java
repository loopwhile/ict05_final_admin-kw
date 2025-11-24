package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmRegisterTokenRequest;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmTemplatePreviewRequest;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmTestSendRequest;
import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.service.FcmService;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * 본사(FQ) 관리자용 FCM 관리 REST API.
 *
 * <p>토큰 등록/해제, 템플릿 미리보기, 테스트 전송, 토픽 구독/해제 등의 관리 기능을 제공한다.
 * 세션 기반 인증(스프링 시큐리티)을 전제로 하며, HQ/ADMIN 권한을 가진 사용자만 접근할 수 있다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
@Tag(name = "HQ FCM 관리", description = "토큰 관리, 템플릿 미리보기, 테스트 전송 및 토픽 구독 관리")
public class HqFcmAdminController {

    private final FcmService fcmService;
    private final MyPageService myPageService;

    /**
     * FCM 토큰 등록(업서트).
     *
     * <p>세션의 사용자 ID를 자동으로 매핑하여 해당 사용자 소유의 토큰을 등록(또는 갱신)한다.</p>
     *
     * @param req           토큰 등록 요청 DTO
     * @param authPrincipal 스프링 시큐리티 인증 프린시펄(커스텀 타입 가능)
     * @param principal     서블릿 프린시펄(대체 소스)
     * @return 등록 결과 맵
     */
    @Operation(summary = "토큰 등록(업서트)",
            description = "세션 사용자 기준으로 FCM 토큰을 등록하거나 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody FcmRegisterTokenRequest req,
                                                        @AuthenticationPrincipal Object authPrincipal,
                                                        Principal principal) {
        Long sessionMemberId = resolveMemberId(authPrincipal, principal);
        fcmService.registerToken(req, sessionMemberId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * FCM 토큰 삭제(언레지스터).
     *
     * @param token         등록 해제할 토큰
     * @param authPrincipal 스프링 시큐리티 인증 프린시펄
     * @param principal     서블릿 프린시펄
     * @return 삭제 결과 맵
     */
    @Operation(summary = "토큰 삭제(언레지스터)",
            description = "세션 사용자 기준으로 FCM 토큰을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @DeleteMapping("/register/{token}")
    public ResponseEntity<Map<String, Object>> unregister(@PathVariable String token,
                                                          @AuthenticationPrincipal Object authPrincipal,
                                                          Principal principal) {
        fcmService.unregisterToken(token, resolveMemberId(authPrincipal, principal));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * 템플릿 미리보기(렌더링 결과 반환).
     *
     * @param req 템플릿 코드 및 변수 맵
     * @return 렌더링된 제목/본문
     */
    @Operation(summary = "템플릿 미리보기",
            description = "템플릿 코드를 렌더링하여 제목과 본문을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "렌더링 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/template/preview")
    public ResponseEntity<Map<String, String>> preview(@Valid @RequestBody FcmTemplatePreviewRequest req) {
        String title = fcmService.renderTitle(req.templateCode(), req.variables());
        String body  = fcmService.renderBody (req.templateCode(), req.variables());
        return ResponseEntity.ok(Map.of("title", title, "body", body));
    }

    /**
     * 테스트 전송 엔드포인트.
     *
     * <p>요청의 {@code topic} 플래그에 따라 토픽 전송 또는 토큰 전송을 수행한다.</p>
     *
     * @param req 테스트 전송 요청 DTO
     * @return 생성된 메시지 ID
     */
    @Operation(summary = "테스트 전송 (토픽/토큰)",
            description = "토픽 또는 단일 토큰으로 테스트 알림을 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전송 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/send/test")
    public ResponseEntity<Map<String, String>> sendTest(@Valid @RequestBody FcmTestSendRequest req) {
        String id = req.topic()
                ? fcmService.sendToTopic(AppType.HQ, req.tokenOrTopic(), req.title(), req.body(), req.data())
                : fcmService.sendToToken(AppType.HQ, req.tokenOrTopic(), req.title(), req.body(), req.data());
        return ResponseEntity.ok(Map.of("messageId", id));
    }

    /**
     * 세션 Principal에서 memberId(Long)를 최대한 관용적으로 추출한다.
     *
     * <p>우선 SecurityContext의 principal을 사용하고, Member/UserDetails/Principal.name 또는
     * 리플렉션(getMemberId/getId 등)을 통해 ID를 추출한다. 불가할 경우 null을 반환한다.</p>
     *
     * @param authPrincipal 스프링의 인증 프린시펄(커스텀 타입 가능)
     * @param plain         서블릿 프린시펄
     * @return memberId 또는 null
     */
    private Long resolveMemberId(Object authPrincipal, Principal plain) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Object p = (authPrincipal != null) ? authPrincipal : (authentication != null ? authentication.getPrincipal() : null);

        if (p instanceof Member m) {
            return m.getId();
        }

        if (p instanceof UserDetails ud) {
            try {
                return myPageService.findByEmail(ud.getUsername()).getId();
            } catch (Exception ignore) {
            }
        }

        if (plain != null && plain.getName() != null && !plain.getName().isBlank()) {
            try {
                return myPageService.findByEmail(plain.getName()).getId();
            } catch (Exception ignore) {
            }
        }

        Long byReflection = extractIdByReflection(p).orElse(null);
        return byReflection;
    }

    /**
     * 리플렉션을 통해 후보 메서드(getMemberId/getId/memberId/id)를 호출하여 ID를 추출 시도한다.
     *
     * @param principal 후보 객체
     * @return Optional에 래핑된 Long ID
     */
    private Optional<Long> extractIdByReflection(Object principal) {
        if (principal == null) return Optional.empty();
        for (String m : new String[]{"getMemberId", "getId", "memberId", "id"}) {
            try {
                Method method = principal.getClass().getMethod(m);
                Object v = method.invoke(principal);
                if (v instanceof Long l) return Optional.of(l);
                if (v instanceof Number n) return Optional.of(n.longValue());
                if (v instanceof String s && !s.isBlank()) return Optional.of(Long.parseLong(s));
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
            }
        }
        return Optional.empty();
    }

    /**
     * 현재 세션 사용자의 토큰들을 지정 토픽에 구독 처리한다.
     *
     * @param topic     구독할 토픽
     * @param principal 서블릿 프린시펄
     * @return 처리 결과 맵
     */
    @Operation(summary = "토픽 구독(세션 사용자 토큰들)",
            description = "세션 사용자의 보유 토큰을 지정 토픽으로 구독 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/topic/subscribe")
    public ResponseEntity<Map<String, Object>> subscribeMyTokens(@RequestParam String topic, Principal principal) {
        Long memberId = extractMemberId(principal);
        fcmService.subscribeToTopic(topic, memberId);
        return ResponseEntity.ok(Map.of("ok", true, "topic", topic));
    }

    /**
     * 현재 세션 사용자의 토큰들을 지정 토픽에서 구독 해제 처리한다.
     *
     * @param topic     해제할 토픽
     * @param principal 서블릿 프린시펄
     * @return 처리 결과 맵
     */
    @Operation(summary = "토픽 구독 해제(세션 사용자 토큰들)",
            description = "세션 사용자의 보유 토큰을 지정 토픽에서 구독 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 해제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/topic/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribeMyTokens(@RequestParam String topic, Principal principal) {
        Long memberId = extractMemberId(principal);
        fcmService.unsubscribeFromTopic(topic, memberId);
        return ResponseEntity.ok(Map.of("ok", true, "topic", topic));
    }

    /**
     * 세션/인증 객체에서 Member ID를 안전하게 추출한다.
     *
     * @param plain 서블릿 프린시펄
     * @return memberId
     * @throws IllegalStateException 식별 불가 시 예외 발생
     */
    private Long extractMemberId(Principal plain) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Object p = (auth != null ? auth.getPrincipal() : null);

        if (p instanceof Member m) return m.getId();

        if (p instanceof UserDetails ud) {
            return myPageService.findByEmail(ud.getUsername()).getId();
        }

        if (plain != null && plain.getName() != null && !plain.getName().isBlank()) {
            return myPageService.findByEmail(plain.getName()).getId();
        }

        throw new IllegalStateException("로그인 사용자를 식별할 수 없습니다.");
    }
}
