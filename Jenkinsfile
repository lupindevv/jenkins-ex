pipeline {
    agent any
    tools {
        nodejs 'npm'  // Make sure this tool is configured in Jenkins
    }
    environment {
        APP_DIR = 'app'
    }
    stages {
        stage('build app') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        echo 'Installing dependencies...'
                        sh 'npm install'
                    }
                }
            }
        }
        
        stage('increment version') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        echo "Incrementing version..."
                        sh 'npm version major --no-git-tag-version'
                        def packageJson = readJSON file: 'package.json'
                        env.VERSION = packageJson.version
                        def matcher = readFile('package.json') =~ /"version": "(.*?)"/
                        env.VERSION = matcher[0][1]
                        env.IMAGE_NAME = "${env.VERSION}-${BUILD_NUMBER}"
                    }
                }
            }
        }
        
        stage('run tests') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        echo 'Running tests..'
                        sh 'npm test'
                    }
                }
            }
            post {
                failure {
                    echo 'Tests failed'
                }
            }
        }
        
        stage('build docker image') {
            steps {
                script {
                    echo "Building docker image..."
                    withCredentials([usernamePassword(credentialsId: 'dockerhubs', usernameVariable: "USER", passwordVariable: "PASS")]) {
                        sh "docker build -t alexthm1/demo-app:${env.IMAGE_NAME} ."
                        sh "echo \$PASS | docker login -u \$USER --password-stdin"
                        sh "docker push alexthm1/demo-app:${env.IMAGE_NAME}"
                    }
                }
            }
        }
        
        stage('commit new version') {
    steps {
        script {
            // Configure git
            sh 'git config --global user.email "jenkins@example.com"'
            sh 'git config --global user.name "jenkins"'
            
            // Make sure we're on the correct branch
            sh 'git checkout for-testing || git checkout -b for-testing'
            
            // Add changes and commit with better message
            sh 'git add .'
            sh """
                git commit -m "ci: version bump to ${env.VERSION}" \
                -m "Build: #${BUILD_NUMBER}" \
                -m "Image: alexthm1/demo-app:${env.IMAGE_NAME}" || echo "No changes to commit"
            """
            
            // Use Jenkins credentials to push with username/password type
            withCredentials([usernamePassword(credentialsId: 'github', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                sh '''
                    set +x
                    git remote set-url origin https://x-access-token:${GIT_PASSWORD}@github.com/lupindevv/jenkins-ex.git
                    git pull origin for-testing || true
                    git push origin for-testing
                    set -x
                '''
            }
        } 
    }
}
    }
}