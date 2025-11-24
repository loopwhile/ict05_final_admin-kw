// // DOM 참조 (id는 화면과 동일하게 맞춰주세요)
// const $email     = document.getElementById('email');
// const $emailHelp = document.getElementById('emailHelp');
// const $submit    = document.getElementById('submitBtn');
//
// // 다른 항목 검증 결과와 함께 사용할 플래그들
// let nameOk  = false;
// let phoneOk = false;
// let emailOk = false;
// let pwOk    = false;
// let matchOk = false;
//
// function updateBtn() {
//     $submit.disabled = !(nameOk && phoneOk && emailOk && pwOk && matchOk);
// }
//
// // 여기 ⬇️ 제가 준 함수 붙여넣기
// async function checkEmailDup() {
//     const v = $email.value.trim();
//     const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
//
//     if (!emailRe.test(v)) {
//         $emailHelp.textContent = '이메일 형식을 확인해주세요.';
//         $emailHelp.className = 'help text-red-600';
//         emailOk = false;
//         return updateBtn();
//     }
//
//     try {
//         const res = await fetch(`/admin/api/auth/exist?email=${encodeURIComponent(v)}`, {
//             headers: { 'Accept': 'application/json' }
//         });
//         if (!res.ok) throw new Error(`HTTP ${res.status}`);
//         const data = await res.json(); // {exists: boolean}
//
//         if (data.exists) {
//             $emailHelp.textContent = '이미 사용 중인 이메일입니다.';
//             $emailHelp.className = 'help text-red-600';
//             emailOk = false;
//         } else {
//             $emailHelp.textContent = '사용 가능한 이메일입니다.';
//             $emailHelp.className = 'help text-green-600';
//             emailOk = true;
//         }
//     } catch (e) {
//         console.error('exist check failed:', e);
//         $emailHelp.textContent = '중복 확인 실패';
//         $emailHelp.className = 'help text-red-600';
//         emailOk = false;
//     } finally {
//         updateBtn();
//     }
// }
//
// // 이벤트 연결
// $email.addEventListener('blur',  checkEmailDup);
// $email.addEventListener('input', checkEmailDup);
//
// // 초기 비활성화
// updateBtn();
