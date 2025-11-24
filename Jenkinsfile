pipeline {
  agent any

  tools {
    maven 'Maven'
    jdk 'Java'
  }

  parameters {
    choice(name: 'ENV', choices: ['dev', 'prod'], description: 'Select target environment')
  }

  environment {
    // default value; useful for downstream script logic
    BUILD_JAR = 'false'
  }

  stages {

    stage('Build') {
      when {
        changeset "**/src/main/java/**"
      }
      steps {
        echo 'Building project'
        sh 'mvn clean package'
        // If the build succeeded, mark that we have a jar to deploy.
        // We set this flag so Deploy/Health Check will run only when Build created artifacts.
        script {
          // If a jar exists under target, set BUILD_JAR to "true"
          def rc = sh(script: 'if ls target/*.jar >/dev/null 2>&1; then echo true; else echo false; fi', returnStdout: true).trim()
          env.BUILD_JAR = rc
          echo "BUILD_JAR=${env.BUILD_JAR}"
        }
      }
    }

    stage('Test') {
      when {
        anyOf {
          changeset "**/src/test/java/**"
          changeset "**/src/main/java/**"
        }
      }
      steps {
        echo 'Testing'
        sh 'mvn test'
        junit '**/target/surefire-reports/*.xml'
      }
    }

    stage('Deploying') {
      // Deploy only when BUILD_JAR was set to true by the Build stage (i.e. a jar exists)
      when {
        expression { return env.BUILD_JAR == 'true' }
      }
      steps {
        echo "Deploying to environment: ${params.ENV}"
        sh '''
        set -e

        if [ "$ENV" = "dev" ]; then
          APP_DIR="/opt/devapp"
        else
          APP_DIR="/opt/prodapp"
        fi

        mkdir -p "$APP_DIR"
        PID=$(pgrep -f "app.jar" || true)
        if [ -n "$PID" ]; then
          echo "🛑 Stopping old PID: $PID"
          kill -9 $PID || true
          sleep 2
        fi

        cp target/*.jar "$APP_DIR/app.jar"
        nohup java -jar "$APP_DIR/app.jar" > "$APP_DIR/app.log" 2>&1 &
        echo "✅ Started new PID: $(pgrep -f app.jar)"
        '''
      }
    }

    stage('Health Check') {
      when {
        expression { return env.BUILD_JAR == 'true' }
      }
      steps {
        echo "Running Health Check for ${params.ENV} environment"
        sh '''
        set -e

        if [ "$ENV" = "dev" ]; then
          APP_DIR="/opt/devapp"
        else
          APP_DIR="/opt/prodapp"
        fi

        LOG_FILE="$APP_DIR/app.log"
        echo "Checking log file at: $LOG_FILE"

        if [ ! -f "$LOG_FILE" ]; then
          echo "ERROR: Log file not found!"
          exit 1
        fi

        if grep -q "Hello from Hrishi new CI/CD Pipeline!" "$LOG_FILE"; then
          echo "Health Check Passed"
        else
          echo "Failed"
          echo "--------log contains-------"
          cat "$LOG_FILE"
          exit 1
        fi
        '''
      }
    }

  } // end stages

  post {
    success {
      echo 'Build + Deploy Successful!'
      archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
      cleanWs()
    }
    failure {
      echo 'Build Failed!'
    }
  }
}
