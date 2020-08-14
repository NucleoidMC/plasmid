node {
    stage('checkout') {
        checkout scm
    }

    stage('incrementVersion') {
        sh './gradlew incrementCiVersion'
    }

    stage('build') {
        sh './gradlew clean build publish'
    }

    stage('archive') {
        archive 'build/libs/*.jar'
    }
}
