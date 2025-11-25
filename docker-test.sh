#!/bin/bash

# Docker 테스트 스크립트
# 사용법: ./docker-test.sh [build|up|down|logs|clean]

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수 정의
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 환경 변수 체크
check_env() {
    print_info "환경 변수 파일 확인 중..."
    if [ ! -f .env ]; then
        print_warning ".env 파일이 없습니다. .env.example를 복사합니다."
        cp .env.example .env
        print_info ".env 파일을 수정하고 다시 실행해주세요."
        exit 1
    fi
    print_success "환경 변수 파일 확인 완료"
}

# Docker 이미지 빌드
build_image() {
    print_info "Docker 이미지 빌드 중..."
    docker build -t realmoneyrealtaste:latest .
    print_success "Docker 이미지 빌드 완료"
}

# 컨테이너 시작
start_containers() {
    print_info "Docker 컨테이너 시작 중..."
    docker compose up -d
    print_success "컨테이너 시작 완료"
    
    print_info "서비스 상태 확인 중..."
    sleep 10
    
    # MySQL 상태 확인
    if docker compose exec mysql mysqladmin ping -h localhost > /dev/null 2>&1; then
        print_success "MySQL 정상 실행 중"
    else
        print_error "MySQL 실행 실패"
        docker compose logs mysql
        exit 1
    fi
    
    # 애플리케이션 상태 확인
    print_info "애플리케이션 시작 대기 중..."
    sleep 30
    
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_success "애플리케이션 정상 실행 중"
        print_info "애플리케이션 URL: http://localhost:8080"
    else
        print_error "애플리케이션 실행 실패"
        docker compose logs app
        exit 1
    fi
}

# 컨테이너 중지
stop_containers() {
    print_info "Docker 컨테이너 중지 중..."
    docker compose down
    print_success "컨테이너 중지 완료"
}

# 로그 보기
show_logs() {
    print_info "로그 보기 (Ctrl+C로 종료)..."
    docker compose logs -f
}

# 정리
clean() {
    print_info "Docker 리소스 정리 중..."
    docker compose down -v --remove-orphans
    
    # 프로젝트 관련 이미지 정리
    print_info "프로젝트 Docker 이미지 정리 중..."
    docker rmi realmoneyrealtaste:latest 2>/dev/null || true
    docker rmi realmoneyrealtaste-app 2>/dev/null || true
    docker rmi rmrt-app 2>/dev/null || true
    
    # 사용하지 않는 이미지 정리
    docker image prune -f
    
    # 전체 시스템 정리
    docker system prune -f
    
    print_success "정리 완료"
}

# 메인 로직
case "$1" in
    build)
        check_env
        build_image
        ;;
    up)
        check_env
        build_image
        start_containers
        ;;
    down)
        stop_containers
        ;;
    logs)
        show_logs
        ;;
    clean)
        clean
        ;;
    *)
        echo "사용법: $0 {build|up|down|logs|clean}"
        echo ""
        echo "명령어 설명:"
        echo "  build  - Docker 이미지만 빌드"
        echo "  up     - 이미지 빌드 및 컨테이너 시작"
        echo "  down   - 컨테이너 중지"
        echo "  logs   - 모든 서비스 로그 보기"
        echo "  clean  - 모든 리소스 정리"
        echo ""
        exit 1
        ;;
esac
