/**
 * [2026-02-12] 쪽지 상세 조회 및 삭제 분기 로직
 */
document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const messageId = urlParams.get('id');
    const currentType = urlParams.get('type') || 'all';

    let currentMessageData = null;

    if (!messageId) {
        alert('잘못된 접근입니다.');
        location.href = '/messages/all';
        return;
    }

    // 1. 데이터 로드 (Ajax)
    fetch(`/messages/api/read?id=${messageId}`)
        .then(res => {
            if (!res.ok) throw new Error('권한이 없거나 쪽지를 찾을 수 없습니다.');
            return res.json();
        })
        .then(data => {
            currentMessageData = data;

            // 데이터 바인딩
            document.getElementById('senderName').textContent = data.senderNickname;
            document.getElementById('receiverName').textContent = data.receiverNickname;
            document.getElementById('sendDate').textContent = data.createdAt.replace('T', ' ').substring(0, 16);
            document.getElementById('detailSubject').textContent = data.title;
            document.getElementById('detailBody').textContent = data.content;

            // 읽은 시간 표시 처리
            if (data.isRead === 1 && data.readedAt) {
                const readDateRow = document.getElementById('readDateRow');
                const readDate = document.getElementById('readDate');
                if (readDateRow) {
                    readDateRow.style.display = 'flex';
                    readDate.textContent = data.readedAt.replace('T', ' ').substring(0, 16);
                }
            }

            // 답장 버튼 활성화 제어
            const replyBtn = document.getElementById('replyBtn');
            if (replyBtn) {
                if (data.type === 'SENT') {
                    replyBtn.style.display = 'none';
                } else {
                    replyBtn.onclick = () => {
                        location.href = `/messages/write?target=${encodeURIComponent(data.senderNickname)}`;
                    };
                }
            }
        })
        .catch(err => {
            alert(err.message);
            history.back();
        });

    // 2. 삭제 버튼 클릭 (일반 삭제 vs 영구 삭제 분기)
    const trashBtn = document.getElementById('trashBtn');
    if (trashBtn) {
        trashBtn.onclick = () => {
            if (!currentMessageData) return;

            const isTrashContext = currentType === 'trash';
            const confirmMsg = isTrashContext ? '이 쪽지를 영구 삭제하시겠습니까?' : '이 쪽지를 삭제하시겠습니까?';

            if (confirm(confirmMsg)) {
                const userType = currentMessageData.type.toLowerCase();

                // 휴지통 페이지면 delete, 아니면 trash 호출
                const apiUrl = isTrashContext
                    ? `/messages/api/delete?id=${messageId}&userType=${userType}`
                    : `/messages/api/trash?id=${messageId}&userType=${userType}`;

                fetch(apiUrl, { method: 'POST' })
                    .then(res => {
                        if (res.ok) {
                            alert('삭제되었습니다.');
                            location.href = `/messages/${currentType}`;
                        } else {
                            alert('삭제 실패. 컨트롤러의 단일 삭제 API를 확인하세요.');
                        }
                    })
                    .catch(err => console.error('Delete error:', err));
            }
        };
    }
});