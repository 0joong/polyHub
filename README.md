# 📘 PolyHub (폴리허브)

## 📖 프로젝트 소개  
**PolyHub**는 한국폴리텍대학 성남캠퍼스 학생들을 위한 **커뮤니티 및 정보 제공 플랫폼**입니다.  
학내 공지사항, 학사일정, 식단 정보 등을 제공하고, 게시판, 설문, 메신저, AI 챗봇 기능 등을 통해 학생 간의 소통과 정보 공유를 지원합니다.

---

## ✨ 주요 기능

### 👤 회원 관리
- 이메일 기반 회원가입 및 인증  
- Spring Security를 이용한 로그인 및 권한 관리  
- 얼굴 인식을 통한 간편 로그인  
- 아이디/비밀번호 찾기, 비밀번호 변경, 회원 탈퇴 기능  

### 📋 게시판
- **공지사항**: 관리자가 등록 및 상단 고정 가능  
- **자유게시판**:  
  - Toast UI Editor를 활용한 글 작성/수정/삭제 (CRUD)  
  - 댓글/대댓글, 추천/비추천 기능  
- **자료실**: 파일 업로드 및 다운로드 지원  

### 📊 설문조사
- 단일/다중 객관식, 주관식 질문을 동적으로 추가  
- 설문 기간 설정 및 상태(진행중 / 시작 전 / 마감) 관리  
- Chart.js를 통한 설문 결과 시각화  

### 📅 학사일정 및 정보
- FullCalendar 연동으로 시각적인 학과 및 학사 일정 제공  
- Jsoup, Selenium을 통한 자동 크롤링으로:
  - 주간 식단표  
  - 학사일정 자동 업데이트
- 학교 도서관 도서정보 연동으로:
  - 도서관 도서 대출 여부 확인

### 🤖 AI 챗봇 및 실시간 메신저
- **AI 챗봇**:  
  - RAG(Retrieval-Augmented Generation) 기술 적용  
  - 학과 관련 질문에 대해 정확한 정보 제공  
- **실시간 메신저**:  
  - WebSocket + STOMP 기반 실시간 채팅  
  - Firebase FCM 및 SSE를 통한 실시간 알림  

### 👑 관리자 기능
- 회원 관리 (권한 변경, 강제 탈퇴)  
- 관리자 대시보드로 시스템 현황 모니터링  
- 전체 또는 특정 권한 그룹 대상 알림 발송  

---

## 🛠 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Java, Spring Boot, Spring Security, Spring Data JPA, MyBatis |
| **Database** | Oracle DB, Redis (캐싱) |
| **Frontend** | HTML, CSS, JavaScript, Thymeleaf, Bootstrap, jQuery |
| **크롤링** | Jsoup, Selenium, HtmlUnit |
| **실시간 통신** | WebSocket, STOMP, Server-Sent Events (SSE), Firebase Cloud Messaging (FCM) |
| **라이브러리/기타** | Lombok, FullCalendar, Toast UI Editor, Chart.js |

---

