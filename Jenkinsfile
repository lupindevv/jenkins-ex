pipeline {
    agent any
    tools {
        nodejs 'npm'  // Changed from 'maven' to 'nodejs'
    }
    stages {
        stage('increment version') {
            steps {
                script {
                    echo "Incrementing version..."
                    sh 'npm version major --no-git-tag-version'
                    def packageJson = readJSON file: 'package.json'
                    env.VERSION = packageJson.version
                    def matcher = readFile('package.json') =~ /"version": "(.*?)"/
                    env.VERSION = matcher[0][1]
                    env.IMAGE_NAME = "${env.VERSION}-${BUILD_NUMBER}"  // Fixed variable references
                }
            }
        }
        stage('build app') {
            steps {
                script {
                    echo 'Installing dependencies...'
                    sh 'npm install'
                }
            }
        }
        stage('run tests') {
            steps {
                script {
                    echo 'Running tests..'
                    sh 'npm test'
                }
            }
            post {  // Moved post outside of steps
                failure {
                    echo 'Tests failed'
                }
            }
        }
        stage('build docker image') {
            steps {
                script {
                    echo "building docker image..."
                    withCredentials([usernamePassword(credentialsId: 'dockerhubs', usernameVariable: "USER", passwordVariable: "PASS")]){  // Fixed credential type
                        sh "docker build -t alexthm1/demo-app:${env.IMAGE_NAME} ."  // Added missing dot and fixed variable reference
                        sh "docker login -u $USER -p $PASS"
                        sh "docker push alexthm1/demo-app:${env.IMAGE_NAME}"  // Fixed variable reference
                    }
                }
            }
        }
        stage('commit new version') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                        sh 'git config --global user.email "jenkins@example.com"'
                        sh 'git config --global user.name "jenkins"'

                        sh 'git status'
                        sh 'git branch'
                        sh 'git config --list'

                        sh "git remote set-url origin https://${USER}:${PASS}@github.com/lupindevv/jenkins-ex.git"
                        sh 'git add .'
                        sh 'git commit -m "ci: version bump"'
                        sh 'git push origin HEAD:jenkins-jobs'
                    }
                }  // Added missing closing brace for script block
            }
        }
    }
}