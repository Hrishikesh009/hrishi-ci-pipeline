pipeline{
  agent any

  tools {
  maven 'Maven'
  jdk 'Java'
  }
  
  parameters{
    choice(name: 'ENV', choices:['dev', 'prod'], description: 'Select target environment')
  }

  stages{

    stage('Build'){
      when {
    changeset "**/src/main/java/**"
  }
     steps{
       echo 'Building project'
       sh 'mvn clean package'
     } 
    }
    
    stage('Test'){
      when {
    anyof {
      changeset "**/src/test/java/**"
      changeset "**/src/main/java/**"   // if code changed, test must run
    }
      steps{
        echo 'Testing'
        sh 'mvn test'
        junit '**/target/surefire-reports/*.xml'
      }
    }

    stage('Deploying'){
       when {
    changeset "**/target/*.jar"
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
        mkdir -p $APP_DIR
        PID=$(pgrep -f "app.jar" || true)
        if [ -n "$PID" ]; then
          echo "🛑 Stopping old PID: $PID"
          kill -9 $PID || true
          sleep 2
        fi
        cp target/*.jar $APP_DIR/app.jar
        nohup java -jar $APP_DIR/app.jar > $APP_DIR/app.log 2>&1 &
        echo "✅ Started new PID: $(pgrep -f app.jar)"
        '''
    }
  }

  stage('Health Check'){
    steps{
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

      if grep -q "Hello from Hrishi's CI/CD Pipeline!" "$LOG_FILE"; then
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
  } 

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
