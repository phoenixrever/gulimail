pipeline {
  agent {
    node {
      label 'maven'
    }

  }

  parameters {
    string(name:'PROJECT_VERSION',defaultValue: 'v0.0Beta',description:'')
    string(name:'PROJECT_NAME',defaultValue: '',description:'')
  }
  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'phoenixhell'
    GITHUB_ACCOUNT = 'phoenixrever'
    BRANCH_NAME = 'master'
  }
  stages {
    stage('pull code') {
      steps {
        git(credentialsId: 'github-id', url: 'https://github.com/phoenixrever/gulimall.git', branch: 'master', changelog: true, poll: false)
        sh 'echo 正在构建==>$PROJECT_NAME 版本号==>$PROJECT_VERSION'
//         container ('maven') {
//             sh 'mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml'
//         }
      }
    }

    stage('build & push') {
      steps {
        container('maven') {
          sh 'mvn -o -Dmaven.test.skip=true -gs `pwd`/configuration/settings.xml clean package'
          sh 'docker build -f $PROJECT_NAME/Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
          withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
            sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
            sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
          }
        }
      }
    }

    stage('push latest') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
          sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
        }

      }
    }

    stage('deploy to dev') {
      steps {
        input(id: 'deploy-to-dev', message: 'deploy to dev?')
        kubernetesDeploy(configs: 'deploy/dev-ol/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
      }
    }

    stage('deploy to production') {
      steps {
        input(id: 'deploy-to-production', message: 'deploy to production?')
        kubernetesDeploy(configs: 'deploy/prod-ol/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
      }
    }

  }
}