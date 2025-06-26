// 서비스 워커는 모듈(import/export)을 지원하지 않으므로,
// v9 이전 방식과 호환되는 라이브러리를 사용해야 합니다.
importScripts("https://www.gstatic.com/firebasejs/9.22.1/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/9.22.1/firebase-messaging-compat.js");

// 1. Firebase 프로젝트 설정 (header.html의 설정과 동일해야 합니다)
const firebaseConfig = {
    apiKey: "AIzaSyC4PP73Rr5s01kU3TM6_7PGG8Z6_B7Y-hI",
    authDomain: "polyhub-b0f5a.firebaseapp.com",
    projectId: "polyhub-b0f5a",
    storageBucket: "polyhub-b0f5a.firebasestorage.app",
    messagingSenderId: "78068345438",
    appId: "1:78068345438:web:cdf00e932f7e6ce42de2cf",
    measurementId: "G-J8DRZEYRWH"
};

// 2. Firebase 앱 초기화
firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

/**
 * 3. 백그라운드에서 메시지를 처리하는 핸들러
 * 웹사이트 탭이 닫혀 있거나, 브라우저가 최소화되어 있을 때
 * FCM 서버로부터 오는 메시지를 항상 '청취'하고 있다가 알림을 띄웁니다.
 */
messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);

    // 알림에 표시될 제목과 내용을 payload에서 추출합니다.
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/images/notification_icon.png' // 알림에 표시될 아이콘 (예시)
    };

    // 브라우저의 Notification API를 사용하여 사용자에게 데스크탑 알림을 보여줍니다.
    self.registration.showNotification(notificationTitle, notificationOptions);
});
