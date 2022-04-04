def call (repo) {
    pipeline {
        agent any
        stages {
            stage('Build') {
                steps {
                    sh "pip install -r requirements.txt"
                }
            }
            stage('Python Lint') {
                steps {
                    sh "pylint-fail-under --fail_under 5.0  *.py "
                }
            }
            stage('Package') {
                steps {
                    withCredentials([string(credentialsId: 'DockerHub', variable: 'TOKEN')]) {
                    sh "docker login -u cherylk19 -p '$TOKEN' docker.io"
                    sh "docker build -t cherylk19/${repo}:latest ."
                    sh "docker push cherylk19/${repo}:latest"
                    }
                }
            }
            stage('Check Container running') {
                steps {
                    sshagent (credentials: ['cheryl-vm']) {
                        sh "ssh -o StrictHostKeyChecking=no azureuser@acit3855-kafka.eastus.cloudapp.azure.com 'cd ~/lab8/deployment && \
                            (docker image inspect -f cherylk19/${repo}:latest || echo cherylk19/${repo}:latest not exist) && \
                            docker-compose ps | grep ${repo} && docker-compose stop ${repo} || echo ${repo} not running'"
                    }
                }
            }
            stage('Deploy') {
                steps {
                    sshagent (credentials: ['cheryl-vm']) {
                        withCredentials([string(credentialsId: 'DockerHub', variable: 'TOKEN')]) {
                            sh "ssh -o StrictHostKeyChecking=no azureuser@acit3855-kafka.eastus.cloudapp.azure.com 'docker login -u cherylk19 -p '$TOKEN' docker.io && \
                            cd ~/lab8/deployment && docker pull cherylk19/${repo}:latest && docker-compose up -d'"
                        }
                    }
                }
            }
        }
    }
}
