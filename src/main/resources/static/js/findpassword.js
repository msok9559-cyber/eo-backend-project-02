
document.addEventListener('DOMContentLoaded', function() {

    var csrfToken = document.querySelector("meta[name='_csrf']").content;
    var csrfHeader = document.querySelector("meta[name='_csrf_header']").content;

    // DOM 요소
    var emailInput = document.getElementById('email');
    var verifyEmailBtn = document.getElementById('verify-email');
    var verifyCodeInput = document.getElementById('verify-code');
    var verifyConfirmBtn = document.getElementById('verify-confirm');
    var newPasswordInput = document.getElementById('new-password');
    var passwordConfirmInput = document.getElementById('password-confirm');
    var submitBtn = document.getElementById('submit-btn');
    var errorMessage = document.getElementById('error-message');

    var verifyRow = document.getElementById('verify-row');
    var passwordRow = document.getElementById('password-row');
    var passwordConfirmRow = document.getElementById('password-confirm-row');

    // 상태 관리
    var emailVerified = false;
    var currentEmail = '';

    // 에러 메시지 표시
    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.className = '';
        errorMessage.style.display = 'block';
    }

    // 에러 메시지 숨김
    function hideError() {
        errorMessage.style.display = 'none';
    }

    // 성공 메시지 표시
    function showSuccess(message) {
        errorMessage.textContent = message;
        errorMessage.className = 'success-message';
        errorMessage.style.display = 'block';
    }

    // 1단계: 이메일 인증 요청
    verifyEmailBtn.addEventListener('click', function() {
        var email = emailInput.value.trim();

        hideError();

        if (!email) {
            showError('이메일을 입력해주세요.');
            return;
        }

        if (!validateEmail(email)) {
            showError('올바른 이메일 형식이 아닙니다.');
            return;
        }

        // 인증 요청
        verifyEmailBtn.disabled = true;
        verifyEmailBtn.textContent = '발송중...';

        fetch('/api/email/send-verification?email=' + encodeURIComponent(email))
            .then(function(response) {
                return response.json();
            })
            .then(function(data) {
                if (data.success) {
                    showSuccess('인증번호가 발송되었습니다.');
                    currentEmail = email;
                    emailInput.disabled = true;
                    verifyRow.style.display = 'flex';
                    verifyEmailBtn.textContent = '재발송';
                    verifyEmailBtn.disabled = false;
                } else {
                    showError(data.message || '인증번호 발송에 실패했습니다.');
                    verifyEmailBtn.disabled = false;
                    verifyEmailBtn.textContent = '인증';
                }
            })
            .catch(function(error) {
                console.error('Error:', error);
                showError('서버 오류가 발생했습니다.');
                verifyEmailBtn.disabled = false;
                verifyEmailBtn.textContent = '인증';
            });
    });

    // 2단계: 인증번호 확인
    verifyConfirmBtn.addEventListener('click', function() {
        var code = verifyCodeInput.value.trim();

        hideError();

        if (!code || code.length !== 6) {
            showError('6자리 인증번호를 입력해주세요.');
            return;
        }

        verifyConfirmBtn.disabled = true;
        verifyConfirmBtn.textContent = '확인중...';

        fetch('/api/email/verify-code?email=' + encodeURIComponent(currentEmail) + '&code=' + encodeURIComponent(code))
            .then(function(response) {
                return response.json();
            })
            .then(function(data) {
                if (data.success) {
                    showSuccess('이메일 인증이 완료되었습니다.');
                    emailVerified = true;

                    // 인증 완료 후 비밀번호 입력 폼 표시
                    verifyCodeInput.disabled = true;
                    verifyConfirmBtn.disabled = true;

                    passwordRow.style.display = 'flex';
                    passwordConfirmRow.style.display = 'flex';
                    submitBtn.style.display = 'block';

                } else {
                    showError(data.message || '인증번호가 일치하지 않습니다.');
                    verifyConfirmBtn.disabled = false;
                    verifyConfirmBtn.textContent = '인증확인';
                }
            })
            .catch(function(error) {
                console.error('Error:', error);
                showError('서버 오류가 발생했습니다.');
                verifyConfirmBtn.disabled = false;
                verifyConfirmBtn.textContent = '인증확인';
            });
    });

    // 3단계: 비밀번호 변경
    submitBtn.addEventListener('click', function() {
        var newPassword = newPasswordInput.value;
        var passwordConfirm = passwordConfirmInput.value;

        hideError();

        // 유효성 검사
        if (!newPassword || !passwordConfirm) {
            showError('모든 필드를 입력해주세요.');
            return;
        }

        if (newPassword.length < 8 || newPassword.length > 20) {
            showError('비밀번호는 8~20자로 입력해주세요.');
            return;
        }

        if (newPassword !== passwordConfirm) {
            showError('비밀번호가 일치하지 않습니다.');
            return;
        }

        if (!emailVerified) {
            showError('이메일 인증을 먼저 완료해주세요.');
            return;
        }

        submitBtn.disabled = true;
        submitBtn.textContent = '변경중...';

        fetch('/api/user/reset-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                email: currentEmail,
                newPassword: newPassword
            })
        })
            .then(function(response) {
                return response.json();
            })
            .then(function(data) {
                if (data.success) {
                    alert('비밀번호가 변경되었습니다. 로그인 페이지로 이동합니다.');
                    window.location.href = '/login';
                } else {
                    showError(data.message || '비밀번호 변경에 실패했습니다.');
                    submitBtn.disabled = false;
                    submitBtn.textContent = '비밀번호 변경';
                }
            })
            .catch(function(error) {
                console.error('Error:', error);
                showError('서버 오류가 발생했습니다.');
                submitBtn.disabled = false;
                submitBtn.textContent = '비밀번호 변경';
            });
    });

    // 이메일 유효성 검사
    function validateEmail(email) {
        var re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }
});