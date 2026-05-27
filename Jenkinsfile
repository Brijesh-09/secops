pipeline {
    agent any
 
    tools {
        maven 'Maven-3.9'   // configured in Jenkins → Tools
    }
 
    environment {
        // ── JFrog Cloud credentials (stored in Jenkins credentials) ──
        JFROG_USER      = credentials('jfrog-user')
        JFROG_APIKEY    = credentials('jfrog-apikey')
 
        // ── SonarCloud credentials ──
        SONAR_TOKEN     = credentials('sonar-token')
 
        // ── SonarCloud project details — update these with YOUR values ──
        SONAR_ORG       = 'brijesh-secops'          // e.g. brijesh-secops
        SONAR_PROJECT   = 'brijesh-secops'  // e.g. brijesh-secops_my-app
 
        // ── JFrog Cloud URL — update with YOUR instance ──
        JFROG_URL       = 'https://trialxcztq9.jfrog.io/artifactory/api/maven/secops-pipeline-libs-snapshot-local''
 
        // ── Auto version using Jenkins build number ──
        APP_VERSION     = "1.0.${BUILD_NUMBER}"
    }
 
    stages {
 
        // ─────────────────────────────────────────────────
        // STAGE 1: Checkout
        // Pull the latest code from your Git repo
        // ─────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "📥 Checking out source code (Build #${BUILD_NUMBER})"
                checkout scm
            }
        }
 
        // ─────────────────────────────────────────────────
        // STAGE 2: Build & Test
        // Compile code, run JUnit tests, generate JaCoCo
        // coverage report — SonarCloud reads this later
        // Dependencies are pulled from JFrog Cloud
        // ─────────────────────────────────────────────────
        stage('Build & Test') {
            steps {
                echo '🔨 Building and running tests...'
                sh """
                    mvn clean test \
                        -s settings.xml \
                        -Djfrog.url=${JFROG_URL} \
                        -Djfrog.user=${JFROG_USER} \
                        -Djfrog.apikey=${JFROG_APIKEY} \
                        -Dproject.version=${APP_VERSION}
                """
            }
            post {
                always {
                    // Publish JUnit test results in Jenkins UI
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo '❌ Tests failed. Fix them before proceeding.'
                }
            }
        }
 
        // ─────────────────────────────────────────────────
        // STAGE 3: SonarCloud Analysis
        // FIX: wrapped inside withSonarQubeEnv() so that
        // the Quality Gate stage can track this analysis.
        // The name 'SonarCloud' must EXACTLY match what you
        // set in Jenkins → Manage Jenkins → System →
        // SonarQube servers → Name field
        // ─────────────────────────────────────────────────
        stage('SonarCloud Analysis') {
    steps {

        echo '🔍 Running SonarCloud code analysis...'

        withSonarQubeEnv('SonarCloud') {

            sh '''
            mvn sonar:sonar \
                -s settings.xml \
                -Dsonar.projectKey=$SONAR_PROJECT \
                -Dsonar.organization=$SONAR_ORG \
                -Dsonar.host.url=https://sonarcloud.io \
                -Dsonar.token=$SONAR_TOKEN \
                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
            '''
        }
    }
}
 
        // ─────────────────────────────────────────────────
        // STAGE 4: Quality Gate
        // Waits for SonarCloud to finish its analysis and
        // return a PASSED or FAILED result via webhook.
        // Pipeline STOPS here if code quality is too low.
        // This is the "Sec" in DevSecOps.
        //
        // REQUIREMENT: You must set up a webhook in
        // SonarCloud → Project → Administration → Webhooks
        // URL: http://<minikube-ip>:30080/sonarqube-webhook/
        // ─────────────────────────────────────────────────
        stage('Quality Gate') {
            steps {
                echo '🚦 Waiting for SonarCloud Quality Gate result...'
                timeout(time: 5, unit: 'MINUTES') {
                    // abortPipeline: true means if quality gate FAILS
                    // the pipeline stops and does NOT deploy to JFrog
                    waitForQualityGate abortPipeline: true
                }
            }
        }
 
        // ─────────────────────────────────────────────────
        // STAGE 5: Package
        // Create the final .jar artifact
        // Archived in Jenkins UI so you can download it
        // ─────────────────────────────────────────────────
        stage('Package') {
            steps {
                echo '📦 Packaging the application...'
                sh """
                    mvn package \
                        -DskipTests \
                        -s settings.xml \
                        -Djfrog.url=${JFROG_URL} \
                        -Djfrog.user=${JFROG_USER} \
                        -Djfrog.apikey=${JFROG_APIKEY} \
                        -Dproject.version=${APP_VERSION}
                """
                // Archive the .jar so it shows up in Jenkins UI
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
 
        // ─────────────────────────────────────────────────
        // STAGE 6: Deploy to JFrog Cloud
        // Push the built .jar to JFrog Artifactory
        // Makes it available for other teams/services
        // ─────────────────────────────────────────────────
        stage('Deploy to JFrog') {
            steps {
                echo "🚀 Pushing artifact v${APP_VERSION} to JFrog Cloud..."
                sh """
                    mvn deploy \
                        -DskipTests \
                        -s settings.xml \
                        -Djfrog.url=${JFROG_URL} \
                        -Djfrog.user=${JFROG_USER} \
                        -Djfrog.apikey=${JFROG_APIKEY} \
                        -Dproject.version=${APP_VERSION}
                """
            }
        }
    }
 
    // ─────────────────────────────────────────────────────
    // POST: Runs after all stages complete
    // ─────────────────────────────────────────────────────
    post {
        success {
            echo """
            ✅ Pipeline SUCCESS
            ───────────────────────────────
            Build     : #${BUILD_NUMBER}
            Version   : ${APP_VERSION}
            Artifact  : my-app-${APP_VERSION}.jar
            JFrog URL : ${JFROG_URL}/libs-snapshot-local/
            SonarCloud: https://sonarcloud.io/project/overview?id=${SONAR_PROJECT}
            ───────────────────────────────
            """
        }
        failure {
            echo """
            ❌ Pipeline FAILED
            Check console output above for details.
            """
        }
        always {
            echo "Build #${BUILD_NUMBER} completed — Status: ${currentBuild.currentResult}"
            // FIX: replaced cleanWs() with deleteDir()
            // cleanWs() requires the 'Workspace Cleanup' plugin
            // deleteDir() is built into Jenkins — no plugin needed
            deleteDir()
        }
    }
}