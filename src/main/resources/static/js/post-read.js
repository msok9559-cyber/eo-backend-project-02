// post-read.js - 댓글 기능 (완전한 버전)
// 댓글 작성/수정/삭제 및 실시간 댓글 수 업데이트

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {

    // URL에서 postId 추출
    var urlParams = new URLSearchParams(window.location.search);
    var postId = urlParams.get('id');

    if (!postId) {
        console.error('postId를 찾을 수 없습니다.');
        return;
    }

    var COMMENT_API = '/api/posts/' + postId + '/comments';

    // 사용자 정보 가져오기 (data 속성에서)
    var postBox = document.querySelector('.post-box');
    var CURRENT_USER_ID = postBox ? parseInt(postBox.getAttribute('data-user-id')) : null;
    var IS_ADMIN = postBox ? postBox.getAttribute('data-is-admin') === 'true' : false;

    console.log('Current User ID:', CURRENT_USER_ID);
    console.log('Is Admin:', IS_ADMIN);

    // DOM 요소
    var commentList = document.getElementById('commentList');
    var commentCount = document.getElementById('commentCount');
    var topCommentCount = document.getElementById('topCommentCount');
    var newComment = document.getElementById('newComment');
    var btnCreateComment = document.getElementById('btnCreateComment');
    var commentError = document.getElementById('commentError');

    // CSRF 토큰 가져오기
    function getCsrfToken() {
        var tokenMeta = document.querySelector('meta[name="_csrf"]');
        var headerMeta = document.querySelector('meta[name="_csrf_header"]');

        console.log('CSRF Meta:', tokenMeta, headerMeta);

        if (tokenMeta && headerMeta) {
            var token = tokenMeta.getAttribute('content');
            var header = headerMeta.getAttribute('content');
            console.log('CSRF Token:', token);
            console.log('CSRF Header:', header);
            return {
                token: token,
                header: header
            };
        }
        console.warn('CSRF 토큰을 찾을 수 없습니다!');
        return null;
    }

    // Fetch 헤더에 CSRF 추가 (CSRF 없으면 생략)
    function getFetchHeaders() {
        var headers = {
            'Content-Type': 'application/json'
        };

        var csrf = getCsrfToken();
        if (csrf && csrf.token && csrf.header) {
            headers[csrf.header] = csrf.token;
            console.log('CSRF 헤더 추가:', csrf.header, '=', csrf.token);
        } else {
            console.log('CSRF 토큰 없음 (비활성화 상태)');
        }

        return headers;
    }

    // 날짜 포맷팅
    function formatDate(dateString) {
        if (!dateString) return '-';
        var date = new Date(dateString);
        var year = date.getFullYear();
        var month = String(date.getMonth() + 1).padStart(2, '0');
        var day = String(date.getDate()).padStart(2, '0');
        var hours = String(date.getHours()).padStart(2, '0');
        var minutes = String(date.getMinutes()).padStart(2, '0');
        return year + '.' + month + '.' + day + ' ' + hours + ':' + minutes;
    }

    // HTML 이스케이프
    function escapeHtml(text) {
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 댓글 목록 렌더링
    function renderComments(comments) {
        // 하단 댓글수 업데이트
        commentCount.textContent = comments.length;

        // 상단 댓글수 업데이트
        if (topCommentCount) {
            topCommentCount.textContent = comments.length;
        }

        if (!comments || comments.length === 0) {
            commentList.innerHTML = '<div class="empty-comment">아직 댓글이 없습니다.</div>';
            return;
        }

        var html = '';
        comments.forEach(function(comment) {
            var isOwner = (CURRENT_USER_ID && comment.userId === CURRENT_USER_ID);

            html += '<div class="comment-item" data-id="' + comment.id + '">';
            html += '  <div class="comment-body">';
            html += '    <div class="comment-top">';
            html += '      <div class="comment-writer">' + escapeHtml(comment.writer || 'unknown') + '</div>';
            html += '      <div class="comment-time">' + formatDate(comment.createdAt) + '</div>';
            html += '    </div>';
            html += '    <div class="comment-content" data-content>' + escapeHtml(comment.content || '') + '</div>';

            // 수정 영역 (숨김)
            html += '    <div class="comment-editbox" style="display:none;">';
            html += '      <textarea maxlength="200"></textarea>';
            html += '      <div class="row">';
            html += '        <button type="button" onclick="cancelEdit(this)">취소</button>';
            html += '        <button type="button" onclick="saveEdit(this, ' + comment.id + ')">저장</button>';
            html += '      </div>';
            html += '    </div>';

            // 버튼 영역 (조건부 표시)
            if (isOwner || IS_ADMIN) {
                html += '    <div class="comment-footer">';
                html += '      <div class="comment-actions">';

                // 작성자만 수정 가능
                if (isOwner) {
                    html += '        <button type="button" onclick="editComment(this)">수정</button>';
                }

                // 작성자 또는 관리자는 삭제 가능
                if (isOwner || IS_ADMIN) {
                    html += '        <button type="button" class="del" onclick="deleteComment(' + comment.id + ')">삭제</button>';
                }

                html += '      </div>';
                html += '    </div>';
            }

            html += '  </div>';
            html += '</div>';
        });

        commentList.innerHTML = html;
    }

    // 댓글 목록 로드
    function loadComments() {
        fetch(COMMENT_API, {
            method: 'GET',
            credentials: 'same-origin'
        })
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('댓글 목록을 불러올 수 없습니다.');
                }
                return response.json();
            })
            .then(function(data) {
                renderComments(data);
            })
            .catch(function(error) {
                console.error('Error loading comments:', error);
                commentList.innerHTML = '<div class="empty-comment">댓글을 불러오는데 실패했습니다.</div>';
            });
    }

    // 댓글 등록 (성공 후 loadComments() 호출)
    function createComment() {
        var content = newComment.value.trim();

        console.log('=== 댓글 작성 시작 ===');
        console.log('postId:', postId);
        console.log('content:', content);
        console.log('API URL:', COMMENT_API);

        // 에러 메시지 초기화
        commentError.style.display = 'none';
        commentError.textContent = '';

        // 유효성 검사
        if (content.length < 1 || content.length > 200) {
            commentError.style.display = 'block';
            commentError.textContent = '댓글은 1~200자여야 합니다.';
            return;
        }

        var requestBody = { content: content };
        console.log('Request Body:', JSON.stringify(requestBody));
        console.log('Headers:', getFetchHeaders());

        fetch(COMMENT_API, {
            method: 'POST',
            credentials: 'same-origin',
            headers: getFetchHeaders(),
            body: JSON.stringify(requestBody)
        })
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('댓글 작성에 실패했습니다.');
                }
                return response.json();
            })
            .then(function(data) {
                newComment.value = '';
                // 댓글 목록 다시 로드 → 댓글 수 자동 업데이트
                loadComments();
            })
            .catch(function(error) {
                console.error('Error creating comment:', error);
                commentError.style.display = 'block';
                commentError.textContent = error.message || '댓글 작성에 실패했습니다.';
            });
    }

    // 댓글 수정 모드 열기
    window.editComment = function(button) {
        var commentItem = button.closest('.comment-item');
        var contentDiv = commentItem.querySelector('[data-content]');
        var editBox = commentItem.querySelector('.comment-editbox');
        var textarea = editBox.querySelector('textarea');

        textarea.value = contentDiv.textContent.trim();
        contentDiv.style.display = 'none';
        editBox.style.display = 'block';
    };

    // 댓글 수정 취소
    window.cancelEdit = function(button) {
        var commentItem = button.closest('.comment-item');
        var contentDiv = commentItem.querySelector('[data-content]');
        var editBox = commentItem.querySelector('.comment-editbox');

        editBox.style.display = 'none';
        contentDiv.style.display = 'block';
    };

    // 댓글 수정 저장 (성공 후 loadComments() 호출)
    window.saveEdit = function(button, commentId) {
        var commentItem = button.closest('.comment-item');
        var textarea = commentItem.querySelector('.comment-editbox textarea');
        var content = textarea.value.trim();

        if (content.length < 1 || content.length > 200) {
            alert('댓글은 1~200자여야 합니다.');
            return;
        }

        fetch(COMMENT_API + '/' + commentId, {
            method: 'PUT',
            credentials: 'same-origin',
            headers: getFetchHeaders(),
            body: JSON.stringify({
                id: commentId,
                postId: postId,
                content: content
            })
        })
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('댓글 수정에 실패했습니다.');
                }
                return response.json();
            })
            .then(function(data) {
                // 댓글 목록 다시 로드 → 댓글 수 자동 업데이트
                loadComments();
            })
            .catch(function(error) {
                console.error('Error updating comment:', error);
                alert(error.message || '댓글 수정에 실패했습니다.');
            });
    };

    // 댓글 삭제 (성공 후 loadComments() 호출)
    window.deleteComment = function(commentId) {
        if (!confirm('댓글을 삭제할까요?')) return;

        fetch(COMMENT_API + '/' + commentId, {
            method: 'DELETE',
            credentials: 'same-origin',
            headers: getFetchHeaders()
        })
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('댓글 삭제에 실패했습니다.');
                }
                return response.json();
            })
            .then(function(data) {
                if (data.success) {
                    // 댓글 목록 다시 로드 → 댓글 수 자동 업데이트
                    loadComments();
                } else {
                    alert(data.message || '댓글 삭제에 실패했습니다.');
                }
            })
            .catch(function(error) {
                console.error('Error deleting comment:', error);
                alert(error.message || '댓글 삭제에 실패했습니다.');
            });
    };

    // 이벤트 리스너 등록
    if (btnCreateComment) {
        btnCreateComment.addEventListener('click', createComment);
    }

    // 초기 댓글 로드
    loadComments();
});