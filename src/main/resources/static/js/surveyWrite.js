// 질문 인덱스 카운터
let questionIndex = 0;

// "질문 추가" 버튼 클릭 이벤트
document.getElementById('addQuestionBtn').addEventListener('click', () => {
    const questionsContainer = document.getElementById('questionsContainer');

    const questionCard = document.createElement('div');
    questionCard.className = 'card mb-3 question-card';
    questionCard.innerHTML = `
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="card-title mb-0">질문 ${questionIndex + 1}</h5>
                    <button type="button" class="btn-close" onclick="removeQuestion(this)"></button>
                </div>
                <div class="row">
                    <div class="col-md-8 mb-3">
                        <label class="form-label">질문 내용 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" name="questions[${questionIndex}].questionText" required>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">질문 유형</label>
                        <select class="form-select" name="questions[${questionIndex}].questionType" onchange="toggleOptions(this)">
                            <option value="SINGLE_CHOICE" selected>객관식 (단일선택)</option>
                            <option value="MULTIPLE_CHOICE">객관식 (다중선택)</option>
                            <option value="TEXT">주관식</option>
                        </select>
                    </div>
                </div>
                <div class="form-check mb-3">
                    <input type="checkbox" class="form-check-input" name="questions[${questionIndex}].required" value="true" checked>
                    <input type="hidden" name="_questions[${questionIndex}].required" value="on" />
                    <label class="form-check-label">필수 응답</label>
                </div>

                <!-- 선택지 영역 -->
                <div class="options-container">
                    <label class="form-label">선택지</label>
                    <div class="option-list">
                        <!-- 선택지가 여기에 동적으로 추가됩니다. -->
                    </div>
                    <button type="button" class="btn btn-sm btn-outline-primary mt-2" onclick="addOption(this)">선택지 추가</button>
                </div>
            </div>
        `;

    questionsContainer.appendChild(questionCard);
    questionIndex++;
});

// 질문 삭제 함수
function removeQuestion(button) {
    button.closest('.question-card').remove();
    // 참고: 질문 삭제 시 인덱스를 재정렬하는 로직은 복잡하므로,
    // 서버에서 null 값을 무시하도록 처리하는 것이 더 간단합니다.
}

// 질문 유형 변경 시 선택지 영역 보이기/숨기기
function toggleOptions(selectElement) {
    const optionsContainer = selectElement.closest('.card-body').querySelector('.options-container');
    if (selectElement.value === 'TEXT') {
        optionsContainer.style.display = 'none';
    } else {
        optionsContainer.style.display = 'block';
    }
}

// 선택지 추가 함수
function addOption(button) {
    const optionList = button.closest('.options-container').querySelector('.option-list');
    const qIndex = Array.from(document.querySelectorAll('.question-card')).indexOf(button.closest('.question-card'));
    const optionIndex = optionList.children.length;

    const optionDiv = document.createElement('div');
    optionDiv.className = 'option-input-group mb-2';
    optionDiv.innerHTML = `
            <input type="text" class="form-control" name="questions[${qIndex}].options[${optionIndex}]" required>
            <button type="button" class="btn btn-sm btn-outline-danger" onclick="this.parentElement.remove()">삭제</button>
        `;
    optionList.appendChild(optionDiv);
}