# `cnm-to-granule` task

## Installation

To build the Lambda code, Refer to following Confluence page:
https://wiki.jpl.nasa.gov/pages/viewpage.action?spaceKey=PD&title=SonarQube%2C+Jacoco+and+Java+17+upgrade
```shell
* Build with sonarQube and Jacoco report
mvn clean verify sonar:sonar \
 -Dsonar.projectKey=cnm2cma-opensource \
 -Dsonar.projectName='cnm2cma-opensource' \
 -Dsonar.host.url=http://localhost:9000 \
 -Dsonar.token=sqp_6dc05b1aa1f622b45112927d2a0510f209776860
 
* Makde sure using java 11 and gradle 8.3
mvn clean dependency:copy-dependencies
gradle build
```

The build artifact will be generated in `build/distributions` and can be uploaded directly to AWS Lambda as the source code.
