document.addEventListener("DOMContentLoaded", function () {
    //변수 선언
    const nicknameMessage = document.querySelector(".nickname-message");
    const pwMessage = document.querySelector(".password-message");

    //탭
    const tabButtons = document.querySelectorAll(".tab-btn");
    const panels = document.querySelectorAll(".tab-panel");

    const hashId = location.hash ? location.hash.replace("#", "") : "";
    const saved = localStorage.getItem("mypage:lastTab");

    // 닉네임 수정
    const nicknameDisplay = document.getElementById("nicknameDisplay");
    const nicknameEditBox = document.getElementById("nicknameEditBox");
    const nicknameText = document.getElementById("nicknameText");
    const editNicknameBtn = document.getElementById("editNicknameBtn");
    const cancelNicknameBtn = document.getElementById("cancelNicknameBtn");
    const checkNicknameBtn = document.getElementById("checkNicknameBtn");
    const saveNicknameBtn = document.getElementById("saveNicknameBtn");
    const nicknameInput = document.getElementById("nicknameInput");

    // 비밀번호 변경
    const step1 = document.getElementById("password-check");
    const step2 = document.getElementById("password-step2");
    const verifyBtn = document.getElementById("verifyPasswordBtn");
    const pwForm = document.getElementById("form");

    // 현재 비밀번호 서버 검증
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    // 현재 비밀번호 input
    const currentPwInput = step1?.querySelector('input[name="currentPassword"]');

    // 탈퇴 모달
    const modal = document.getElementById("withdrawModal");
    const openBtn = document.getElementById("openWithdrawModal");
    const closeBtn = document.getElementById("closeWithdrawModal");


    const clearAllMessages = function () {
        if (nicknameMessage) {
            nicknameMessage.textContent = "";
            nicknameMessage.classList.remove("success", "error");
        }

        if (pwMessage) {
            pwMessage.textContent = "";
            pwMessage.classList.remove("success", "error");
        }

        document.querySelectorAll(".alert").forEach(function (el) {
            el.remove();
        });
    };



    // 탭 전환
    const activateTab = function (targetId) {
        tabButtons.forEach(function (b) {
            b.classList.toggle("is-active", b.dataset.target === targetId);
        });
        panels.forEach(function (p) {
            p.classList.toggle("is-active", p.id === targetId);
        });
    };

    const setPwMsg = function (msg, type) {
        if (!pwMessage) return;
        pwMessage.textContent = msg || "";
        pwMessage.classList.remove("success", "error");
        if (type) pwMessage.classList.add(type);
    };


    // 탭 벗어나면 초기화
    const resetPasswordTab = function () {
        if (!step1 || !step2) return;

        setPwMsg("", null);

        const newPw = step2.querySelector('input[name="newPassword"]');
        const newPwC = step2.querySelector('input[name="newPasswordConfirm"]');

        if (currentPwInput) currentPwInput.value = "";
        if (newPw) newPw.value = "";
        if (newPwC) newPwC.value = "";

        step2.classList.add("hidden");
        step1.classList.remove("hidden");

        step2.querySelectorAll('input[name="newPassword"], input[name="newPasswordConfirm"]')
            .forEach(function (inp) {
                inp.disabled = true;
            });
    };

    tabButtons.forEach(function (btn) {
        btn.addEventListener("click", function () {
            const targetId = btn.dataset.target;
            if (!targetId) return;

            // 비밀번호 탭을 벗어나면 초기화
            if (targetId !== "tab-password") resetPasswordTab();

            clearAllMessages();

            activateTab(targetId);

            history.replaceState(null, "", "#" + targetId);

        });
    });

    // 메세지 자동으로 5초후 삭제
    setTimeout(function () {clearAllMessages();}, 5000);


    if (hashId && document.getElementById(hashId)) {
        activateTab(hashId);
    } else if (saved && document.getElementById(saved)) {
        activateTab(saved);
    }

    if (location.hash) {
        const hashId = location.hash.replace("#", "");
        if (document.getElementById(hashId)) activateTab(hashId);
    }

    // 닉네임 영역 초기 상태 강제
    if (nicknameDisplay && nicknameEditBox) {
        nicknameDisplay.classList.remove("hidden");
        nicknameEditBox.classList.add("hidden");
    }

    // 중복확인 통과 상태
    let nicknameCheckedOk = false;
    let lastCheckedNickname = "";

    const setNickMsg = function (msg, type) {
        if (!nicknameMessage) return;
        nicknameMessage.textContent = msg || "";
        nicknameMessage.classList.remove("success", "error");
        if (type) nicknameMessage.classList.add(type);
    };

    const openNickEdit = function () {
        if (!nicknameDisplay || !nicknameEditBox) return;
        nicknameDisplay.classList.add("hidden");
        nicknameEditBox.classList.remove("hidden");
        if (nicknameInput) {
            nicknameInput.value = "";
            nicknameInput.focus();
        }
        nicknameCheckedOk = false;
        lastCheckedNickname = "";
        setNickMsg("", null);
    };

    const closeNickEdit = function () {
        if (!nicknameDisplay || !nicknameEditBox) return;
        nicknameDisplay.classList.remove("hidden");
        nicknameEditBox.classList.add("hidden");
        nicknameCheckedOk = false;
        lastCheckedNickname = "";
        setNickMsg("", null);
    };

    editNicknameBtn?.addEventListener("click", openNickEdit);
    cancelNicknameBtn?.addEventListener("click", closeNickEdit);

    // 입력이 바뀌면 “중복확인 다시 필요”
    nicknameInput?.addEventListener("input", function () {
        nicknameCheckedOk = false;
        lastCheckedNickname = "";
        setNickMsg("", null);
    });

    const checkNicknameAvailability = function (nickname) {
        return fetch("/mypage/nickname/check?nickname=" + encodeURIComponent(nickname), {
            method: "GET",
            headers: { "Accept": "application/json" },
            credentials: "same-origin"
        }).then(function (res) {
            if (!res.ok) throw new Error("중복 확인 실패");
            return res.json();
        });
    };

    // 닉네임 입력칸에서 Enter 누르면 submit 막기
    nicknameInput?.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            // 엔터 = 중복확인 버튼 클릭처럼 동작
            checkNicknameBtn?.click();
        }
    })

    checkNicknameBtn?.addEventListener("click", function () {
        const newNick = nicknameInput?.value.trim() ?? "";
        setNickMsg("", null);

        if (!newNick) {
            setNickMsg("새 닉네임을 입력해주세요.", "error");
            nicknameInput?.focus();
            return;
        }

        const currentNick = nicknameText?.textContent.trim() ?? "";
        if (newNick === currentNick) {
            setNickMsg("현재 닉네임과 동일합니다.", "error");
            return;
        }

        checkNicknameAvailability(newNick)
            .then(function (data) {
                const available = !!(data.available ?? data.isAvailable ?? data.ok);
                const message = data.message || (available ? "사용 가능합니다." : "이미 사용 중입니다.");
                setNickMsg(message, available ? "success" : "error");

                nicknameCheckedOk = available;
                lastCheckedNickname = newNick;
            })
            .catch(function () {
                nicknameCheckedOk = false;
                lastCheckedNickname = "";
                setNickMsg("중복 확인을 할 수 없습니다. 저장 시 서버에서 검증합니다.", "error");
            });
    });

    const nicknameForm =
        saveNicknameBtn?.closest("form") ??
        document.querySelector('form[action*="/mypage/nickname"]');

    saveNicknameBtn?.addEventListener("click", function () {
        const newNick = nicknameInput?.value.trim() ?? "";
        setNickMsg("", null);

        if (!newNick) {
            setNickMsg("새 닉네임을 입력해주세요.", "error");
            nicknameInput?.focus();
            return;
        }

        const currentNick = nicknameText?.textContent.trim() ?? "";
        if (newNick === currentNick) {
            setNickMsg("현재 닉네임과 동일합니다.", "error");
            return;
        }

        if (!nicknameForm) {
            setNickMsg("닉네임 저장 폼이 없습니다. (버튼이 form 안에 있는지 확인)", "error");
            return;
        }

        // 저장 직전에 한 번 더 서버 검증
        checkNicknameAvailability(newNick)
            .then(function (data) {
                const available = !!(data.available ?? data.isAvailable ?? data.ok);
                if (!available) {
                    setNickMsg(data.message || "이미 사용 중입니다.", "error");
                    nicknameCheckedOk = false;
                    lastCheckedNickname = "";
                    return;
                }

                // 중복확인 버튼을 누르지 않았어도 저장 가능하게(UX 좋음)
                nicknameCheckedOk = true;
                lastCheckedNickname = newNick;

                // UI 즉시 반영
                nicknameText.textContent = newNick;
                document.querySelectorAll(".nickname").forEach(function (el) {
                    el.textContent = newNick;
                });

                let hiddenInput = nicknameForm.querySelector('input[name="nickname"]');
                if (!hiddenInput) {
                    hiddenInput = document.createElement("input");
                    hiddenInput.type = "hidden";
                    hiddenInput.name = "nickname";
                    nicknameForm.appendChild(hiddenInput);
                }
                hiddenInput.value = newNick;
                nicknameForm.submit();
            })
            .catch(function () {
                setNickMsg("서버 확인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "error");
            });
    });

    // step1에서 엔터 = 확인 버튼
    currentPwInput?.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            verifyBtn?.click();
        }
    });

    // step2 열리기 전 submit 방지
    pwForm?.addEventListener("submit", function (e) {
        const isStep2Open = step2 && !step2.classList.contains("hidden");
        if (!isStep2Open) {
            e.preventDefault();
            setPwMsg("현재 비밀번호 확인을 먼저 진행해주세요.", "error");
        }
    });

    // 초기에는 step2 비활성화
    if (step2) {
        step2.querySelectorAll('input[name="newPassword"], input[name="newPasswordConfirm"]')
            .forEach(function (inp) {
                inp.disabled = true;
            });
    }

    // 비밀번호
    const verifyCurrentPassword = function (currentPassword) {
        const headers = { "Content-Type": "application/json", "Accept": "application/json" };
        if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

        return fetch("/mypage/password/verify", {
            method: "POST",
            headers,
            credentials: "same-origin",
            body: JSON.stringify({ currentPassword })
        }).then(function (res) {
            if (!res.ok) throw new Error("비밀번호 검증 실패");
            return res.json();
        });
    };

    // 확인 버튼 클릭
    verifyBtn?.addEventListener("click", function () {
        const currentPw = currentPwInput?.value.trim() ?? "";

        setPwMsg("", null);

        if (!currentPw) {
            alert("현재 비밀번호를 입력해주세요.");
            currentPwInput?.focus();
            return;
        }
        verifyCurrentPassword(currentPw)
            .then(function (data) {
                const match = !!(data.match ?? data.ok ?? data.valid);

                if (!match) {
                    setPwMsg(data.message || "현재 비밀번호가 일치하지 않습니다.", "error");
                    currentPwInput?.focus();
                    return;
                }

                setPwMsg(data.message || "현재 비밀번호가 일치합니다.", "success");

                step1.classList.add("hidden");
                step2.classList.remove("hidden");

                step2.querySelectorAll('input[name="newPassword"], input[name="newPasswordConfirm"]')
                    .forEach(function (inp) {
                        inp.disabled = false;
                    });

                step2.querySelector('input[name="newPassword"]')?.focus();
            })
            .catch(function () {
                setPwMsg("비밀번호 확인 중 오류가 발생했습니다.", "error");
            });
    });

    // 탈퇴 모달
    const openModal = function () {
        if (!modal) return;
        modal.classList.add("is-open");
        modal.setAttribute("aria-hidden", "false");
    };

    const closeModal = function () {
        if (!modal) return;
        modal.classList.remove("is-open");
        modal.setAttribute("aria-hidden", "true");
    };

    openBtn?.addEventListener("click", openModal);
    closeBtn?.addEventListener("click", closeModal);

    modal?.addEventListener("click", function (e) {
        if (e.target === modal) closeModal();
    });

    window.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && modal?.classList.contains("is-open")) closeModal();
    });
});
function moveParent(url) {
    if (window.opener && !window.opener.closed) {
        window.opener.location.href = url;
        window.opener.focus();
    } else {
        window.location.href = url;
    }
}
