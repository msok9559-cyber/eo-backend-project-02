
// Admin Dashboard JavaScript

    let currentSection = 'users';
    let currentPage = 0;
    let currentBoardId = null;

// 초기화
    document.addEventListener('DOMContentLoaded', function () {
        loadAdminInfo();
        loadUsers();
        setupMenuListeners();
        setupButtonListeners();
    });

// 메뉴 클릭 이벤트
    function setupMenuListeners() {
        const menuItems = document.querySelectorAll('#menu-list > div');
        menuItems.forEach(function (item) {
            item.addEventListener('click', function () {
                // 메뉴 활성화
                menuItems.forEach(function (m) {
                    m.classList.remove('active');
                });
                this.classList.add('active');

                // 섹션 전환
                const section = this.dataset.section;
                switchSection(section);
            });
        });
    }

// 버튼 클릭 이벤트
    function setupButtonListeners() {
        document.getElementById('board-add-btn').addEventListener('click', openBoardModal);
        document.getElementById('board-cancel-btn').addEventListener('click', closeBoardModal);
        document.getElementById('board-save-btn').addEventListener('click', saveBoard);
        document.getElementById('logout-btn').addEventListener('click', logout);
    }

// 섹션 전환
    function switchSection(section) {
        currentSection = section;
        currentPage = 0;

        // 모든 섹션 숨기기
        const sections = document.querySelectorAll('#main-content > section');
        sections.forEach(function (s) {
            s.classList.remove('active');
        });

        // 선택된 섹션 보이기
        document.getElementById(section + '-section').classList.add('active');

        // 데이터 로드
        switch (section) {
            case 'users':
                loadUsers();
                break;
            case 'boards':
                loadBoards();
                break;
            case 'posts':
                loadPosts();
                break;
            case 'comments':
                loadComments();
                break;
        }
    }

// 관리자 정보

    function loadAdminInfo() {
        document.getElementById('admin-name').textContent = '관리자';
    }

    function logout() {
        if (confirm('로그아웃 하시겠습니까?')) {
            window.location.href = '/logout';
        }
    }

// 사용자 관리

    function loadUsers() {
        fetch('/admin/users?page=' + currentPage + '&size=20')
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                renderUsersTable(data.content);
                renderPagination('users', data);
            })
            .catch(function (error) {
                console.error('Error loading users:', error);
                showError('users-tbody', 7, '사용자 목록을 불러올 수 없습니다.');
            });
    }

    function renderUsersTable(users) {
        const tbody = document.getElementById('users-tbody');

        if (!users || users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7">사용자가 없습니다.</td></tr>';
            return;
        }

        let html = '';
        users.forEach(function (user) {
            const roleClass = user.role === 'ADMIN' ? 'badge-primary' : 'badge-success';
            const statusClass = user.active ? 'badge-success' : 'badge-danger';
            const statusText = user.active ? '활성' : '비활성';
            const roleButtonText = user.role === 'USER' ? '👑 관리자로' : '👤 사용자로';
            const actionButtonText = user.active ? '🚫 정지' : '✅ 활성화';
            const actionFunction = user.active ? 'banUser' : 'activateUser';

            html += '<tr>';
            html += '<td>' + user.username + '</td>';
            html += '<td>' + user.name + '</td>';
            html += '<td>' + user.email + '</td>';
            html += '<td><span style="display:inline-block;padding:4px 8px;border-radius:4px;font-size:12px;font-weight:500;background:#cce5ff;color:#004085;">' + user.role + '</span></td>';
            html += '<td><span style="display:inline-block;padding:4px 8px;border-radius:4px;font-size:12px;font-weight:500;background:' + (user.active ? '#d4edda' : '#f8d7da') + ';color:' + (user.active ? '#155724' : '#721c24') + ';">' + statusText + '</span></td>';
            html += '<td>' + formatDate(user.createdAt) + '</td>';
            html += '<td>';
            html += '<button style="padding:5px 10px;font-size:12px;margin-right:5px;background:#5B87DE;color:white;border:none;border-radius:4px;cursor:pointer;" onclick="changeUserRole(' + user.id + ', \'' + user.role + '\')">' + roleButtonText + '</button>';
            html += '<button style="padding:5px 10px;font-size:12px;background:' + (user.active ? '#ff4444' : '#00C851') + ';color:white;border:none;border-radius:4px;cursor:pointer;" onclick="' + actionFunction + '(' + user.id + ')">' + actionButtonText + '</button>';
            html += '</td>';
            html += '</tr>';
        });

        tbody.innerHTML = html;
    }

    function changeUserRole(userId, currentRole) {
        const newRole = currentRole === 'USER' ? 'ADMIN' : 'USER';
        const reason = prompt('권한 변경 사유를 입력하세요:');

        if (!reason) return;

        fetch('/admin/users/' + userId + '/role?role=' + newRole + '&reason=' + encodeURIComponent(reason), {
            method: 'PATCH'
        })
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data.success) {
                    alert(data.message);
                    loadUsers();
                } else {
                    alert(data.message);
                }
            })
            .catch(function (error) {
                console.error('Error:', error);
                alert('권한 변경에 실패했습니다.');
            });
    }

    function banUser(userId) {
        const reason = prompt('정지 사유를 입력하세요:');
        if (!reason) return;

        const duration = prompt('정지 기간(일)을 입력하세요 (무기한은 비워두세요):');

        let url = '/admin/users/' + userId + '/ban?reason=' + encodeURIComponent(reason);
        if (duration) url += '&duration=' + duration;

        fetch(url, {method: 'POST'})
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data.success) {
                    alert(data.message);
                    loadUsers();
                } else {
                    alert(data.message);
                }
            })
            .catch(function (error) {
                console.error('Error:', error);
                alert('사용자 정지에 실패했습니다.');
            });
    }

    function activateUser(userId) {
        if (!confirm('사용자를 활성화하시겠습니까?')) return;

        fetch('/admin/users/' + userId + '/activate', {method: 'POST'})
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data.success) {
                    alert(data.message);
                    loadUsers();
                } else {
                    alert(data.message);
                }
            })
            .catch(function (error) {
                console.error('Error:', error);
                alert('사용자 활성화에 실패했습니다.');
            });
    }

