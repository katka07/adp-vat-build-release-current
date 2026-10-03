def requireNonBlank(String name, String value) {
    if (value == null || value.trim().isEmpty()) {
        error("Required value '${name}' is not configured")
    }
}

pipeline {
    agent { label 'work-agent' }

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        timeout(time: 90, unit: 'MINUTES')
    }

    parameters {
        choice(name: 'ACTION', choices: ['RELEASE', 'ROLLBACK_PROD'], description: 'Normal release or deploy an older approved production version')
        string(name: 'RELEASE_VERSION', defaultValue: '', description: 'Optional. For production releases prefer a Git tag such as v2.6.4; otherwise provide 2.6.4')
        booleanParam(name: 'DEPLOY_PRODUCTION', defaultValue: false, description: 'After unified QA/SQA approval, promote and deploy the same immutable artifact to production')
        booleanParam(name: 'BUILD_DOCKER_IMAGE', defaultValue: true, description: 'Build the optional production runtime image from the qualified JAR')
        string(name: 'ROLLBACK_VERSION', defaultValue: '', description: 'Required only for ACTION=ROLLBACK_PROD; must already exist in the approved production Artifactory repository')
    }

    environment {
        MAVEN_SETTINGS = 'config/settings-jfrog.xml.example'
        JFROG_CREDENTIALS_ID = 'jfrog-vat-ci'
        DEPLOY_SSH_CREDENTIALS_ID = 'vat-deploy-ssh'
        JFROG_CLI_HOME_DIR = "${WORKSPACE}/.jfrog-ci"
    }

    stages {
        stage('Checkout') {
            when { expression { params.ACTION == 'RELEASE' } }
            steps {
                checkout scm
                sh 'git status --short && git rev-parse HEAD'
            }
        }

        stage('Resolve Release Version') {
            when { expression { params.ACTION == 'RELEASE' } }
            steps {
                script {
                    requireNonBlank('JFROG_URL', env.JFROG_URL)
                    requireNonBlank('JFROG_MAVEN_VIRTUAL_REPO', env.JFROG_MAVEN_VIRTUAL_REPO)
                    requireNonBlank('JFROG_CANDIDATE_REPO', env.JFROG_CANDIDATE_REPO)
                    requireNonBlank('JFROG_SNAPSHOT_REPO', env.JFROG_SNAPSHOT_REPO)
                    requireNonBlank('JFROG_PRODUCTION_REPO', env.JFROG_PRODUCTION_REPO)

                    String requested = params.RELEASE_VERSION == null ? '' : params.RELEASE_VERSION.trim()
                    String exactTag = sh(
                        script: "git describe --tags --exact-match HEAD 2>/dev/null || true",
                        returnStdout: true
                    ).trim()

                    if (requested) {
                        env.APP_VERSION = requested.replaceFirst('^v', '')
                    } else if (exactTag) {
                        env.APP_VERSION = exactTag.replaceFirst('^v', '')
                    } else {
                        String sha = sh(script: 'git rev-parse --short=8 HEAD', returnStdout: true).trim()
                        env.APP_VERSION = "${env.BUILD_NUMBER}-${sha}-SNAPSHOT"
                    }

                    if (params.DEPLOY_PRODUCTION && env.APP_VERSION.endsWith('-SNAPSHOT')) {
                        error('Production deployment requires a non-SNAPSHOT release version or exact Git tag')
                    }

                    env.SOURCE_REPO = env.APP_VERSION.endsWith('-SNAPSHOT') ? env.JFROG_SNAPSHOT_REPO : env.JFROG_CANDIDATE_REPO
                    env.MAVEN_VERSION_PATH = "com/example/vat/vat-processing-service/${env.APP_VERSION}"
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.APP_VERSION}"
                    echo "Build version: ${env.APP_VERSION}"
                    echo "Artifactory source repository: ${env.SOURCE_REPO}"
                }
            }
        }

        stage('Build, Maven Tests and Deploy Candidate') {
            when { expression { params.ACTION == 'RELEASE' } }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.JFROG_CREDENTIALS_ID,
                    usernameVariable: 'JFROG_USERNAME',
                    passwordVariable: 'JFROG_TOKEN'
                )]) {
                    sh '''#!/bin/bash
                        set -euo pipefail
                        mvn -B -U -s "$MAVEN_SETTINGS" \
                          -Drevision="$APP_VERSION" \
                          clean deploy
                        sha256sum "target/vat-processing-service-${APP_VERSION}.jar" | tee artifact.sha256
                    '''
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, fingerprint: true, artifacts: 'target/*.jar,artifact.sha256'
                }
            }
        }

        stage('SonarQube Quality Gate') {
            when { expression { params.ACTION == 'RELEASE' } }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.JFROG_CREDENTIALS_ID,
                    usernameVariable: 'JFROG_USERNAME',
                    passwordVariable: 'JFROG_TOKEN'
                )]) {
                    withSonarQubeEnv('sonarqube') {
                        sh '''#!/bin/bash
                            set -euo pipefail
                            mvn -B -s "$MAVEN_SETTINGS" \
                              -Drevision="$APP_VERSION" \
                              -DskipTests \
                              sonar:sonar
                        '''
                    }
                }
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Optional Production Docker Image') {
            when {
                allOf {
                    expression { params.ACTION == 'RELEASE' }
                    expression { params.BUILD_DOCKER_IMAGE }
                }
            }
            steps {
                sh '''#!/bin/bash
                    set -euo pipefail
                    docker build \
                      --build-arg JAR_FILE="target/vat-processing-service-${APP_VERSION}.jar" \
                      --build-arg APP_VERSION="$APP_VERSION" \
                      --build-arg VCS_REF="$(git rev-parse HEAD)" \
                      --build-arg BUILD_DATE="$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
                      -t "vat-processing-service:${APP_VERSION}" .
                    docker run --rm "vat-processing-service:${APP_VERSION}" version
                '''
            }
        }

        stage('Deploy Candidate to QA') {
            when { expression { params.ACTION == 'RELEASE' } }
            steps {
                script {
                    env.ARTIFACT_SHA256 = sh(
                        script: "awk '{print \$1}' artifact.sha256",
                        returnStdout: true
                    ).trim()
                }
                withCredentials([usernamePassword(
                    credentialsId: env.JFROG_CREDENTIALS_ID,
                    usernameVariable: 'JFROG_USERNAME',
                    passwordVariable: 'JFROG_TOKEN'
                )]) {
                    sshagent(credentials: [env.DEPLOY_SSH_CREDENTIALS_ID]) {
                        sh '''#!/bin/bash
                            set -euo pipefail
                            ansible-playbook \
                              -i ansible/inventory/qa.ini \
                              ansible/deploy.yml \
                              -e "release_version=${APP_VERSION}" \
                              -e "jfrog_repo=${SOURCE_REPO}"
                            ansible-playbook \
                              -i ansible/inventory/qa.ini \
                              ansible/verify.yml
                        '''
                    }
                }
            }
        }

        stage('QA / SQA Qualification and Approval') {
            when { expression { params.ACTION == 'RELEASE' } }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.JFROG_CREDENTIALS_ID,
                    usernameVariable: 'JFROG_USERNAME',
                    passwordVariable: 'JFROG_TOKEN'
                )]) {
                    sh '''#!/bin/bash
                        set -euo pipefail
                        export JFROG_QA_SOURCE_REPO="$SOURCE_REPO"
                        mvn -B -U -s "$MAVEN_SETTINGS" \
                          -f qa-sqa-tests/pom.xml \
                          -Dvat.version="$APP_VERSION" \
                          clean verify
                    '''
                }
                script {
                    if (params.DEPLOY_PRODUCTION) {
                        timeout(time: 24, unit: 'HOURS') {
                            input message: "QA/SQA approved VAT ${env.APP_VERSION} for production?", ok: 'Promote exact artifact'
                        }
                    } else {
                        echo 'QA/SQA automated qualification completed. Production promotion was not requested.'
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'qa-sqa-tests/target/failsafe-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'qa-sqa-tests/target/qa-vat-return.json'
                }
            }
        }

        stage('Promote Exact Artifact in JFrog') {
            when {
                allOf {
                    expression { params.ACTION == 'RELEASE' }
                    expression { params.DEPLOY_PRODUCTION }
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.JFROG_CREDENTIALS_ID,
                    usernameVariable: 'JFROG_USERNAME',
                    passwordVariable: 'JFROG_TOKEN'
                )]) {
                    sh '''#!/bin/bash
                        set -euo pipefail
                        rm -rf "$JFROG_CLI_HOME_DIR"
                        jf config add vat-ci \
                          --url="$JFROG_URL" \
                          --access-token="$JFROG_TOKEN" \
                          --interactive=false
                        jf rt cp \
                          "${JFROG_CANDIDATE_REPO}/${MAVEN_VERSION_PATH}/" \
                          "${JFROG_PRODUCTION_REPO}/${MAVEN_VERSION_PATH}/" \
                          --server-id=vat-ci
                        jf rt search \
                          "${JFROG_PRODUCTION_REPO}/${MAVEN_VERSION_PATH}/vat-processing-service-${APP_VERSION}.jar" \
                          --server-id=vat-ci
                    '''
                }
            }
        }

        stage('Deploy Production') {
            when {
                allOf {
                    expression { params.ACTION == 'RELEASE' }
                    expression { params.DEPLOY_PRODUCTION }
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.JFROG_CREDENTIALS_ID,
                    usernameVariable: 'JFROG_USERNAME',
                    passwordVariable: 'JFROG_TOKEN'
                )]) {
                    sshagent(credentials: [env.DEPLOY_SSH_CREDENTIALS_ID]) {
                        script {
                            try {
                                sh '''#!/bin/bash
                                    set -euo pipefail
                                    ansible-playbook \
                                      -i ansible/inventory/prod.ini \
                                      ansible/deploy.yml \
                                      -e "release_version=${APP_VERSION}" \
                                      -e "jfrog_repo=${JFROG_PRODUCTION_REPO}"
                                    ansible-playbook \
                                      -i ansible/inventory/prod.ini \
                                      ansible/verify.yml
                                '''
                            } catch (err) {
                                echo 'Production verification failed; restoring the immediately previous deployed release.'
                                sh '''#!/bin/bash
                                    set -euo pipefail
                                    ansible-playbook \
                                      -i ansible/inventory/prod.ini \
                                      ansible/rollback-previous.yml
                                    ansible-playbook \
                                      -i ansible/inventory/prod.ini \
                                      ansible/verify.yml
                                '''
                                throw err
                            }
                        }
                    }
                }
            }
        }

        stage('Rollback Approved Production Version') {
            when { expression { params.ACTION == 'ROLLBACK_PROD' } }
            steps {
                script {
                    requireNonBlank('ROLLBACK_VERSION', params.ROLLBACK_VERSION)
                    requireNonBlank('JFROG_URL', env.JFROG_URL)
                    requireNonBlank('JFROG_PRODUCTION_REPO', env.JFROG_PRODUCTION_REPO)
                }
                input message: "Roll production back to approved version ${params.ROLLBACK_VERSION}?", ok: 'Rollback'
                withCredentials([usernamePassword(
                    credentialsId: env.JFROG_CREDENTIALS_ID,
                    usernameVariable: 'JFROG_USERNAME',
                    passwordVariable: 'JFROG_TOKEN'
                )]) {
                    sshagent(credentials: [env.DEPLOY_SSH_CREDENTIALS_ID]) {
                        sh '''#!/bin/bash
                            set -euo pipefail
                            ansible-playbook \
                              -i ansible/inventory/prod.ini \
                              ansible/rollback.yml \
                              -e "rollback_version=${ROLLBACK_VERSION}"
                            ansible-playbook \
                              -i ansible/inventory/prod.ini \
                              ansible/verify.yml
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }
}
