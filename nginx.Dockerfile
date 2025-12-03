# Nginx 설정을 포함하는 Docker 이미지 빌드

# 1. 베이스 이미지
FROM nginx:latest

# 2. 로컬의 nginx.conf 파일을 컨테이너의 설정 디렉터리로 복사
# 이 한 줄로 모든 서버 블록과 라우팅 규칙이 적용됩니다.
COPY nginx.conf /etc/nginx/conf.d/default.conf