# TUNI - Trade University (Backend)

## 📖 프로젝트 개요 (Overview)

**TUNI**는 같은 대학교 학생들 간의 신뢰 기반 중고 거래를 위한 웹 플랫폼입니다. 대학교 이메일 인증을 통해 사용자의 신원을 보장하고, 교내 커뮤니티 내에서 안전하고 편리한 거래 환경을 제공하는 것을 목표로 합니다.

## ✨ 핵심 기능 (Core Features)

- **사용자 인증 (User Authentication)**
  - 대학교 이메일(`edu`)을 통한 인증 코드 기반의 Passwordless 로그인
  - JWT(Access Token, Refresh Token)를 이용한 인증 상태 관리 및 자동 갱신
  - Spring Security를 통한 API 경로 보안 및 인가 처리
- **상품 관리 (Product Management)**
  - 상품 등록, 상세 조회, 수정, 삭제 (CRUD) 기능
  - 카테고리, 가격, 판매 상태 등 다양한 조건 관리
- **이미지 처리 (Image Handling)**
  - 다중 이미지 업로드 및 외부 디렉터리 저장
  - `Thumbnailator`를 이용한 썸네일 자동 생성
  - 게시물별 대표 이미지 지정 및 변경
- **사용자 상호작용 (User Interaction)**
  - 게시물 좋아요 / 좋아요 취소 (Toggle) 기능
  - 사용자별 판매/판매완료 상품 수, 좋아요 목록 등 마이페이지 정보 제공
  - WebSocket/STOMP 기반의 실시간 채팅 기능

## 🛠️ 기술 스택 (Tech Stack)

| 구분 | 기술 |
| :--- | :--- |
| **Framework** | Spring Boot 3.4, Spring Security |
| **Language** | Java 21 |
| **Database** | MariaDB, Redis |
| **ORM/Mapper** | MyBatis |
| **Authentication**| JWT (jjwt library) |
| **Build Tool** | Maven |
