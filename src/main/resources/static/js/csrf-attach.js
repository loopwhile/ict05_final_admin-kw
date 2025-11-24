// /static/js/lib/csrf-attach.js
// axios / fetch / jQuery 요청에 CSRF 헤더 자동 부착
// 👉 외부 도메인(CDN/Google 등)에는 절대 붙이지 않음 (same-origin + state-changing 만)

(function () {
  if (window.__CSRF_ATTACH_DONE__) return;
  window.__CSRF_ATTACH_DONE__ = true;

  function readMeta() {
    const tokenEl  = document.querySelector('meta[name="_csrf"]');
    const headerEl = document.querySelector('meta[name="_csrf_header"]');
    return {
      token:  tokenEl && tokenEl.content,
      header: headerEl && headerEl.content
    };
  }

  function isSafeMethod(m) {
    const method = (m || 'GET').toUpperCase();
    return ['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method);
  }

  /* ===== [NEW] same-origin 판별 ===== */
  function isSameOrigin(url) {
    try {
      return new URL(url, location.origin).origin === location.origin;
    } catch {
      // 상대경로 등은 같은 출처로 간주
      return true;
    }
  }

  const META = readMeta();
  if (!META.token || !META.header) return;

  /* ---- axios ---- */
  function attachToAxios(instance) {
    if (!instance || instance.__csrfAttached) return;

    instance.interceptors.request.use((config) => {
      const method = (config.method || 'get').toUpperCase();

      /* ===== [CHANGED] same-origin URL 계산 추가 ===== */
      let fullUrl = config.url || '';
      try {
        const base = config.baseURL || location.origin;
        fullUrl = new URL(config.url || '', base).href;
      } catch { /* no-op */ }

      /* ===== [CHANGED] same-origin + state-changing 에만 주입 ===== */
      if (!isSafeMethod(method) && isSameOrigin(fullUrl)) {
        config.headers = config.headers || {};
        if (!config.headers[META.header]) config.headers[META.header] = META.token;
        // 세션 쿠키 필요 시 same-origin 기본 전송
        if (config.withCredentials == null) config.withCredentials = true;
      }
      return config;
    });

    instance.__csrfAttached = true;
  }

  attachToAxios(window.api);
  attachToAxios(window.axios);

  /* ---- fetch ---- */
  function attachToFetch() {
    if (!window.fetch || window.__FETCH_CSRF_PATCHED__) return;

    const originalFetch = window.fetch;
    window.fetch = function (input, init = {}) {

      /* ===== [CHANGED] method / url 추출 개선 ===== */
      const method = (init.method || (input instanceof Request ? input.method : 'GET')).toUpperCase();
      const urlStr =
        (input instanceof Request) ? input.url :
        (typeof input === 'string') ? input :
        (input && input.url) || '';

      const sameOrigin = isSameOrigin(urlStr);
      const stateChanging = !isSafeMethod(method);

      /* ===== [CHANGED] same-origin + state-changing 에만 CSRF 추가 ===== */
      if (stateChanging && sameOrigin) {
        // 기존 헤더 + init 헤더 머지
        const headers = new Headers(input instanceof Request ? input.headers : undefined);
        const extra   = new Headers(init.headers || undefined);
        extra.forEach((v, k) => headers.set(k, v));

        if (!headers.has(META.header)) headers.set(META.header, META.token);

        const nextInit = {
          ...init,
          headers,
          credentials: init.credentials ?? 'same-origin'
        };

        if (input instanceof Request) {
          const req = new Request(input, nextInit);
          return originalFetch(req);
        }
        return originalFetch(urlStr || input, nextInit);
      }

      // 외부 도메인 or 안전 메서드는 수정 없이 통과
      return originalFetch(input, init);
    };

    window.__FETCH_CSRF_PATCHED__ = true;
  }

  attachToFetch();

  /* ---- jQuery (선택) ---- */
  if (window.$) {
    $.ajaxSetup({
      beforeSend: function (xhr, settings) {
        const m = (settings.type || 'GET').toUpperCase();
        /* ===== [CHANGED] same-origin 체크 추가 ===== */
        if (!isSafeMethod(m) && isSameOrigin(settings.url)) {
          xhr.setRequestHeader(META.header, META.token);
        }
      }
    });
  }
})();
