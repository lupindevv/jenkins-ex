#!/usr/bin/env groovy

def buildNPM() {
    // Remove 'script' block - not needed in function definitions
    sh 'npm install'
}

def incrementVS() {
    echo "Incrementing version..."
    sh 'npm version major --no-git-tag-version'
    def packageJson = readJSON file: 'package.json'
    env.VERSION = packageJson.version
    def matcher = readFile('package.json') =~ /"version": "(.*?)"/
    env.VERSION = matcher[0][1]
    env.IMAGE_NAME = "${env.VERSION}-${BUILD_NUMBER}"
}

def runTests() {
    echo 'running tests...'
    sh 'npm test'
}

def buildDockerImage() {
    echo "Building docker image..."
    withCredentials([usernamePassword(credentialsId: 'dockerhubs', usernameVariable: "USER", passwordVariable: "PASS")]) {
        sh "docker build -t alexthm1/demo-app:${env.IMAGE_NAME} ."
        sh "echo \$PASS | docker login -u \$USER --password-stdin"
        sh "docker push alexthm1/demo-app:${env.IMAGE_NAME}" 
    }
}

def commitNewVersion() {
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
        // Changed single quotes to double quotes to allow variable interpolation
        sh """
            set +x
            git push https://\${TOKEN}@github.com/lupindevv/jenkins-ex.git HEAD:for-testing
            set -x
        """
    } // This closing brace was missing
} // This closing brace was missing

// Add this to make the script loadable in the pipeline
return this