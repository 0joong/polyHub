package com.polyHub.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class FirebaseConfig {

    // application.properties에 설정된 키 파일 경로를 읽어옵니다.
    // 예: firebase.key.path=firebase-admin-key.json
    @Value("${firebase.key.path}")
    private String firebaseKeyPath;

    /**
     * Firebase Admin SDK를 초기화하고, FirebaseApp Bean을 생성합니다.
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 클래스패스에서 비공개 키 파일을 읽어옵니다.
        ClassPathResource resource = new ClassPathResource(firebaseKeyPath);

        try (InputStream serviceAccount = resource.getInputStream()) {
            // 이미 초기화되었는지 확인하여, 중복 초기화를 방지합니다.
            List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
            if (firebaseApps != null && !firebaseApps.isEmpty()) {
                for (FirebaseApp app : firebaseApps) {
                    if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                        return app;
                    }
                }
            }

            // Firebase 옵션을 설정합니다.
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // FirebaseApp을 초기화합니다.
            return FirebaseApp.initializeApp(options);
        }
    }

    /**
     * 초기화된 FirebaseApp을 사용하여, 메시징 서비스를 위한 FirebaseMessaging Bean을 생성합니다.
     * 이 Bean을 다른 서비스에서 주입받아 알림을 보낼 수 있습니다.
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        return FirebaseMessaging.getInstance(firebaseApp());
    }
}
