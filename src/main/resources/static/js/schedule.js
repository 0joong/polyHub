// DOM이 완전히 로드된 후 스크립트를 실행합니다.
document.addEventListener('DOMContentLoaded', function() {
    // HTML 요소들을 변수에 할당합니다.
    const calendarEl = document.getElementById('calendar');
    const scheduleModal = new bootstrap.Modal(document.getElementById('scheduleModal'));
    const scheduleForm = document.getElementById('scheduleForm');

    // HTML의 data-is-admin 속성에서 관리자 여부를 읽어옵니다.
    const isAdmin = calendarEl.dataset.isAdmin === 'true';

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,listWeek'
        },
        locale: 'ko',
        editable: isAdmin, // 관리자일 때만 드래그 앤 드롭 가능
        events: '/api/schedules', // 서버에서 일정 데이터를 가져올 경로

        // 날짜 클릭 시 동작
        dateClick: function(info) {
            if (!isAdmin) return; // 관리자가 아니면 아무 동작 안함
            scheduleForm.reset();
            document.getElementById('scheduleId').value = '';
            document.getElementById('scheduleModalLabel').innerText = '일정 추가';
            document.getElementById('scheduleStartDate').value = info.dateStr;
            document.getElementById('deleteButton').style.display = 'none';
            scheduleModal.show();
        },

        // 기존 일정 클릭 시 동작
        eventClick: function(info) {
            if (!isAdmin) {
                // 일반 사용자는 일정 내용만 alert로 보여줌
                alert(
                    '제목: ' + info.event.title + '\n' +
                    '기간: ' + info.event.start.toLocaleDateString() +
                    (info.event.end ? ' ~ ' + info.event.end.toLocaleDateString() : '') + '\n' +
                    '내용: ' + (info.event.extendedProps.content || '없음')
                );
                return;
            }
            // 관리자는 수정/삭제 모달을 띄움
            document.getElementById('scheduleId').value = info.event.id;
            document.getElementById('scheduleModalLabel').innerText = '일정 수정';
            document.getElementById('scheduleTitle').value = info.event.title;
            document.getElementById('scheduleStartDate').value = info.event.startStr.split('T')[0];
            document.getElementById('scheduleEndDate').value = info.event.end ? info.event.endStr.split('T')[0] : '';
            document.getElementById('scheduleContent').value = info.event.extendedProps.content || '';
            document.getElementById('deleteButton').style.display = 'block';
            scheduleModal.show();
        }
    });

    // 달력을 화면에 렌더링합니다.
    calendar.render();

    // 저장 버튼 클릭 이벤트
    document.getElementById('saveButton').addEventListener('click', function() {
        const id = document.getElementById('scheduleId').value;
        const title = document.getElementById('scheduleTitle').value;
        const start = document.getElementById('scheduleStartDate').value;
        const end = document.getElementById('scheduleEndDate').value;
        const content = document.getElementById('scheduleContent').value;

        if (!title || !start) {
            alert('일정 제목과 시작일은 필수입니다.');
            return;
        }

        const scheduleData = { title, start, end, content };
        const url = id ? `/api/schedules/${id}` : '/api/schedules';
        const method = id ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(scheduleData)
        })
            .then(response => {
                if (response.ok) {
                    calendar.refetchEvents(); // 달력 데이터 다시 불러오기
                    scheduleModal.hide();     // 모달 닫기
                } else {
                    alert('저장에 실패했습니다.');
                }
            });
    });

    // 삭제 버튼 클릭 이벤트
    document.getElementById('deleteButton').addEventListener('click', function() {
        const id = document.getElementById('scheduleId').value;
        if (!id) return;

        if (confirm('정말로 이 일정을 삭제하시겠습니까?')) {
            fetch(`/api/schedules/${id}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        calendar.refetchEvents();
                        scheduleModal.hide();
                    } else {
                        alert('삭제에 실패했습니다.');
                    }
                });
        }
    });
});
