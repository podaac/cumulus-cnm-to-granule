mvn dependency:copy-dependencies
gradle -x test build
curl -u jenkins:${JENKINS_ARTIFACT_TOKEN} -X PUT https://podaac-ci.jpl.nasa.gov:8443/artifactory/ext-release-local/gov/nasa/cumulus/cnmToGranule/1.3.0/cnmToGranule-1.3.0.zip  -T build/distributions/cnmToGranule-1.3.0.zip
