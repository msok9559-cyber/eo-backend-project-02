/**
 * 쪽지 목록 기능 스크립트
 * - 목록 선택, 상세보기 이동, 대량 삭제(일반/영구) 처리
 * - CSRF 토큰은 message-com.js에서 처리
 */
document.addEventListener('DOMContentLoaded', function() {
    // 1. 행 클릭 시 상세보기 이동 (체크박스 클릭 시 제외)
    const messageRows = document.querySelectorAll('.messageRow');
    messageRows.forEach(row => {
        row.addEventListener('click', function(e) {
            if (e.target.classList.contains('rowCheck') || e.target.type === 'checkbox') {
                return;
            }
            const messageId = this.getAttribute('data-id');
            const pathParts = window.location.pathname.split('/');
            const currentType = pathParts[pathParts.length - 1] || 'all';

            location.href = `/messages/read?id=${messageId}&type=${currentType}`;
        });
    });

    // 2. 전체 선택 체크박스 로직
    const checkAll = document.getElementById('checkAll');
    const rowChecks = document.querySelectorAll('.rowCheck');
    if (checkAll) {
        checkAll.addEventListener('change', function() {
            rowChecks.forEach(cb => cb.checked = this.checked);
        });
    }

    // 3. 선택 삭제 로직 (Bulk Delete/Trash)
    const deleteBtn = document.getElementById('deleteButton');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function() {
            const checkedBoxes = document.querySelectorAll('.rowCheck:checked');

            if (checkedBoxes.length === 0) {
                alert('삭제할 쪽지를 선택해주세요.');
                return;
            }

            // 선택된 각 행의 정보 수집
            const selectedRows = Array.from(checkedBoxes)
                .map(cb => cb.closest('.messageRow'));

            const selectedIds = selectedRows.map(row => parseInt(row.getAttribute('data-id')));
            const selectedTypes = selectedRows.map(row => row.getAttribute('data-type'));

            console.log('=== 선택 삭제 정보 ===');
            console.log('Selected IDs:', selectedIds);
            console.log('Selected Types:', selectedTypes);
            console.log('====================');

            // 현재 위치가 휴지통인지 확인
            const isTrashPage = window.location.pathname.includes('/trash');
            const confirmMsg = isTrashPage ?
                `선택한 ${selectedIds.length}개의 쪽지를 영구 삭제하시겠습니까?` :
                `선택한 ${selectedIds.length}개의 쪽지를 삭제하시겠습니까?`;

            if (confirm(confirmMsg)) {
                // 타입별로 메시지를 그룹화
                const messagesByType = {};
                selectedRows.forEach((row, index) => {
                    const id = selectedIds[index];
                    const type = selectedTypes[index];
                    // SENT → 'sent', RECEIVED → 'received'
                    const userType = type === 'SENT' ? 'sent' : 'received';

                    if (!messagesByType[userType]) {
                        messagesByType[userType] = [];
                    }
                    messagesByType[userType].push(id);
                });

                console.log('Messages by Type:', messagesByType);

                // message-com.js에서 제공하는 getCsrfHeaders() 사용
                const headers = MessageCommon.getCsrfHeaders();

                // 컨트롤러에 추가한 /bulk 경로 호출
                const endpoint = isTrashPage ? '/messages/api/delete/bulk' : '/messages/api/trash/bulk';

                // 각 타입별로 별도 API 호출
                let completedRequests = 0;
                let totalRequests = Object.keys(messagesByType).length;

                Object.entries(messagesByType).forEach(([userType, ids]) => {
                    const requestBody = {
                        ids: ids,
                        userType: userType
                    };

                    console.log(`=== ${userType} 쪽지 삭제 요청 ===`);
                    console.log('Endpoint:', endpoint);
                    console.log('Request Body:', JSON.stringify(requestBody, null, 2));
                    console.log('================================');

                    fetch(endpoint, {
                        method: 'POST',
                        headers: headers,
                        body: JSON.stringify(requestBody),
                        credentials: 'same-origin'
                    })
                        .then(res => {
                            console.log(`[${userType}] Response Status:`, res.status);
                            completedRequests++;

                            if (!res.ok) {
                                return res.text().then(text => {
                                    console.error(`[${userType}] Error Response:`, text);
                                    throw new Error(`${userType} 쪽지 삭제 실패 (상태: ${res.status})`);
                                });
                            }

                            // 모든 요청이 완료되면 새로고침
                            if (completedRequests === totalRequests) {
                                console.log('모든 삭제 요청 완료');
                                alert('삭제되었습니다.');
                                location.reload();
                            }
                        })
                        .catch(err => {
                            console.error(`[${userType}] Fetch Error:`, err);
                            alert('삭제 처리에 실패했습니다: ' + err.message);
                        });
                });
            }
        });
    }
});