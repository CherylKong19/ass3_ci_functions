def call (repo) {
    pipeline {
        agent any
//         parameters {
//         booleanParam(defaultValue: false, description:'Deploy the App', name: 'DEPLOY')
//         }
        stages {
            stage('Build') {
                steps {
                    sh "pip install -r requirements.txt"
                }
            }
            // stage('Python Lint') {
            //     steps {
            //         sh "pylint-fail-under --fail_under 5.0  ${directory}/*.py "
            //     }
            // }
            stage('Python Lint') {
                steps {
                    sh "pylint-fail-under --fail_under 5.0  *.py "
                }
            }
            stage('Package') {
//                 when {
//                      expression { env.GIT_BRANCH == 'main' }
//                     branch 'main'
//                 }
                steps {
                    withCredentials([string(credentialsId: 'DockerHub', variable: 'TOKEN')]) {
                    sh "docker login -u cherylk19 -p '$TOKEN' docker.io"
                    sh "docker build -t cherylk19/${repo}:latest ."
                    sh "docker push cherylk19/${repo}:latest"
                    }
                }
            }
            stage('Deploy') {
//                 when {
//                     expression { params.DEPLOY }
//                 }
                steps {
                    sshagent (credentials: ['cheryl-vm']) {
                        withCredentials([string(credentialsId: 'DockerHub', variable: 'TOKEN')]) {
                            sh "ssh -o StrictHostKeyChecking=no azureuser@acit3855-kafka.eastus.cloudapp.azure.com 'docker login -u cherylk19 -p '$TOKEN' docker.io && \
                            cd ~/lab8/deployment && docker-compose stop ${repo} && docker container prune -f && docker pull cherylk19/${repo}:latest && docker-compose up -d'"
                        }
                    }
                }
            }
        }
    }
}
