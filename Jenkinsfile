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
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        DOCKERHUB_USERNAME    = 'loopwhile' // As per DevOps.md
        ADMIN_BACKEND_IMAGE   = "${DOCKERHUB_USERNAME}/ict05-final-admin-backend"
        ADMIN_PDF_IMAGE       = "${DOCKERHUB_USERNAME}/ict05-final-admin-pdf"
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
                script {
                    echo "Building Admin Backend Docker image..."
                    // 백엔드용 Dockerfile이 프로젝트 루트에 있다고 가정
                    def customImage = docker.build(ADMIN_BACKEND_IMAGE, ".")
                    
                    echo "Pushing Admin Backend image to Docker Hub..."
                    docker.withRegistry('https://registry.hub.docker.com', DOCKERHUB_CREDENTIALS) {
                        customImage.push("latest")
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
                    docker.withRegistry('https://registry.hub.docker.com', DOCKERHUB_CREDENTIALS) {
                        customImage.push("latest")
                    }
                }
            }
        }

        // Stage 4: Trigger the deployment on the EC2 server
        stage('Deploy to EC2') {
            steps {
                echo "Executing deployment script on EC2 for 'admin' services..."
                // Jenkins가 EC2 인스턴스에서 직접 실행되므로, 스크립트를 바로 실행
                sh '/home/ubuntu/deploy/deploy.sh admin'
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