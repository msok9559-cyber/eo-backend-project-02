document.addEventListener('DOMContentLoaded', () => {
    const writeForm = document.getElementById('writeForm');

    if (!writeForm) return;

    writeForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // CSRF 토큰 가져오기
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        const messageData = {
            receiverNickname: document.getElementById('receiverNickname').value.trim(),
            title: document.getElementById('title').value.trim(),
            content: document.getElementById('content').value.trim()
        };

        // 입력값 검증
        if (!messageData.receiverNickname) {
            alert('받는 사람 닉네임을 입력하세요.');
            return;
        }
        if (!messageData.title) {
            alert('제목을 입력하세요.');
            return;
        }
        if (!messageData.content) {
            alert('내용을 입력하세요.');
            return;
        }

        const headers = {
            'Content-Type': 'application/json'
        };

        // CSRF 토큰이 있으면 헤더에 추가
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        fetch('/messages/api/write', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(messageData)
        })
            .then(async response => {
                console.log('Response Status:', response.status);

                if (response.ok) {
                    // 성공 시 알림 후 보낸 쪽지함으로 이동
                    alert('쪽지를 보냈습니다.');
                    location.href = '/messages/sent';
                } else {
                    const text = await response.text();
                    console.log('Response Text:', text);

                    try {
                        const responseData = JSON.parse(text);

                        if (response.status === 400) {
                            let errorMessage = '입력 값을 확인해주세요:\n';
                            for (const key in responseData) {
                                errorMessage += `- ${responseData[key]}\n`;
                            }
                            alert(errorMessage);
                        } else {
                            alert(responseData.message || '존재하지 않는 닉네임입니다.');
                        }
                    } catch (e) {
                        alert(`오류 (${response.status}): 서버에 문제가 발생했습니다.`);
                    }
                }
            })
            .catch(err => {
                console.error('Fetch Error:', err);
                alert('통신 중 오류가 발생했습니다.');
            });
    });
});