// 게시판 관리

    function loadBoards() {
        fetch('/admin/boards')
            .then(function (response) {
                return response.json();
            })
            .then(function (boards) {
                renderBoardsTable(boards);
            })
            .catch(function (error) {
                console.error('Error loading boards:', error);
                showError('boards-tbody', 4, '게시판 목록을 불러올 수 없습니다.');
            });
    }

    function renderBoardsTable(boards) {
        const tbody = document.getElementById('boards-tbody');

        if (!boards || boards.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">게시판이 없습니다.</td></tr>';
            return;
        }

        let html = '';
        boards.forEach(function (board) {
            html += '<tr>';
            html += '<td>' + board.id + '</td>';
            html += '<td>' + board.title + '</td>';
            html += '<td>' + formatDate(board.createdAt) + '</td>';
            html += '<td>';
            html += '<button style="padding:5px 10px;font-size:12px;margin-right:5px;background:#5B87DE;color:white;border:none;border-radius:4px;cursor:pointer;" onclick="editBoard(' + board.id + ', \'' + escapeHtml(board.title) + '\')">✏️ 수정</button>';
            html += '<button style="padding:5px 10px;font-size:12px;background:#ff4444;color:white;border:none;border-radius:4px;cursor:pointer;" onclick="deleteBoard(' + board.id + ')">🗑️ 삭제</button>';
            html += '</td>';
            html += '</tr>';
        });

        tbody.innerHTML = html;
    }

    function openBoardModal() {
        currentBoardId = null;
        document.getElementById('board-modal-title').textContent = '게시판 추가';
        document.getElementById('board-title-input').value = '';
        document.getElementById('board-modal').classList.add('active');
    }

    function closeBoardModal() {
        document.getElementById('board-modal').classList.remove('active');
    }

    function editBoard(boardId, title) {
        currentBoardId = boardId;
        document.getElementById('board-modal-title').textContent = '게시판 수정';
        document.getElementById('board-title-input').value = title;
        document.getElementById('board-modal').classList.add('active');
    }

    function saveBoard() {
        const title = document.getElementById('board-title-input').value.trim();

        if (!title) {
            alert('게시판 이름을 입력하세요.');
            return;
        }

        if (currentBoardId) {
            // 수정
            fetch('/admin/boards/' + currentBoardId + '?title=' + encodeURIComponent(title), {
                method: 'PUT'
            })
                .then(function (response) {
                    return response.json();
                })
                .then(function (data) {
                    if (data.success) {
                        alert(data.message);
                        closeBoardModal();
                        loadBoards();
                    } else {
                        alert(data.message);
                    }
                })
                .catch(function (error) {
                    console.error('Error:', error);
                    alert('게시판 수정에 실패했습니다.');
                });
        } else {
            // 생성
            fetch('/admin/boards?title=' + encodeURIComponent(title), {
                method: 'POST'
            })
                .then(function (response) {
                    return response.json();
                })
                .then(function (data) {
                    if (data.success) {
                        alert(data.message);
                        closeBoardModal();
                        loadBoards();
                    } else {
                        alert(data.message);
                    }
                })
                .catch(function (error) {
                    console.error('Error:', error);
                    alert('게시판 생성에 실패했습니다.');
                });
        }
    }

    function deleteBoard(boardId) {
        if (!confirm('정말 삭제하시겠습니까?\n게시판의 모든 게시글도 삭제됩니다.')) return;

        fetch('/admin/boards/' + boardId, {method: 'DELETE'})
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data.success) {
                    alert(data.message);
                    loadBoards();
                } else {
                    alert(data.message);
                }
            })
            .catch(function (error) {
                console.error('Error:', error);
                alert('게시판 삭제에 실패했습니다.');
            });
    }

