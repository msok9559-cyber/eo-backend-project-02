// 간단 프론트 검증(선택)
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('writeForm');
    if (!form) return;

    const title = document.getElementById('title');
    const content = document.getElementById('content');
    const err = document.getElementById('formError');

    form.addEventListener('submit', (e) => {
        err.style.display = 'none';
        err.textContent = '';

        const t = title.value.trim();
        const c = content.value.trim();

        if (t.length < 1 || t.length > 100) {
            e.preventDefault();
            err.style.display = 'block';
            err.textContent = '제목은 1~100자여야 합니다.';
            return;
        }
        if (c.length < 1 || c.length > 5000) {
            e.preventDefault();
            err.style.display = 'block';
            err.textContent = '내용은 1~5000자여야 합니다.';
            return;
        }
    });
});