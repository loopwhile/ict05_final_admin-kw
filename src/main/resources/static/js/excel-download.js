(function (w, d) {
  function excelDownload(btn) {
    const ep = btn?.getAttribute('data-excel-endpoint');
    if (!ep) { console.warn('data-excel-endpoint missing'); return; }

    const base = location.pathname.startsWith('/admin') ? '/admin' : '';
    const endpoint = ep.startsWith('/admin/') ? ep : (base + ep);

    const params = new URLSearchParams(location.search);
    const formId = btn?.getAttribute('data-form-id') || 'frm';
    const form = d.getElementById(formId);
    if (form) {
      const fd = new FormData(form);
      for (const [k, v] of fd.entries()) { params.delete(k); if (v !== '') params.set(k, v); }
    }
    for (const [k, v] of Array.from(params.entries())) if (v === '') params.delete(k);

    w.location.href = endpoint + (params.toString() ? `?${params}` : '');
  }
  w.excelDownload = excelDownload;               // 전역 노출
})(window, document);
