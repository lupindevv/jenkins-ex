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
        DOCKER_REPO = 'alexthm1/demo-app'
    }
    stages {
        stage('Load Functions') {
            steps {
                script {
                    // Load the shared functions
                    functions = load "jenkins-functions.groovy"
                }
            }
        }
        
        stage('Build App') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        // Use the correct function reference
                        functions.buildNPM()
                    }
                }
            }
        }
        
        stage('Increment Version') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        // Use the correct function name from your script (incrementVS not incrementVersion)
                        functions.incrementVS()
                    }
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        functions.runTests()
                    }
                }
            }
            post {
                failure {
                    echo 'Tests failed'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    functions.buildDockerImage()
                }
            }
        }
        
        stage('Commit New Version') {
            steps {
                script {
                    functions.commitNewVersion()
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
                        docker pull ${DOCKER_REPO}:${env.IMAGE_NAME}
                        
                        # Stop and remove existing container if it exists
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                        
                        # Run the new container
                        docker run -d \\
                            --name ${CONTAINER_NAME} \\
                            -p ${APP_PORT}:${APP_PORT} \\
                            --restart unless-stopped \\
                            ${DOCKER_REPO}:${env.IMAGE_NAME}
                            
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