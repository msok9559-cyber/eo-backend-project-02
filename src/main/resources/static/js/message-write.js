/**
 * 쪽지 작성 및 전송 로직
 * 유효성 검사 에러(400) 및 런타임 에러 상세 메시지 출력 유지
 * receiverNickname 필드 기반 전송
 */
document.addEventListener('DOMContentLoaded', () => {
    const writeForm = document.getElementById('writeForm');

    if (!writeForm) return;

    writeForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // 서비스 로직에 맞춰 receiverNickname으로 데이터 구성
        const messageData = {
            receiverNickname: document.getElementById('receiverNickname').value,
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        };

        // MessageController @PostMapping("/api/write") 호출
        fetch('/messages/api/write', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(messageData)
        })
            .then(async response => {
                if (response.ok) {
                    // 성공 시 알림 후 보낸 쪽지함으로 이동
                    alert('쪽지를 보냈습니다.');
                    location.href = '/messages/sent';
                } else {
                    // 400 Bad Request 등 에러 발생 시 처리
                    const responseData = await response.json();

                    if (response.status === 400) {
                        // 유효성 검사 에러 (Map 형태: 필드명 - 에러메시지) 처리
                        let errorMessage = '입력 값을 확인해주세요:\n';
                        for (const key in responseData) {
                            errorMessage += `- ${responseData[key]}\n`;
                        }
                        alert(errorMessage);
                    } else {
                        // 기타 런타임 에러 (RuntimeException 등) 처리
                        alert(responseData.message || '존재하지 않는 닉네임이거나 발송할 수 없습니다.');
                    }
                }
            })
            .catch(err => {
                console.error('Submit Error:', err);
                alert('통신 중 오류가 발생했습니다.');
            });
    });
});