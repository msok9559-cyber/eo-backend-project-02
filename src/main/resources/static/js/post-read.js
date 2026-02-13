const COMMENT_API = `/api/posts/${postId}/comments`;

const $list = document.getElementById('commentList');
const $count = document.getElementById('commentCount');
const $empty = document.getElementById('emptyCommentMsg');

const $new = document.getElementById('newComment');
const $btnCreate = document.getElementById('btnCreateComment');
const $err = document.getElementById('commentError');

function getCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (!token || !header) return null;
    return { token, header };
}

function withCsrf(headers = {}) {
    const csrf = getCsrf();
    if (!csrf) return headers;
    return { ...headers, [csrf.header]: csrf.token };
}

async function fetchJson(url, options = {}) {
    const res = await fetch(url, {
        credentials: 'same-origin',
        ...options,
        headers: withCsrf({
            'Content-Type': 'application/json',
            ...(options.headers || {})
        })
    });

    if (res.status === 401) throw new Error('로그인이 필요합니다.');
    if (res.status === 403) throw new Error('권한이 없습니다.');
    if (res.status === 404) throw new Error('요청한 리소스를 찾을 수 없습니다.');
    if (!res.ok) {
        const t = await res.text();
        throw new Error(t || `HTTP ${res.status}`);
    }
    if (res.status === 204) return null;
    return res.json();
}

function formatDateTime(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    const yy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mi = String(d.getMinutes()).padStart(2, '0');
    return `${yy}.${mm}.${dd} ${hh}:${mi}`;
}

function escapeHtml(str) {
    return String(str)
        .replaceAll('&','&amp;')
        .replaceAll('<','&lt;')
        .replaceAll('>','&gt;')
        .replaceAll('"','&quot;')
        .replaceAll("'","&#039;");
}


function render(comments) {
    $count.textContent = String(comments.length);
    $empty.style.display = comments.length ? 'none' : 'block';

    $list.innerHTML = comments.map(c => {
        const isOwner = (currentUserId != null && Number(c.userId) === Number(currentUserId));
        const showDelete = isOwner || isAdmin;
        const showEdit = isOwner && !isAdmin;

        return `
                <div class="comment-item" id="comment-${c.id}" data-id="${c.id}">
                    <div class="comment-body">
                        <div class="comment-top">
                            <div class="comment-writer">${escapeHtml(c.writer ?? 'unknown')}</div>
                            <div class="comment-time">${formatDateTime(c.createdAt)}</div>
                        </div>

                        <div class="comment-content" data-content>${escapeHtml(c.content ?? '')}</div>

                        <div class="comment-editbox" data-editbox>
                            <textarea maxlength="200"></textarea>
                            <div class="row">
                                <button type="button" data-action="cancel">취소</button>
                                <button type="button" data-action="save">저장</button>
                            </div>
                        </div>

                        <div class="comment-footer" style="${(showEdit || showDelete) ? '' : 'display:none'}">
                            <div class="comment-actions">
                                ${showEdit ? `<button type="button" data-action="edit">수정</button>` : ``}
                                ${showDelete ? `<button type="button" class="del" data-action="delete">삭제</button>` : ``}
                            </div>
                        </div>
                    </div>
                </div>
            `;
    }).join('');
}

async function loadComments() {
    const data = await fetchJson(COMMENT_API, { method: 'GET' });
    render(Array.isArray(data) ? data : []);
}

async function createComment() {
    if (!$new) return;

    const content = $new.value.trim();
    $err.style.display = 'none';
    $err.textContent = '';

    if (content.length < 1 || content.length > 200) {
        $err.style.display = 'block';
        $err.textContent = '댓글은 1~200자여야 합니다.';
        return;
    }

    await fetchJson(COMMENT_API, {
        method: 'POST',
        body: JSON.stringify({ content })
    });

    $new.value = '';
    await loadComments();
}

function openEditBox($item) {
    const $editBox = $item.querySelector('[data-editbox]');
    const $content = $item.querySelector('[data-content]');
    const $ta = $editBox.querySelector('textarea');

    $ta.value = $content.textContent.trim();
    $editBox.style.display = 'block';
    $content.style.display = 'none';
}

function closeEditBox($item) {
    const $editBox = $item.querySelector('[data-editbox]');
    const $content = $item.querySelector('[data-content]');
    $editBox.style.display = 'none';
    $content.style.display = 'block';
}

async function saveEdit($item) {
    if (isAdmin) {
        alert('관리자는 댓글을 수정할 수 없습니다.');
        return;
    }

    const id = $item.dataset.id;
    const $editBox = $item.querySelector('[data-editbox]');
    const $ta = $editBox.querySelector('textarea');
    const content = $ta.value.trim();

    if (content.length < 1 || content.length > 200) {
        alert('댓글은 1~200자여야 합니다.');
        return;
    }

    await fetchJson(`${COMMENT_API}/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ id: Number(id), postId: postId, content })
    });

    await loadComments();
}

async function deleteComment(id) {
    if (!confirm('댓글을 삭제할까요?')) return;
    await fetchJson(`${COMMENT_API}/${id}`, { method: 'DELETE' });
    await loadComments();
}

window.addEventListener('load', async () => {
    await loadComments();

    if ($btnCreate) $btnCreate.addEventListener('click', async () => {
        try { await createComment(); }
        catch (e) {
            $err.style.display = 'block';
            $err.textContent = e.message;
        }
    });

    $list.addEventListener('click', async (e) => {
        const btn = e.target.closest('button');
        if (!btn) return;

        const $item = e.target.closest('.comment-item');
        if (!$item) return;

        const action = btn.dataset.action;

        try {
            if (action === 'edit') openEditBox($item);
            if (action === 'cancel') closeEditBox($item);
            if (action === 'save') await saveEdit($item);
            if (action === 'delete') await deleteComment($item.dataset.id);
        } catch (err) {
            alert(err.message || '요청 실패');
        }
    });
});