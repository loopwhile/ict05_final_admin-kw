// /static/js/fcm-web.js (ES Module, 단일 onMessage, 스코프 정리판)
import { initializeApp } from "https://www.gstatic.com/firebasejs/9.22.1/firebase-app-compat.js";
import { isSupported, getMessaging, getToken, onMessage } from "https://www.gstatic.com/firebasejs/9.22.1/firebase-messaging-compat.js";

// Firebase config는 head.html에서 window.firebaseConfig로 주입됩니다.
// VAPID_KEY는 head.html의 meta 태그에서 가져옵니다.

let app;
let messaging;

try {
    // window.firebaseConfig를 사용하여 Firebase 앱 초기화
    app = initializeApp(window.firebaseConfig);
    messaging = getMessaging(app);
    console.log("[FCM] Firebase initialized successfully.");
} catch (error) {
    console.error("[FCM] Error initializing Firebase:", error);
}

/**
 * 기기 식별자를 가져옵니다 (예: localStorage에 저장된 UUID).
 * 없으면 새로 생성하여 저장합니다.
 * @returns {string} 디바이스 식별자
 */
function getDeviceId() {
    let deviceId = localStorage.getItem("fcm_device_id");
    if (!deviceId) {
        deviceId = crypto.randomUUID(); // UUID 생성 (현대 브라우저 지원)
        localStorage.setItem("fcm_device_id", deviceId);
    }
    return deviceId;
}

/**
 * 현재 플랫폼 타입을 반환합니다. (WEB)
 * @returns {string} 플랫폼 타입
 */
function getPlatformType() {
    return "WEB";
}

/**
 * 웹 푸시를 활성화하고 FCM 토큰을 백엔드에 등록합니다.
 * @returns {Promise<string|null>} 등록된 FCM 토큰 또는 null
 */
export async function enableWebPush() {
    if (!(await isSupported())) {
        console.warn('[FCM] Web Push not supported in this browser.');
        return null;
    }
    if (!messaging) {
        console.error("[FCM] Firebase Messaging is not initialized.");
        return null;
    }
    const VAPID_KEY = (document.querySelector('meta[name="vapid-key"]')?.content || '').trim();
    if (!VAPID_KEY) {
        console.error("[FCM] VAPID public key is not configured. Please set meta[name=\"vapid-key\"].");
        return null;
    }

    try {
        const perm = await Notification.requestPermission();
        if (perm !== 'granted') {
            console.warn('[FCM] Notification permission denied');
            return null;
        }

        // 서비스워커 등록 (스코프는 /admin/ 유지)
        const reg = await navigator.serviceWorker.register('/admin/firebase-messaging-sw.js', { scope: '/admin/' });

        const token = await getToken(messaging, { vapidKey: VAPID_KEY, serviceWorkerRegistration: reg });
        if (!token) {
            console.warn('[FCM] Failed to get token');
            return null;
        }

        // Axios를 사용하여 백엔드에 토큰 등록 (csrf-attach.js가 CSRF 헤더를 자동으로 추가)
        await axios.post("/fcm/register", {
            token: token,
            deviceId: getDeviceId(),
            appType: 'HQ',
            platform: getPlatformType()
        });

        localStorage.setItem('fcm_token', token);
        console.log('[FCM] token registered', token);
        return token;

    } catch (error) {
        console.error("[FCM] Error enabling web push or registering token:", error);
        return null;
    }
}

/**
 * FCM 토큰을 백엔드에서 등록 해제하고 Firebase에서 삭제합니다.
 * @param {string|null} token - 등록 해제할 FCM 토큰 (선택 사항, 없으면 localStorage에서 가져옴)
 */
export async function unregisterWebPush(token = null) {
    if (!messaging) {
        console.error("[FCM] Firebase Messaging is not initialized.");
        return;
    }

    let currentToken = token || localStorage.getItem('fcm_token');
    if (!currentToken) {
        currentToken = await messaging.getToken(); // 현재 활성 토큰 가져오기 시도
    }

    if (!currentToken) {
        console.warn("[FCM] No FCM token found to unregister.");
        return;
    }

    try {
        // 백엔드에서 토큰 삭제 (csrf-attach.js가 CSRF 헤더를 자동으로 추가)
        await axios.delete(`/fcm/register/${currentToken}`);
        console.log("[FCM] Token unregistered from backend:", currentToken);

        // Firebase에서 토큰 삭제
        await messaging.deleteToken(currentToken);
        localStorage.removeItem('fcm_token');
        console.log("[FCM] Token deleted from Firebase:", currentToken);

    } catch (error) {
        console.error("[FCM] Error unregistering web push or deleting token:", error);
    }
}

// FCM 토큰 변경 시 처리 (새 토큰을 백엔드에 등록)
if (messaging) {
    onMessage(messaging, (payload) => {
        console.log("[FCM] Message received in foreground. ", payload);
        const notificationTitle = payload.notification?.title || payload.data?.title || "알림";
        const notificationOptions = {
            body: payload.notification?.body || payload.data?.body || "",
            icon: payload.notification?.icon || payload.data?.icon || '/admin/images/fcm/toastlab.png',
            badge: payload.notification?.badge || payload.data?.badge || '/admin/images/fcm/badge-72.png',
            data: payload.data,
            requireInteraction: true // 사용자가 클릭할 때까지 알림 유지
        };

        // 브라우저 알림 표시 (Service Worker가 아닌 메인 스레드에서)
        if (Notification.permission === "granted") {
            new Notification(notificationTitle, notificationOptions);
        } else {
            // 권한이 없으면 토스트 등으로 표시 (프로젝트의 showToast 함수 사용)
            if (typeof showToast === 'function') {
                showToast(notificationTitle + ": " + notificationOptions.body, "info");
            } else {
                alert(notificationTitle + ": " + notificationOptions.body);
            }
        }
    });

    // 토큰 새로고침 처리
    messaging.onTokenRefresh(() => {
        console.log("[FCM] Token refresh event triggered.");
        enableWebPush(); // 새 토큰을 가져와 백엔드에 등록
    });
}

// DOM 로드 후 초기화 및 자동 활성화 시도
document.addEventListener('DOMContentLoaded', async () => {
    if ("serviceWorker" in navigator && "PushManager" in window) {
        // 권한이 이미 있고, 로컬 스토리지에 토큰이 있으면 자동 업서트
        if (Notification.permission === 'granted' && localStorage.getItem('fcm_token')) {
            try {
                await enableWebPush();
            } catch (e) {
                console.warn('[FCM] auto-enable failed:', e);
            }
        }
    } else {
        console.warn("Service Worker or Push API not supported by this browser.");
    }
});
