// Jenkinsfile for the Admin Project

pipeline {
    // Run on any available agent
    agent any

    tools {
        // Use the 'docker' tool configured in Jenkins Global Tool Configuration
        dockerTool 'docker'
    }

    // Environment variables used throughout the pipeline
    environment {
        // [수정된 부분] Credentials ID 문자열을 환경 변수에 직접 할당
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
        DOCKERHUB_USERNAME       = 'loopwhile' // As per DevOps.md
        ADMIN_BACKEND_IMAGE      = "${DOCKERHUB_USERNAME}/ict05-final-admin-backend"
        ADMIN_PDF_IMAGE          = "${DOCKERHUB_USERNAME}/ict05-final-admin-pdf"
    }

    stages {
        // Stage 1: Checkout source code from Git
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                git branch: 'main', url: 'https://github.com/loopwhile/ict05_final_admin-kw.git'
            }
        }

        // Stage 2: Build and push the Spring Boot backend image
        stage('Build & Push Admin Backend') {
            steps {
                // Use withCredentials to access the secret file
                withCredentials([file(credentialsId: 'firebase-admin-key', variable: 'FIREBASE_ADMIN_KEY_FILE')]) {
                    script {
                        // Use a try-finally block to ensure the secret file is cleaned up
                        try {
                            echo "Preparing secret file for Docker build..."
                            // Create the directory and copy the secret file to the location expected by the Dockerfile
                            sh 'mkdir -p fcm-secret'
                            sh 'cp $FIREBASE_ADMIN_KEY_FILE fcm-secret/firebase-admin.json'

                            echo "Building Admin Backend Docker image..."
                            def customImage = docker.build(ADMIN_BACKEND_IMAGE, ".")
                            
                            echo "Pushing Admin Backend image to Docker Hub..."
                            // [수정된 부분] DOCKERHUB_CREDENTIALS_ID 변수 사용
                            docker.withRegistry('https://registry.hub.docker.com', DOCKERHUB_CREDENTIALS_ID) {
                                customImage.push("latest")
                            }
                        } finally {
                            // Clean up the secret file and directory from the workspace
                            echo "Cleaning up secret file..."
                            sh 'rm -rf fcm-secret'
                        }
                    }
                }
            }
        }

        // Stage 3: Build and push the Python PDF service image
        stage('Build & Push Admin PDF Service') {
            steps {
                script {
                    echo "Building Admin PDF Service Docker image..."
                    // PDF 서비스용 Dockerfile이 'python-pdf-download' 하위 폴더에 있다고 가정
                    def customImage = docker.build(ADMIN_PDF_IMAGE, "python-pdf-download")
                    
                    echo "Pushing Admin PDF Service image to Docker Hub..."
                    // [수정된 부분] DOCKERHUB_CREDENTIALS_ID 변수 사용
                    docker.withRegistry('https://registry.hub.docker.com', DOCKERHUB_CREDENTIALS_ID) {
                        customImage.push("latest")
                    }
                }
            }
        }

        // Stage 4: Trigger the deployment on the EC2 server
        stage('Deploy to EC2') {
            steps {
                echo "Executing deployment script on EC2 via SSH..."
                // 등록한 SSH 키 파일을 사용해 EC2에 접속하여 스크립트 실행
                withCredentials([file(credentialsId: 'ec2-ssh-key', variable: 'SSH_KEY_FILE')]) {
                    script {
                        // 1. 키 파일 권한 설정 (필수)
                        sh 'chmod 600 $SSH_KEY_FILE'
                        
                        // 2. SSH로 접속해서 스크립트 실행 (StrictHostKeyChecking=no 옵션으로 접속 확인 무시)
                        // 주의: ubuntu@172.31.37.197 부분은 사용자님의 EC2 사설 IP입니다.
                        sh "ssh -o StrictHostKeyChecking=no -i $SSH_KEY_FILE ubuntu@172.31.37.197 '/home/ubuntu/deploy/deploy.sh admin'"
                    }
                }
            }
        }
    }

    // Post-build actions
    post {
        always {
            echo 'Admin pipeline finished.'
        }
    }
}
