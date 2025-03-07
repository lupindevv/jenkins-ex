pipeline {
    agent any
    tools {
        nodejs 'npm'  // Make sure this tool is configured in Jenkins
    }
    environment {  // Fixed spelling from "envirnoment" to "environment"
        APP_DIR = 'app'
    }
    stages {
        stage('build app') {
            steps {
                dir(env.APP_DIR) {  // Fixed from "dev" to "dir"
                    script {
                        echo 'Installing dependencies...'
                        sh 'npm install'
                    }
                }
            }
        }
        
        stage('increment version') {
            steps {
                dir(env.APP_DIR) {  // Fixed from "dev" to "dir"
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
                dir(env.APP_DIR) {  // Added dir block for test stage
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
                        sh "echo \$PASS | docker login -u \$USER --password-stdin"  // More secure login method
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
                    
                    // Add changes and commit
                    sh 'git add .'
                    sh 'git commit -m "ci: version bump" || echo "No changes to commit"'
                    sh "git pull origin for-testing"
                    
                    // Use Jenkins credentials to push
                    withCredentials([string(credentialsId: 'githubtoken', variable: 'TOKEN')]) {
                        sh "git push https://${TOKEN}@github.com/lupindevv/jenkins-ex.git HEAD:for-testing"
                    }
                } 
            }
        }
    }
}

 