// 전역 변수
let currentSection = 'users';
let currentPage = 0;

//  초기화
document.addEventListener('DOMContentLoaded', function () {
    loadAdminInfo();
    loadSection('users');
    setupMenuListeners();
    setupButtonListeners();
});

// CSRF 토큰 처리
function getCsrfToken() {
    const token = document.querySelector('meta[name="_csrf"]');
    const header = document.querySelector('meta[name="_csrf_header"]');

    if (token && header) {
        return {
            token: token.getAttribute('content'),
            header: header.getAttribute('content')
        };
    }
    return null;
}

function fetchWithCsrf(url, options) {
    options = options || {};
    options.headers = options.headers || {};

    const csrf = getCsrfToken();
    if (csrf) {
        options.headers[csrf.header] = csrf.token;
    }

    return fetch(url, options);
}

// 이벤트 리스너 설정
function setupMenuListeners() {
    const menuItems = document.querySelectorAll('#menu-list > div');
    menuItems.forEach(function (item) {
        item.addEventListener('click', function () {
            const section = this.dataset.section;
            switchSection(section);

            // 메뉴 활성화
            menuItems.forEach(function (m) {
                m.classList.remove('active');
            });
            this.classList.add('active');
        });
    });
}

function setupButtonListeners() {
    document.getElementById('main-page-btn').addEventListener('click', goToMain);
    document.getElementById('logout-btn').addEventListener('click', logout);
}

//네비게이션
function goToMain() {
    window.location.href = '/';
}

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        window.location.href = '/logout';
    }
}

function switchSection(section) {
    currentSection = section;
    currentPage = 0;

    // 모든 섹션 숨기기
    const sections = document.querySelectorAll('#main-content > section');
    sections.forEach(function (s) {
        s.classList.remove('active');
    });

    // 선택된 섹션 보이기
    const targetSection = document.getElementById(section + '-section');
    if (targetSection) {
        targetSection.classList.add('active');
    }

    // 데이터 로드
    loadSection(section);
}

// 데이터 로드
function loadSection(section) {
    const config = {
        users: {
            url: '/admin/users',
            render: renderUsersTable,
            errorColspan: 7
        },
        posts: {
            url: '/admin/posts',
            render: renderPostsTable,
            errorColspan: 6
        },
        comments: {
            url: '/admin/comments',
            render: renderCommentsTable,
            errorColspan: 5
        }
    };

    const sectionConfig = config[section];
    if (!sectionConfig) return;

    fetch(sectionConfig.url + '?page=' + currentPage + '&size=20')
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            sectionConfig.render(data.content);
            renderPagination(section, data);
        })
        .catch(function (error) {
            console.error('Error loading ' + section + ':', error);
            showError(section + '-tbody', sectionConfig.errorColspan, section + ' 목록을 불러올 수 없습니다.');
        });
}

//  공통 렌더링 함수
function createButton(text, className, onclick) {
    return `<button class="${className}" onclick="${onclick}">${text}</button>`;
}

function createBadge(text, type) {
    return `<span class="badge badge-${type}">${text}</span>`;
}

// 사용자 관리
function loadAdminInfo() {
    document.getElementById('admin-name').textContent = '관리자';
}

