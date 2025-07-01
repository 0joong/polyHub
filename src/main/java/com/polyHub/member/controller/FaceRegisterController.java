package com.polyHub.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polyHub.member.entity.CustomUserDetails;
import com.polyHub.member.entity.FaceId;
import com.polyHub.member.entity.Member;
import com.polyHub.member.repository.FaceIdRepository;
import com.polyHub.member.repository.MemberRepository;
import com.polyHub.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FaceRegisterController {

    private final FaceIdRepository faceIdRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @PostMapping("/face-register")
    public Map<String, Object> registerFace(@RequestParam("file") MultipartFile file,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // ✅ 세션에서 로그인된 사용자 이메일 가져오기
            String email = userDetails.getMember().getEmail();

            // 1️⃣ 받은 사진을 저장
            String fileName = "face_" + System.currentTimeMillis() + ".png";
            Path faceImagesDir = Paths.get("face_images");
            if (!Files.exists(faceImagesDir)) {
                Files.createDirectories(faceImagesDir);
            }
            Path filePath = faceImagesDir.resolve(fileName);
            Files.write(filePath, file.getBytes());

            // 2️⃣ 얼굴 벡터 추출 후 DB 저장
            FaceId faceId = FaceId.builder()
                    .email(email)
                    .vectorPath(sendToPythonServer(filePath)) // 예: 얼굴 임베딩 벡터 파일 경로
                    .build();
            faceIdRepository.save(faceId);

            return Map.of("success", true, "faceId", faceId.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false);
        }
    }


    private String sendToPythonServer(Path filePath) throws Exception {

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // [추가] 호스트 이름 검증 비활성화를 위한 SSL 파라미터 설정
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm(""); // 이 부분이 호스트 이름(IP 주소 포함) 검증을 끕니다.

        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParams) // [추가] 생성된 파라미터를 클라이언트에 적용
                .build();

        // Multipart/form-data 요청 준비
        HttpRequest.BodyPublisher bodyPublisher = ofMimeMultipartData(filePath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://172.31.57.21:5000/api/extract-embedding"))
                .header("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                .POST(bodyPublisher)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Python 서버에서 { "vectorPath": "..." } 형식으로 응답한다고 가정
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> result = objectMapper.readValue(response.body(), Map.class);
        System.out.println(result);

        return result.get("vectorPath");
    }

    // Multipart/form-data 바디 생성 메서드
    private static HttpRequest.BodyPublisher ofMimeMultipartData(Path filePath) throws IOException {
        var boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        var byteArrays = new ArrayList<byte[]>();

        // 파일 part
        String fileHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath.getFileName() + "\"\r\n" +
                "Content-Type: image/png\r\n\r\n";
        byteArrays.add(fileHeader.getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(filePath));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

        // 마지막 boundary
        String endBoundary = "--" + boundary + "--\r\n";
        byteArrays.add(endBoundary.getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    @PostMapping(value = "/face-login", consumes = "application/json")
    public ResponseEntity<String> faceLogin(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = payload.get("email");

        // 1️⃣ JPA로 사용자 조회
        Member member = memberService.findByEmail(email);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("회원 정보가 없습니다.");
        }

        // --- [추가] 로그인 성공 시, 마지막 로그인 시각 업데이트 ---
        // 인증 처리 직전에 서비스 호출
        memberService.updateLastLoginAt(email);
        // ---------------------------------------------------

        CustomUserDetails userDetails = new CustomUserDetails(member, member.getAuthorities());

        // 2️⃣ Spring Security 수동 인증 처리
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, member.getAuthorities());

        // 3️⃣ SecurityContext 객체 생성 + 인증 정보 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);

        // 4️⃣ SecurityContextHolder에도 넣어주기 (optional)
        SecurityContextHolder.setContext(context);

        // 5️⃣ 세션에 SecurityContext 저장
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return ResponseEntity.ok("얼굴 로그인 성공!");
    }

}
