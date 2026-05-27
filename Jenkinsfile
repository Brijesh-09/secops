// ============================================================
//  DevSecOps Pipeline
//  Tools: Jenkins (Minikube) + SonarCloud + JFrog Cloud
// ============================================================

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

        // ── SonarCloud project details (update these) ──
        SONAR_ORG       = 'brijesh-secops'          // your SonarCloud org key
        SONAR_PROJECT   = 'brijesh-secops'  // your SonarCloud project key

        // ── JFrog Cloud repo URL (update with your instance) ──
        JFROG_URL       = 'https://trialxcztq9.jfrog.io/artifactory/api/maven/libs-release-local-libs-snapshot'

        // ── Auto version using build number ──
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
        // coverage report (SonarCloud reads this later)
        // Dependencies pulled from JFrog Cloud
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
                    // Show test results in Jenkins UI
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo '❌ Tests failed. Fix them before proceeding.'
                }
            }
        }

        // ─────────────────────────────────────────────────
        // STAGE 3: SonarCloud Analysis
        // Sends code + coverage data to SonarCloud
        // SonarCloud runs the scan on their servers
        // ─────────────────────────────────────────────────
        stage('SonarCloud Analysis') {
            steps {
                echo '🔍 Running SonarCloud code analysis...'
                sh """
                    mvn sonar:sonar \
                        -s settings.xml \
                        -Dsonar.projectKey=${SONAR_PROJECT} \
                        -Dsonar.organization=${SONAR_ORG} \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.login=${SONAR_TOKEN} \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                """
            }
        }

        // ─────────────────────────────────────────────────
        // STAGE 4: Quality Gate
        // Waits for SonarCloud to finish analysis
        // Pipeline STOPS here if code quality is too low
        // This is the "Sec" in DevSecOps
        // ─────────────────────────────────────────────────
        stage('Quality Gate') {
            steps {
                echo '🚦 Waiting for SonarCloud Quality Gate result...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ─────────────────────────────────────────────────
        // STAGE 5: Package
        // Create the final .jar artifact
        // Archive it in Jenkins so you can download it
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
                // Archive artifact in Jenkins UI
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        // ─────────────────────────────────────────────────
        // STAGE 6: Deploy to JFrog Cloud
        // Push the .jar to your JFrog Cloud repository
        // This makes it available for other teams to use
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
    // POST: Notifications after pipeline finishes
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
            ❌ Pipeline FAILED at stage: ${env.STAGE_NAME}
            Check console output above for details.
            """
        }
        always {
            echo "Build #${BUILD_NUMBER} completed — Status: ${currentBuild.currentResult}"
            // Clean workspace to save disk space on Minikube
            cleanWs()
        }
    }
}
