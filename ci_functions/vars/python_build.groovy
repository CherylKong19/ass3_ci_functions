def call (repo) {
    pipeline {
        agent any
        parameters {
        booleanParam(defaultValue: false, description:'Deploy the App', name: 'DEPLOY')
        }
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
                    sh "docker login -u '<username>' -p '$TOKEN' docker.io"
                    sh "docker build -t cherylk19/${repo}:latest ."
                    sh "docker push cherylk19/${repo}:latest"
                    }
                }
            }
            stage('Deploy') {
                when {
                    expression { params.DEPLOY }
                }
                steps {
                    dir("lab8/deployment") {
                        withCredentials([string(credentialsId: 'DockerHub', variable: 'TOKEN')]) {
                        sh "docker login -u cherylk19 -p '$TOKEN' docker.io"
                        sh "docker-compose stop ${repo}"
                        sh "docker prune"
                        sh "docker pull cherylk19/${repo}:latest"
                        sh "docker-compose up -d"
                        }
                    }
                }
            }
        }
    }
}