function renderUsersTable(users) {
    const tbody = document.getElementById('users-tbody');

    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7">사용자가 없습니다.</td></tr>';
        return;
    }

    let html = '';
    users.forEach(function (user) {
        const roleType = user.role === 'ADMIN' ? 'primary' : 'success';
        const statusType = user.active ? 'success' : 'danger';
        const statusText = user.active ? '활성' : '비활성';
        const roleButtonText = user.role === 'USER' ? '관리자로' : '사용자로';
        const actionButtonText = user.active ? '정지' : '활성화';
        const actionFunction = user.active ? 'banUser' : 'activateUser';

        html += `
            <tr>
                <td>${user.username}</td>
                <td>${user.name}</td>
                <td>${user.email}</td>
                <td>${createBadge(user.role, roleType)}</td>
                <td>${createBadge(statusText, statusType)}</td>
                <td>${formatDate(user.createdAt)}</td>
                <td>
                    ${createButton(roleButtonText, 'btn-primary', `changeUserRole(${user.id}, '${user.role}')`)}
                    ${createButton(actionButtonText, user.active ? 'btn-danger' : 'btn-success', `${actionFunction}(${user.id})`)}
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

function changeUserRole(userId, currentRole) {
    const newRole = currentRole === 'USER' ? 'ADMIN' : 'USER';
    const reason = prompt('권한 변경 사유를 입력하세요:');

    if (!reason) return;

    fetchWithCsrf('/admin/users/' + userId + '/role?role=' + newRole + '&reason=' + encodeURIComponent(reason), {
        method: 'PATCH'
    })
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (data.success) {
                alert(data.message);
                loadSection('users');
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

    fetchWithCsrf(url, { method: 'POST' })
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (data.success) {
                alert(data.message);
                loadSection('users');
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

    fetchWithCsrf('/admin/users/' + userId + '/activate', { method: 'POST' })
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (data.success) {
                alert(data.message);
                loadSection('users');
            } else {
                alert(data.message);
            }
        })
        .catch(function (error) {
            console.error('Error:', error);
            alert('사용자 활성화에 실패했습니다.');
        });
}

// 게시물 관리
function renderPostsTable(posts) {
    const tbody = document.getElementById('posts-tbody');

    if (!posts || posts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">게시물이 없습니다.</td></tr>';
        return;
    }

    let html = '';
    posts.forEach(function (post) {
        html += `
            <tr>
                <td>${post.title}</td>
                <td>${post.writer || 'Unknown'}</td>
                <td>${post.boardId}</td>
                <td>${post.viewCount}</td>
                <td>${formatDate(post.createdAt)}</td>
                <td>${createButton('삭제', 'btn-danger', `deletePost(${post.id})`)}</td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

function deletePost(postId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    fetchWithCsrf('/admin/posts/' + postId, { method: 'DELETE' })
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (data.success) {
                alert(data.message);
                loadSection('posts');
            } else {
                alert(data.message);
            }
        })
        .catch(function (error) {
            console.error('Error:', error);
            alert('게시물 삭제에 실패했습니다.');
        });
}

// 댓글 관리
function renderCommentsTable(comments) {
    const tbody = document.getElementById('comments-tbody');

    if (!comments || comments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5">댓글이 없습니다.</td></tr>';
        return;
    }

    let html = '';
    comments.forEach(function (comment) {
        html += `
            <tr>
                <td>${escapeHtml(comment.content)}</td>
                <td>${comment.writer || 'Unknown'}</td>
                <td>${comment.postId}</td>
                <td>${formatDate(comment.createdAt)}</td>
                <td>${createButton('삭제', 'btn-danger', `deleteComment(${comment.id})`)}</td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

function deleteComment(commentId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    fetchWithCsrf('/admin/comments/' + commentId, { method: 'DELETE' })
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (data.success) {
                alert(data.message);
                loadSection('comments');
            } else {
                alert(data.message);
            }
        })
        .catch(function (error) {
            console.error('Error:', error);
            alert('댓글 삭제에 실패했습니다.');
        });
}

// 유틸리티 함수
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

function showError(tbodyId, colspan, message) {
    const tbody = document.getElementById(tbodyId);
    tbody.innerHTML = '<tr><td colspan="' + colspan + '">' + message + '</td></tr>';
}

// 페이지네이션
function renderPagination(section, data) {
    const container = document.getElementById(section + '-pagination');
    if (!container) return;

    const totalPages = data.totalPages || 1;
    const currentPageNum = data.number || 0;

    let html = '';

    if (currentPageNum > 0) {
        html += `<button onclick="goToPage('${section}', ${currentPageNum - 1})">‹</button>`;
    }

    for (let i = 0; i < totalPages; i++) {
        const activeClass = i === currentPageNum ? ' class="active"' : '';
        html += `<button${activeClass} onclick="goToPage('${section}', ${i})">${i + 1}</button>`;
    }

    if (currentPageNum < totalPages - 1) {
        html += `<button onclick="goToPage('${section}', ${currentPageNum + 1})">›</button>`;
    }

    container.innerHTML = html;
}

function goToPage(section, page) {
    currentPage = page;
    loadSection(section);
}