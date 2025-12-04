// 통합 배포용 Jenkinsfile (admin-kw 프로젝트)
// 이 파이프라인이 admin과 user 프로젝트를 모두 배포합니다.

pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
        COMPOSE_FILE             = 'docker-compose.yml'
        COMPOSE_PLUGIN_VERSION   = 'v2.27.0'
        USER_PROJECT_DIR         = 'ict05_final_user-kw'
        FIREBASE_CREDENTIALS_ID  = 'firebase-admin-key'
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
                    echo "Debugging: Listing files in current workspace..."
                    sh 'ls -la'
        
                    echo "Building and deploying all services using ${COMPOSE_FILE}..."
                    withCredentials([file(credentialsId: "${env.FIREBASE_CREDENTIALS_ID}", variable: 'FIREBASE_ADMIN_KEY_FILE')]) {
                        sh label: 'Compose build & up', script: '''
                            set -euxo pipefail

                            mkdir -p fcm-secret
                            cp "$FIREBASE_ADMIN_KEY_FILE" fcm-secret/firebase-admin.json

                            CF="${COMPOSE_FILE:-docker-compose.yml}"
                            WS="${WORKSPACE:-$(pwd)}"
                            USER_PROJECT_PATH="${USER_PROJECT_PATH:-$WS/${USER_PROJECT_DIR:-ict05_final_user-kw}}"

                            if [ -d "$USER_PROJECT_PATH" ]; then
                                mkdir -p "$USER_PROJECT_PATH/fcm-secret"
                                cp "$FIREBASE_ADMIN_KEY_FILE" "$USER_PROJECT_PATH/fcm-secret/firebase-admin.json"
                            fi

                        if [ ! -d "$USER_PROJECT_PATH" ]; then
                            echo "WARNING: USER_PROJECT_PATH ($USER_PROJECT_PATH) does not exist."
                        fi
                        export USER_PROJECT_PATH
                        echo "USER_PROJECT_PATH resolved to: $USER_PROJECT_PATH"

                        # docker compose v2 플러그인이 Jenkins 이미지에 없을 경우 즉석 설치
                        CLI_PLUGIN_DIR="${HOME:-/var/jenkins_home}/.docker/cli-plugins"
                        if ! docker compose version >/dev/null 2>&1; then
                            echo "docker compose plugin not detected. Installing ${COMPOSE_PLUGIN_VERSION:-v2.27.0} locally..."
                            mkdir -p "$CLI_PLUGIN_DIR"

                            DOWNLOAD_URL="https://github.com/docker/compose/releases/download/${COMPOSE_PLUGIN_VERSION:-v2.27.0}/docker-compose-linux-x86_64"
                            if command -v curl >/dev/null 2>&1; then
                                curl -fsSL "$DOWNLOAD_URL" -o "$CLI_PLUGIN_DIR/docker-compose"
                            elif command -v wget >/dev/null 2>&1; then
                                wget -qO "$CLI_PLUGIN_DIR/docker-compose" "$DOWNLOAD_URL"
                            else
                                echo "ERROR: Neither curl nor wget is available to download docker compose." >&2
                                exit 5
                            fi

                            chmod +x "$CLI_PLUGIN_DIR/docker-compose"
                        fi

                        # 워크스페이스에 compose 파일 존재 확인
                        if [ -f "$CF" ]; then
                            CF_HOST_PATH="$WS/$CF"   # WS와 동일 위치라면 이 절도 안전
                            [ -f "$CF_HOST_PATH" ] || CF_HOST_PATH="$CF"
                        elif [ -f "$WS/$CF" ]; then
                            CF_HOST_PATH="$WS/$CF"
                        else
                            echo "ERROR: ${WS} 내에 ${CF} 파일을 찾지 못했습니다."
                            exit 2
                        fi
        
                        # 로컬 Compose 우선 사용 (v2 → v1)
                        if docker compose version >/dev/null 2>&1; then
                            COMPOSE="docker compose"
                            CF_ARG="$CF_HOST_PATH"   # 로컬은 호스트 경로 그대로 사용
                        elif command -v docker-compose >/dev/null 2>&1; then
                            COMPOSE="docker-compose"
                            CF_ARG="$CF_HOST_PATH"
                        else
                            echo "Docker Compose not found locally, falling back to containerized compose."
                            COMPOSE_IMAGE="docker/compose:2.27.0"
        
                            # 컨테이너에서는 /work 로 고정 마운트 후 절대경로 사용
                            COMPOSE="docker run --rm -i \
                                -v /var/run/docker.sock:/var/run/docker.sock \
                                -v \"$WS\":/work -w /work \
                                -v \"${HOME:-/var/jenkins_home}/.docker\":/root/.docker:ro \
                                $COMPOSE_IMAGE"
                            CF_ARG="/work/$(basename \"$CF_HOST_PATH\")"
                        fi
        
                        echo "Using compose command: $COMPOSE"
                        echo "Compose file (arg):   $CF_ARG"
        
                        # 사전 가시성 점검(컨테이너에서도 보이는지 확인)
                        if echo "$COMPOSE" | grep -q '^docker run'; then
                            docker run --rm -i -v "$WS":/work -w /work alpine:3.20 ls -la /work || true
                            docker run --rm -i -v "$WS":/work -w /work alpine:3.20 test -f "$CF_ARG"
                        else
                            test -f "$CF_ARG"
                        fi
        
                        # 안정화 시퀀스: pull → build → up --remove-orphans
                        $COMPOSE -f "$CF_ARG" pull --ignore-pull-failures || true
                        $COMPOSE -f "$CF_ARG" build --pull
                        $COMPOSE -f "$CF_ARG" up -d --remove-orphans
        
                        $COMPOSE -f "$CF_ARG" ps
                    '''
                    }
                }
            }    }

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
