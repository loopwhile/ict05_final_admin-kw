// 통합 배포용 Jenkinsfile (admin-kw 프로젝트)
// 이 파이프라인이 admin과 user 프로젝트를 모두 배포합니다.

pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
        COMPOSE_FILE             = 'docker-compose.yml'
    }

    stages {
        // 1단계: Admin 프로젝트(현재 프로젝트) 체크아웃
        stage('Checkout Admin Project') {
            steps {
                echo 'Checking out admin-kw source code...'
                checkout scm
            }
        }

        // 2단계: User 프로젝트 체크아웃 (하위 디렉터리에)
        stage('Checkout User Project') {
            steps {
                echo 'Checking out user-kw source code into a subdirectory...'
                // docker-compose.yml의 build context 경로에 맞춰 하위 디렉터리에 체크아웃
                dir('ict05_final_user-kw') {
                    git branch: 'main', url: 'https://github.com/loopwhile/ict05_final_user-kw.git'
                }
            }
        }

        // 3단계: Docker Hub 로그인 (보안 강화된 방식)
        stage('Login to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        // 4단계: 통합 빌드 및 배포 (자동 감지 기능 추가)
        stage('Build and Deploy All Services') {
            steps {
                echo "Building and deploying all services using ${COMPOSE_FILE}..."
                sh '''
                    set -e # 오류 발생 시 즉시 중단

                    # Compose 명령 자동 감지
                    if docker compose version >/dev/null 2>&1; then
                        COMPOSE="docker compose"
                    elif command -v docker-compose >/dev/null 2>&1; then
                        COMPOSE="docker-compose"
                    else
                        echo "ERROR: Docker Compose가 이 Jenkins 에이전트에 설치되어 있지 않습니다."
                        exit 1
                    fi

                    # 안정적인 배포를 위해 pull -> build -> up 순서로 실행
                    echo "Using compose command: $COMPOSE"
                    $COMPOSE -f docker-compose.yml pull
                    $COMPOSE -f docker-compose.yml build
                    $COMPOSE -f docker-compose.yml up -d
                '''
            }
        }
    }

    post {
        always {
            // 작업 후 불필요한 이미지 정리 (dangling 이미지)
            echo 'Cleaning up dangling Docker images...'
            sh 'docker image prune -f'
            
            // Docker Hub 로그아웃
            echo 'Logging out from Docker Hub...'
            sh 'docker logout'

            echo 'Unified pipeline finished.'
        }
    }
}