pipeline {
    agent any
    tools {
        nodejs 'npm'
    }
    environment {
        APP_DIR = 'app'
        REMOTE_USER = 'root'
        REMOTE_HOST = '134.122.89.95'
        CONTAINER_NAME = 'demo-app'
        APP_PORT = '3000'
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
                    
                    // Add changes and commit
                    sh 'git add .'
                    sh """
                        git commit -m "ci: version bump to ${env.VERSION}" \
                        -m "Build: #${BUILD_NUMBER}" \
                        -m "Image: alexthm1/demo-app:${env.IMAGE_NAME}" || echo "No changes to commit"
                    """
                    
                    // Pull before pushing
                    sh "git pull origin for-testing || true"
                    
                    // Use Jenkins credentials to push
                    withCredentials([string(credentialsId: 'gittoken', variable: 'TOKEN')]) {
                        sh '''
                            set +x
                            git push https://${TOKEN}@github.com/lupindevv/jenkins-ex.git HEAD:for-testing
                            set -x
                        '''
                    }
                }
            }
        }
        
        stage('Deploy to Remote Server') {
            steps {
                script {
                    echo "Deploying to remote server ${REMOTE_HOST}..."
                    
                    // Create deployment script
                    writeFile file: 'deploy.sh', text: """#!/bin/bash
                        # Pull the latest image
                        docker pull alexthm1/demo-app:${env.IMAGE_NAME}
                        
                        # Stop and remove existing container if it exists
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                        
                        # Run the new container
                        docker run -d \\
                            --name ${CONTAINER_NAME} \\
                            -p ${APP_PORT}:${APP_PORT} \\
                            --restart unless-stopped \\
                            alexthm1/demo-app:${env.IMAGE_NAME}
                            
                        # Display container status
                        docker ps | grep ${CONTAINER_NAME}
                    """
                    
                    // Make the script executable
                    sh "chmod +x deploy.sh"
                    
                    // Use SSH Agent for simplified deployment
                    sshagent(['jenkins-ssh']) {
                        sh """
                            # Copy deployment script to server
                            scp -o StrictHostKeyChecking=no deploy.sh ${REMOTE_USER}@${REMOTE_HOST}:~/
                            
                            # Execute deployment script
                            ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} 'chmod +x ~/deploy.sh && ~/deploy.sh'
                        """
                    }
                    
                    echo "Deployment completed successfully"
                }
            }
        }
    }
    post {
        success {
            echo "Pipeline executed successfully"
        }
        failure {
            echo "Pipeline execution failed"
        }
    }
}