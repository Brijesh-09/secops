pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {

        // Jenkins Credentials
        JFROG_USER   = credentials('jfrog-user')
        JFROG_APIKEY = credentials('jfrog-apikey')

        SONAR_TOKEN  = credentials('sonar-token')

        // SonarCloud
        SONAR_ORG    = 'brijesh-secops'
        SONAR_PROJECT = 'brijesh-secops'

        // JFrog Base URL
        JFROG_URL = 'https://trialxcztq9.jfrog.io/artifactory'

        // Build Version
        APP_VERSION = "1.0.${BUILD_NUMBER}"
    }

    stages {

        // =====================================================
        // CHECKOUT
        // =====================================================
        stage('Checkout') {
            steps {

                echo "📥 Checking out source code (Build #${BUILD_NUMBER})"

                checkout scm
            }
        }

        // =====================================================
        // BUILD + TEST
        // =====================================================
        stage('Build & Test') {
            steps {

                echo '🔨 Building and running tests...'

                sh '''
                mvn clean test \
                    -s settings.xml
                '''
            }

            post {

                always {
                    junit '**/target/surefire-reports/*.xml'
                }

                failure {
                    echo '❌ Tests failed.'
                }
            }
        }

        // =====================================================
        // SONARCLOUD ANALYSIS
        // =====================================================
        stage('SonarCloud Analysis') {

            steps {

                echo '🔍 Running SonarCloud analysis...'

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

        // =====================================================
        // QUALITY GATE
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
        // PACKAGE
        // =====================================================
        stage('Package') {

            steps {

                echo '📦 Packaging application...'

                sh '''
                mvn package \
                    -DskipTests \
                    -s settings.xml
                '''

                archiveArtifacts artifacts: 'target/*.jar',
                                 fingerprint: true
            }
        }

        // =====================================================
        // DEPLOY TO JFROG
        // =====================================================
        stage('Deploy to JFrog') {

            steps {

                echo "🚀 Deploying artifact to JFrog..."

                sh '''
                mvn deploy \
                    -DskipTests \
                    -s settings.xml
                '''
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

            Build Number : #${BUILD_NUMBER}
            Version      : ${APP_VERSION}

            SonarCloud:
            https://sonarcloud.io/project/overview?id=${SONAR_PROJECT}

            JFrog:
            ${JFROG_URL}

            """
        }

        failure {

            echo """
            ❌ PIPELINE FAILED

            Check Jenkins console logs for details.
            """
        }

        always {

            echo "Build #${BUILD_NUMBER} completed with status: ${currentBuild.currentResult}"

            deleteDir()
        }
    }
}