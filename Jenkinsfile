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

        // 4단계: 통합 빌드 및 배포 (자동 감지 및 컨테이너 폴백 기능)
        stage('Build and Deploy All Services') {
            steps {
                echo "Building and deploying all services using ${COMPOSE_FILE}..."
                sh label: 'Compose build & up', script: '''
                    set -euxo pipefail

                    # Compose 명령 자동 감지 (v2 → v1 → 컨테이너 폴백)
                    if docker compose version >/dev/null 2>&1; then
                        COMPOSE="docker compose"
                    elif command -v docker-compose >/dev/null 2>&1; then
                        COMPOSE="docker-compose"
                    else
                        echo "Docker Compose not found locally, falling back to containerized compose."
                        COMPOSE_IMAGE="docker/compose:1.29.2"
                        # 경로에 불필요한 따옴표나 이스케이프가 들어가지 않도록 수정
                        COMPOSE="docker run --rm -i \
                            -v /var/run/docker.sock:/var/run/docker.sock \
                            -v $WORKSPACE:$WORKSPACE -w $WORKSPACE \
                            -v ${HOME}/.docker:/root/.docker:ro \
                            ${COMPOSE_IMAGE}"
                    fi

                    echo "Using compose command: $COMPOSE"

                    # 안정적인 배포를 위해 pull -> build -> up 순서로 실행
                    $COMPOSE -f docker-compose.yml pull --ignore-pull-failures
                    $COMPOSE -f docker-compose.yml build
                    $COMPOSE -f docker-compose.yml up -d --remove-orphans
                    
                    echo "Deployment finished. Showing running containers:"
                    $COMPOSE -f docker-compose.yml ps
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
