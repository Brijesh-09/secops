pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {
        // ── Jenkins stored credentials ──
        JFROG_USER   = credentials('jfrog-user')
        JFROG_APIKEY = credentials('jfrog-apikey')
        SONAR_TOKEN  = credentials('sonar-token')

        // ── SonarCloud project details ──
        SONAR_ORG     = 'brijesh-secops'
        SONAR_PROJECT = 'brijesh-secops'

        // ── JFrog Cloud base URL ──
        JFROG_URL = 'https://trialxcztq9.jfrog.io/artifactory'

        // ── Auto version per build ──
        APP_VERSION = "1.0.${BUILD_NUMBER}"
    }

    stages {

        // =====================================================
        // STAGE 1: CHECKOUT
        // Pull latest code from GitHub
        // =====================================================
        stage('Checkout') {
            steps {
                echo "📥 Checking out source code (Build #${BUILD_NUMBER})"
                checkout scm
            }
        }

        // =====================================================
        // STAGE 2: BUILD & TEST
        // - Compiles the Java code
        // - Runs JUnit 5 tests
        // - Generates JaCoCo coverage report
        // - Pulls dependencies from JFrog Cloud
        // =====================================================
        stage('Build & Test') {
            steps {
                echo '🔨 Building and running tests...'
                sh """
                    mvn clean test \
                        -s settings.xml \
                        -Djfrog.user=${JFROG_USER} \
                        -Djfrog.apikey=${JFROG_APIKEY}
                """
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo '❌ Tests failed. Fix them before proceeding.'
                }
            }
        }

        // =====================================================
        // STAGE 3: SONARCLOUD ANALYSIS
        // - Sends code + JaCoCo coverage to SonarCloud
        // - withSonarQubeEnv() registers the analysis so
        //   the Quality Gate stage can track it
        // =====================================================
        stage('SonarCloud Analysis') {
            steps {
                echo '🔍 Running SonarCloud analysis...'
                withSonarQubeEnv('SonarCloud') {
                    sh """
                        mvn sonar:sonar \
                            -s settings.xml \
                            -Djfrog.user=${JFROG_USER} \
                            -Djfrog.apikey=${JFROG_APIKEY} \
                            -Dsonar.projectKey=${SONAR_PROJECT} \
                            -Dsonar.organization=${SONAR_ORG} \
                            -Dsonar.host.url=https://sonarcloud.io \
                            -Dsonar.token=${SONAR_TOKEN} \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
        }

        // =====================================================
        // STAGE 4: QUALITY GATE
        // - Waits for SonarCloud to return PASS or FAIL
        // - Pipeline STOPS if quality is below threshold
        // - This is the "Sec" part of DevSecOps
        // =====================================================
        stage('Quality Gate') {
            steps {
                echo '🚦 Waiting for SonarCloud Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // =====================================================
        // STAGE 5: PACKAGE
        // - Builds the final .jar artifact
        // - Archives it in Jenkins UI for download
        // =====================================================
        stage('Package') {
            steps {
                echo '📦 Packaging application...'
                sh """
                    mvn package \
                        -DskipTests \
                        -s settings.xml \
                        -Djfrog.user=${JFROG_USER} \
                        -Djfrog.apikey=${JFROG_APIKEY}
                """
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        // =====================================================
        // STAGE 6: DEPLOY TO JFROG
        // - Pushes the .jar to JFrog Cloud Artifactory
        // - Since version is SNAPSHOT, goes to:
        //   secops-pipeline-libs-snapshot-local
        // =====================================================
        stage('Deploy to JFrog') {
            steps {
                echo "🚀 Deploying artifact v${APP_VERSION} to JFrog..."
                sh """
                    mvn deploy \
                        -DskipTests \
                        -s settings.xml \
                        -Djfrog.user=${JFROG_USER} \
                        -Djfrog.apikey=${JFROG_APIKEY}
                """
            }
        }
    }

    // =====================================================
    // POST ACTIONS
    // =====================================================
    post {
        success {
            echo """
            ✅ PIPELINE SUCCESS
            ─────────────────────────────────────
            Build      : #${BUILD_NUMBER}
            Version    : ${APP_VERSION}
            SonarCloud : https://sonarcloud.io/project/overview?id=${SONAR_PROJECT}
            JFrog      : ${JFROG_URL}/secops-pipeline-libs-snapshot-local/
            ─────────────────────────────────────
            """
        }
        failure {
            echo """
            ❌ PIPELINE FAILED
            Check Jenkins console logs for details.
            """
        }
        always {
            echo "Build #${BUILD_NUMBER} completed — Status: ${currentBuild.currentResult}"
            // deleteDir() is built-in — no plugin needed
            deleteDir()
        }
    }
}