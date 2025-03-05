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
            def functions = load "jenkins-functions.groovy"
            // Make them accessible in the pipeline
            env.functions = functions
        }
    }
}
stage('build app with NPM') {
    steps {
        dir(env.APP_DIR) {
            script {
                functions.buildNPM()
            }
        }
    }
}
        stage('build app with NPM') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        buildNPM()
                    }
                }
            }
        }
        
        stage('increment version') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        incrementVersion()
                    }
                }
            }
        }
        
        stage('run tests') {
            steps {
                dir(env.APP_DIR) {
                    script {
                        runTests()
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
                    buildDockerImage()
                }
            }
        }
        
        stage('commit new version') {
            steps {
                script {
                    commitNewVersion()
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