node('master') {
	
		
		
		// Set a particular maven install to use for this build
		env.JAVA_HOME="${tool 'jdk1.8.0_72'}"
		env.PATH="${tool 'maven3'}/bin:${env.PATH}"
		
		def newVersion = getSourceCode()
		
		developmentStage(newVersion)
	
		//copyToArtifactRepo()
		
		//stagingStage()
		
	}
	
	//checkpointandinputs()
	
	//productionStage()
	
	def getSourceCode(){
		// Checkout my source
		git url: 'https://github.com/dcarbajosa/SpringHibernateExample.git', branch: 'master'
		
		def v = getVersion()
		if (v) {
			echo "Building version ${v}"
		}
		def BUILD_TYPE = "-SNAPSHOT"
		def newVersion = "${v}-${env.BUILD_NUMBER}"
		
		sh "mvn build-helper:parse-version versions:set -DnewVersion=${newVersion}${BUILD_TYPE}"
		
		return newVersion
	}
	
	def developmentStage(newVersion){
		// Start 'Dev' stage
		stage name: 'Dev', concurrency: 1
		
		// Execute maven build
		sh "mvn -B clean deploy "
		
		sh "git remote set-url origin git@github.com:dcarbajosa/SpringHibernateExample.git"
		
		// create a build tag on release branch
		sh "git tag -a ${newVersion} -m 'Snapshot for Jenkins Scripts Version ${newVersion}'"
	
		// Archive the artifacts from the above build
		step([$class: 'ArtifactArchiver', artifacts: '**/target/*.war', fingerprint: true])
		step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
		
		// push this tag
		sh "git push origin --tags"
		
		echo "${pwd()}"
	}
	
	
	
	def copyToArtifactRepo(){
		
		sh 'mvn -B deploy'
		
	}
	
	def stagingStage(){
		// Start a new stage and make sure only one build can enter this stage
		stage name: 'Staging', concurrency: 1
	
		// Call deploy function defined below
	//    deploy 'target/x.war', 'staging'
	}
	
	def checkpointandinputs(){
		
		// Pause this workflow for human interaction
		input message: "Does http://localhost:8080/staging/ look good?"
		
		// Start a try catch block
		try {
			/**
			 * Assuming significant time was spent up until this point, establish a
			 * safe point so this build can be run from here if this build fails after
			 * this.
			 */
			checkpoint('Before production')
		} catch (NoSuchMethodError _) {
			// Since Checkpoints is a feature of CloudBees jenkins enterprise, log it
			// and continue with the rest of the workflow
			echo 'Checkpoint feature available in Jenkins Enterprise by CloudBees.'
		}
	}
	
	def productionStage(){
		stage name: 'Production', concurrency: 1
		
		// Run this part of the job on a node with name or label of 'windows-32bit'
		//node('windows-32bit') {
		
			// Exceute a command
		//    sh 'wget -O - -S http://localhost:8080/staging/'
		//    unarchive mapping: ['target/x.war' : 'x.war']
		//    deploy 'x.war', 'production'
		//    echo 'Deployed to http://localhost:8080/production/'
		//}
	}
	
	
	
	
	def getVersion() {
	  def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
	  matcher ? matcher[0][1] : null
	}
	
	def getArtifactId() {
		def matcher = readFile('pom.xml') =~ '<artifactId>(.+)</artifactId>'
		matcher ? matcher[0][1] : null
	}
	def reflection(){
		echo ("${this.class.name}")
		def methods = this.class.declaredMethods
		def methodsNames = new StringBuilder()
		methods.each
		{
			methodsNames << it.name << " "
		}
		echo "\tMethods Names: ${methodsNames}"
		def fields = this.class.declaredFields
		def fieldsNames = new StringBuilder()
		fields.each
		{
			fieldsNames << it.name << " "
		}
		echo "\tFields Names: ${fieldsNames}"
		
	}
	/**
	 * Function to deploy artifacts
	 */
	def deploy(war, id) {
		sh "cp ${war} /tmp/webapps/${id}.war"
	}
	
	/**
	 * Function to undeploy artifacts
	 */
	def undeploy(id) {
		sh "rm /tmp/webapps/${id}.war"
	}
	
	/**
	 * Function to create a random Id for testing
	 */
	def runWithServer(body) {
		def id = UUID.randomUUID().toString()
		deploy 'target/x.war', id
		try {
			body.call "http://localhost:8080/${id}/"
		} finally {
			undeploy id
		}
	}