// 게시물 관리

    function loadPosts() {
        fetch('/admin/posts?page=' + currentPage + '&size=20')
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                renderPostsTable(data.content);
                renderPagination('posts', data);
            })
            .catch(function (error) {
                console.error('Error loading posts:', error);
                showError('posts-tbody', 6, '게시물 목록을 불러올 수 없습니다.');
            });
    }

    function renderPostsTable(posts) {
        const tbody = document.getElementById('posts-tbody');

        if (!posts || posts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">게시물이 없습니다.</td></tr>';
            return;
        }

        let html = '';
        posts.forEach(function (post) {
            html += '<tr>';
            html += '<td>' + post.title + '</td>';
            html += '<td>' + (post.writer || 'Unknown') + '</td>';
            html += '<td>' + post.boardId + '</td>';
            html += '<td>' + post.viewCount + '</td>';
            html += '<td>' + formatDate(post.createdAt) + '</td>';
            html += '<td>';
            html += '<button style="padding:5px 10px;font-size:12px;background:#ff4444;color:white;border:none;border-radius:4px;cursor:pointer;" onclick="deletePost(' + post.id + ')">🗑️ 삭제</button>';
            html += '</td>';
            html += '</tr>';
        });

        tbody.innerHTML = html;
    }

    function deletePost(postId) {
        if (!confirm('정말 삭제하시겠습니까?')) return;

        fetch('/admin/posts/' + postId, {method: 'DELETE'})
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data.success) {
                    alert(data.message);
                    loadPosts();
                } else {
                    alert(data.message);
                }
            })
            .catch(function (error) {
                console.error('Error:', error);
                alert('게시물 삭제에 실패했습니다.');
            });
    }


function loadComments() {
    fetch('/admin/comments?page=' + currentPage + '&size=20')
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            renderCommentsTable(data.content);
            renderPagination('comments', data);
        })
        .catch(function (error) {
            console.error('Error loading comments:', error);
            showError('comments-tbody', 5, '댓글 목록을 불러올 수 없습니다.');
        });
}

function renderCommentsTable(comments) {
    const tbody = document.getElementById('comments-tbody');

    if (!comments || comments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5">댓글이 없습니다.</td></tr>';
        return;
    }

    let html = '';
    comments.forEach(function (comment) {
        html += '<tr>';
        html += '<td>' + escapeHtml(comment.content) + '</td>';
        html += '<td>' + (comment.writer || 'Unknown') + '</td>';
        html += '<td>' + comment.postId + '</td>';
        html += '<td>' + formatDate(comment.createdAt) + '</td>';
        html += '<td>';
        html += '<button style="padding:5px 10px;font-size:12px;background:#ff4444;color:white;border:none;border-radius:4px;cursor:pointer;" onclick="deleteComment(' + comment.id + ')">🗑️ 삭제</button>';
        html += '</td>';
        html += '</tr>';
    });

    tbody.innerHTML = html;
}

function deleteComment(commentId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    fetch('/admin/comments/' + commentId, { method: 'DELETE' })
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (data.success) {
                alert(data.message);
                loadComments();
            } else {
                alert(data.message);
            }
        })
        .catch(function (error) {
            console.error('Error:', error);
            alert('댓글 삭제에 실패했습니다.');
        });
}


// 유틸리티

    function formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR');
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function renderPagination(section, data) {
        const container = document.getElementById(section + '-pagination');
        if (!container) return;

        const totalPages = data.totalPages || 1;
        const currentPageNum = data.number || 0;

        let html = '';

        if (currentPageNum > 0) {
            html += '<button onclick="goToPage(\'' + section + '\', ' + (currentPageNum - 1) + ')">‹</button>';
        }

        for (let i = 0; i < totalPages; i++) {
            if (i === currentPageNum) {
                html += '<button class="active">' + (i + 1) + '</button>';
            } else {
                html += '<button onclick="goToPage(\'' + section + '\', ' + i + ')">' + (i + 1) + '</button>';
            }
        }

        if (currentPageNum < totalPages - 1) {
            html += '<button onclick="goToPage(\'' + section + '\', ' + (currentPageNum + 1) + ')">›</button>';
        }

        container.innerHTML = html;
    }

    function goToPage(section, page) {
        currentPage = page;

        switch (section) {
            case 'users':
                loadUsers();
                break;
            case 'posts':
                loadPosts();
                break;
            case 'comments':
                loadComments();
                break;
        }
    }

    function showError(tbodyId, colspan, message) {
        const tbody = document.getElementById(tbodyId);
        tbody.innerHTML = '<tr><td colspan="' + colspan + '">' + message + '</td></tr>';
    }
