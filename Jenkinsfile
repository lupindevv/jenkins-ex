pipeline {
    agent any
    tools {
        nodejs 'npm'
    }
    environment {
        APP_DIR = 'app'
        DOCKER_REPO = 'alexthm1/demo-app'
    }
    stages {
        stage('Increment Version') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        echo "Incrementing patch version..."
                        // Use patch instead of major for smaller version increments
                        sh 'npm version patch --no-git-tag-version'
                        
                        // Read the new version
                        def packageJson = readJSON file: 'package.json'
                        env.VERSION = packageJson.version
                        echo "New version: ${env.VERSION}"
                        
                        // Set the Docker image tag
                        env.IMAGE_TAG = "${env.VERSION}"
                    }
                }
            }
        }
        
        stage('Build and Test') {
            steps {
                dir(env.APP_DIR) {
                    sh 'npm install'
                    sh 'npm test'
                }
            }
        }
        
        stage('Build and Push Docker Image') {
            steps {
                dir(env.APP_DIR) { // Make sure we're in the directory with the Dockerfile
                    script {
                        echo "Building docker image ${DOCKER_REPO}:${env.IMAGE_TAG}..."
                        
                        withCredentials([usernamePassword(credentialsId: 'dockerhubs', usernameVariable: "DOCKER_USER", passwordVariable: "DOCKER_PASS")]) {
                            // Login to DockerHub
                            sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
                            
                            // Build and push the image
                            sh "docker build -t ${DOCKER_REPO}:${env.IMAGE_TAG} ."
                            sh "docker push ${DOCKER_REPO}:${env.IMAGE_TAG}"
                        }
                    }
                }
            }
        }
        
        stage('Commit Version Update') {
            steps {
                script {
                    // Configure git
                    sh 'git config --global user.email "jenkins@example.com"'
                    sh 'git config --global user.name "Jenkins CI"'
                    
                    // Make sure we're on the right branch
                    sh 'git checkout for-testing || git checkout -b for-testing'
                    
                    // Add just the package.json file
                    sh "git add ${env.APP_DIR}/package.json"
                    sh "git commit -m 'ci: bump version to ${env.VERSION}' || echo 'No changes to commit'"
                    
                    // Use proper credential handling to pull and push
                    withCredentials([string(credentialsId: 'github', variable: 'GITHUB_TOKEN')]) {
                        sh """
                            git remote set-url origin https://x-access-token:${GITHUB_TOKEN}@github.com/lupindevv/jenkins-ex.git
                            git pull origin for-testing || true
                            git push origin for-testing
                        """
                    }
                }
            }
        }
    }
}