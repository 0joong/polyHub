async function validateForm() {
    const email = document.getElementById('email').value;
    const code = document.getElementById('verificationCode').value;

    if (!email || !code) {
        alert("이메일과 인증코드를 입력해주세요.");
        return false;
    }

    try {
        const response = await fetch(`/member/verifyCode?email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`);
        const data = await response.json();

        if (!data.success) {
            alert("인증코드가 올바르지 않습니다. 다시 확인해주세요.");
            return false;
        }
    } catch (error) {
        alert("네트워크 오류가 발생했습니다.");
        return false;
    }

    return true;
}

function sendVerificationCode() {
    const email = document.getElementById('email').value;
    if (!email) {
        alert("이메일을 먼저 입력해주세요.");
        return;
    }

    alert("인증 코드가 전송되었습니다!");

    fetch('/member/sendVerificationCode?email=' + email)
        .then(response => response.json())
        .then(data => {
            if (!data.success) {
                alert("오류가 발생했습니다. 다시 시도해주세요.");
            }
        })
        .catch(error => {
            alert("네트워크 오류가 발생했습니다.");
        });
}

function verifyCode() {
    const email = document.getElementById('email').value;
    const code = document.getElementById('verificationCode').value;

    if (!email || !code) {
        alert("이메일과 인증 코드를 입력해주세요.");
        return;
    }

    fetch(`/member/verifyCode?email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`)
        .then(response => response.json())
        .then(data => {
            const resultElement = document.getElementById('verificationResult');
            const faceRegisterBtn = document.getElementById('faceRegisterBtn');
            if (data.success) {
                resultElement.textContent = "✅ 인증 코드가 일치합니다!";
                resultElement.classList.remove("text-danger");
                resultElement.classList.add("text-success");
            } else {
                resultElement.textContent = "❌ 인증 코드가 올바르지 않습니다.";
                resultElement.classList.remove("text-success");
                resultElement.classList.add("text-danger");
            }
        })
        .catch(error => {
            alert("네트워크 오류가 발생했습니다.");
        });
}