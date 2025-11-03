pipeline{
  agent any

  tools {
  maven 'Maven'
  jdk 'Java'
  }
  
  parameters{
    choice(name: 'ENV', choices:['dev', 'prod'], description: 'Target environment')
  }

  stages{

    stage('Build'){
     steps{
       echo 'Building project'
       sh 'mvn clean package'
     } 
    }
    
    stage('Test'){
      steps{
        echo 'Testing'
        sh 'mvn test'
        junit '**/target/surefire-reports/*.xml'
      }
    }

    stage('Deploying'){
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
