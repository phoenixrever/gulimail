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
      }
    }

//     注意 -o 是离线模式
    stage('build & push') {
      steps {
        container('maven') {
          sh 'mvn -Dmaven.test.skip=true -gs `pwd`/mvn-setting.xml clean package'
          sh 'cd $PROJECT_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
          withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
            sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
            sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
          }
        }
      }
    }

// 推送最新镜像
    stage('push latest') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
          sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
        }

      }
    }

//发布好了 更新github的tag
    stage('push with tag'){
      when{
        expression{
          return params.PROJECT_VERSION =~ /v.*/
        }
      }
      steps {
          container ('maven') {
            input(id: 'release-image-with-tag', message: 'release image with tag?')
              withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                sh 'git config --global user.email "phoenixrever@gmail.com" '
                sh 'git config --global user.name "phoenixrever" '
                sh 'git tag -a $PROJECT_VERSION -m "$PROJECT_VERSION" '
                sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GITHUB_ACCOUNT/gulimall.git --tags --ipv4'
              }
            sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
            sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
         }
      }
    }

//     configs deploy/** 里面所有部署文件
    stage('deploy to prod') {
      steps {
        input(id: 'deploy-to-prod $PROJECT_NAME', message: 'deploy to prod?')
        kubernetesDeploy(configs: 'deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
      }
    }
  }
}