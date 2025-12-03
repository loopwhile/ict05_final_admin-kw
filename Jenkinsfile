// 통합 배포용 Jenkinsfile (admin-kw 프로젝트)
// 이 파이프라인이 admin과 user 프로젝트를 모두 배포합니다.

pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
        DOCKERHUB_USERNAME       = 'loopwhile'
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

        // 3단계: Docker Hub 로그인
        stage('Login to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                }
            }
        }

        // 4단계: 통합 빌드 및 배포
        stage('Build and Deploy All Services') {
            steps {
                echo "Building and deploying all services using ${COMPOSE_FILE}..."
                // docker-compose.yml에 정의된 모든 서비스를 빌드하고 재시작
                sh "docker-compose -f ${COMPOSE_FILE} up -d --build"
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