node {
    stage('checkout') {
        checkout scm
    }

    stage('build') {
        sh './gradlew incrementCiVersion clean build'
    }

    stage('publish') {
      sh './gradlew publish'
    }

    stage('archive') {
        archive 'build/libs/*.jar'
    }
}
