pipeline{
  agent any

  parameters{
    choice(name: 'ENV', choices:['dev', 'prod'], description: 'Target environment')
  }

  stages{

    stage('Build'){
     step{
       echo 'Building project'
       sh 'mvn clean package'
     } 
    }
    
    stage('Test'){
      step{
        echo 'Testing'
        sh 'mvn test'
        junit '**/target/surefire-reports/*.xml'
      }
    }

    stage('Deploy'){
      step{
        echo 'Deploying'
         sh '''
        set -e
        APP_DIR="/opt/ciapp"
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
