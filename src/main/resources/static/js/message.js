/**
 * [2026-02-12] 쪽지 목록 기능 스크립트
 * - 목록 선택, 상세보기 이동, 대량 삭제(일반/영구) 처리
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
            const selectedIds = Array.from(checkedBoxes)
                .map(cb => cb.closest('.messageRow').getAttribute('data-id'));

            if (selectedIds.length === 0) {
                alert('삭제할 쪽지를 선택해주세요.');
                return;
            }

            // 현재 위치가 휴지통인지 확인
            const isTrashPage = window.location.pathname.includes('/trash');
            const confirmMsg = isTrashPage ?
                `선택한 ${selectedIds.length}개의 쪽지를 영구 삭제하시겠습니까?` :
                `선택한 ${selectedIds.length}개의 쪽지를 삭제하시겠습니까?`;

            if (confirm(confirmMsg)) {
                // 컨트롤러에 추가한 /bulk 경로 호출
                const endpoint = isTrashPage ? '/messages/api/delete/bulk' : '/messages/api/trash/bulk';

                fetch(endpoint, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ ids: selectedIds })
                })
                    .then(res => {
                        if (res.ok) {
                            alert('삭제되었습니다.');
                            location.reload();
                        } else {
                            alert('삭제 처리에 실패했습니다. (API 경로 및 컨트롤러를 확인하세요)');
                        }
                    })
                    .catch(err => {
                        console.error('Bulk Delete error:', err);
                        alert('오류가 발생했습니다.');
                    });
            }
        });
    }
});