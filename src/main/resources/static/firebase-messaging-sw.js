// src/main/resources/static/firebase-messaging-sw.js

// Firebase SDK (CDN)
importScripts('https://www.gstatic.com/firebasejs/9.22.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.22.1/firebase-messaging-compat.js');

// TODO: Firebase 프로젝트 설정 (head.html의 window.firebaseConfig와 동일하게 설정)
// 서비스 워커는 window 객체에 접근할 수 없으므로 여기에 직접 정의해야 합니다.
const firebaseConfig = {
    apiKey: "AIzaSyA7m5jVdo-w7TBG6h6wW4h6mc5gbNjqYlU",
    authDomain: "ict05-final.firebaseapp.com",
    projectId: "ict05-final",
    storageBucket: "ict05-final.firebasestorage.app",
    messagingSenderId: "382264607725",
    appId: "1:382264607725:web:da28516c4a49f92e045de4",
    measurementId: "G-YEHZ8996H8"
};

// Firebase 앱 초기화
const app = firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// 백그라운드 메시지 수신 처리
messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);

    const notificationTitle = payload.notification?.title || payload.data?.title || "알림";
    const notificationOptions = {
        body: payload.notification?.body || payload.data?.body || "",
        icon: payload.notification?.icon || payload.data?.icon || '/admin/images/fcm/toastlab.png',
        badge: payload.notification?.badge || payload.data?.badge || '/admin/images/fcm/badge-72.png',
        data: payload.data,
        requireInteraction: true // 사용자가 클릭할 때까지 알림 유지
    };

    // 알림 클릭 시 동작 정의
    self.addEventListener('notificationclick', (event) => {
        event.notification.close(); // 알림 닫기
        const clickedNotification = event.notification;
        const link = clickedNotification.data?.link || '/admin'; // 알림에 포함된 링크 또는 기본 링크

        event.waitUntil(
            clients.openWindow(link) // 새 탭으로 링크 열기
        );
    });

    return self.registration.showNotification(notificationTitle, notificationOptions);
});