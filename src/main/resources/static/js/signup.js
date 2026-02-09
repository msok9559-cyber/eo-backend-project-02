
document.addEventListener('DOMContentLoaded', function() {

// 중복 체크 상태
    let usernameChecked = false;
    let nicknameChecked = false;
    let emailVerified = false;

// 아이디 중복 체크
    document.getElementById('check-username').addEventListener('click', function() {
        const username = document.getElementById('username').value;

        if (!username) {
            alert('아이디를 입력해주세요.');
            return;
        }

        // 중복 체크 API 호출
        fetch('/check-username?username=' + encodeURIComponent(username))
            .then(response => response.json())
            .then(available => {
                if (available) {
                    alert('사용 가능한 아이디입니다.');
                    usernameChecked = true;
                } else {
                    alert('이미 사용중인 아이디입니다.');
                    usernameChecked = false;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('중복 체크 중 오류가 발생했습니다.');
            });
    });

// 닉네임 중복 체크
    document.getElementById('check-nickname').addEventListener('click', function() {
        const nickname = document.getElementById('nickname').value;

        if (!nickname) {
            alert('닉네임을 입력해주세요.');
            return;
        }

        // 중복 체크 API 호출
        fetch('/check-nickname?nickname=' + encodeURIComponent(nickname))
            .then(response => response.json())
            .then(available => {
                if (available) {
                    alert('사용 가능한 닉네임입니다.');
                    nicknameChecked = true;
                } else {
                    alert('이미 사용중인 닉네임입니다.');
                    nicknameChecked = false;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('중복 체크 중 오류가 발생했습니다.');
            });
    });

// 이메일 인증 요청
    document.getElementById('verify-email').addEventListener('click', function() {
        const email = document.getElementById('email').value;

        if (!email) {
            alert('이메일을 입력해주세요.');
            return;
        }

        // 이메일 형식 검증
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            alert('올바른 이메일 형식이 아닙니다.');
            return;
        }

        // 버튼 비활성화 (중복 클릭 방지)
        const btn = document.getElementById('verify-email');
        btn.disabled = true;
        btn.textContent = '발송중...';

        // 실제 API 호출
        fetch('/send-verification-email?email=' + encodeURIComponent(email))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    // 인증번호 입력칸 보이기
                    document.getElementById('verify-row').style.display = 'flex';
                    btn.textContent = '재발송';
                    btn.disabled = false;
                } else {
                    alert(data.message);
                    btn.disabled = false;
                    btn.textContent = '인증';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('인증번호 발송에 실패했습니다.');
                btn.disabled = false;
                btn.textContent = '인증';
            });
    });

// 인증번호 확인
    document.getElementById('verify-confirm').addEventListener('click', function() {
        const email = document.getElementById('email').value;
        const inputCode = document.getElementById('verify-code').value;

        if (!inputCode) {
            alert('인증번호를 입력해주세요.');
            return;
        }

        // 버튼 비활성화
        const btn = document.getElementById('verify-confirm');
        btn.disabled = true;
        btn.textContent = '확인중...';

        // 실제 API 호출
        fetch('/verify-email-code?email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(inputCode))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    emailVerified = true;

                    // 인증 완료 후 입력칸 비활성화
                    document.getElementById('email').disabled = true;
                    document.getElementById('verify-code').disabled = true;
                    document.getElementById('verify-email').disabled = true;
                    document.getElementById('verify-confirm').disabled = true;
                } else {
                    alert(data.message);
                    emailVerified = false;
                    btn.disabled = false;
                    btn.textContent = '인증확인';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('인증 확인에 실패했습니다.');
                btn.disabled = false;
                btn.textContent = '인증확인';
            });
    });

// 폼 제출 시 검증
    document.getElementById('signup-form').addEventListener('submit', function(e) {
        // 비밀번호 확인
        const password = document.getElementById('password').value;
        const passwordConfirm = document.getElementById('password-confirm').value;

        if (password !== passwordConfirm) {
            e.preventDefault();
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }

        // 아이디 중복 체크 확인
        if (!usernameChecked) {
            e.preventDefault();
            alert('아이디 중복 확인을 해주세요.');
            return;
        }

        // 닉네임 중복 체크 확인
        if (!nicknameChecked) {
            e.preventDefault();
            alert('닉네임 중복 확인을 해주세요.');
            return;
        }

        // 이메일 인증 확인
        if (!emailVerified) {
            e.preventDefault();
            alert('이메일 인증을 완료해주세요.');
            return;
        }

        // 필수 약관 체크
        const termsPrivacy = document.getElementById('terms-privacy').checked;
        const termsService = document.getElementById('terms-service').checked;

        if (!termsPrivacy || !termsService) {
            e.preventDefault();
            alert('필수 약관에 동의해주세요.');
            return;
        }
    });

// 아이디/닉네임 변경 시 중복 체크 상태 초기화
    document.getElementById('username').addEventListener('input', function() {
        usernameChecked = false;
    });

    document.getElementById('nickname').addEventListener('input', function() {
        nicknameChecked = false;
    });

// 이메일 변경 시 인증 상태 초기화
    document.getElementById('email').addEventListener('input', function() {
        emailVerified = false;
        document.getElementById('verify-row').style.display = 'none';
        document.getElementById('verify-code').value = '';

        // 버튼 상태 초기화
        const requestBtn = document.getElementById('verify-email');
        requestBtn.disabled = false;
        requestBtn.textContent = '인증';
    });

});