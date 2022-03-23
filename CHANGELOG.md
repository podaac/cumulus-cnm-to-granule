# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] 
### Added
- **PODAAC-4324**
  - Make output of this lambda compliant with cumulus 10.1.2 schema
### Changed
### Deprecated
### Removed
### Fixed
### Security

## [v1.7.0] - 2022-03-15
### Added
- **PODAAC-4308**
  - Support sftp URI from CNM messages
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **Snyk**
  - upgrade aws-java-sdk-s3@1.12.144 to aws-java-sdk-s3@1.12.176
  - manual import jackson-databind@2.13.2 due to @2.12.3 (which has issues) being pulled in by aws-java-sdk-s3 even after version updates

## [v1.6.0] - 2021-12-03
### Added
- **PODAAC-4012**
  - Support https URI from CNM messages
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **Snyk**
  - Upgrade gson@2.8.2 to com.google.code.gson:gson@2.8.9
  - Upgrade aws-java-sdk-s3@1.12.28 to com.amazonaws:aws-java-sdk-s3@1.12.110
  - Upgrade aws-lambda-java-core@1.1.0 to com.amazonaws:aws-lambda-java-core@1.2.1

## [v1.5.4] - 2022-01-20
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **PODAAC-4095**
  - Upgrade to cumulus-message-adapter-java 1.3.9 to address [log4j vulnerability](https://nvd.nist.gov/vuln/detail/CVE-2021-44832)
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.12.28 to com.amazonaws:aws-java-sdk-s3@1.12.144
  - Upgrade com.google.code.gson:gson@2.8.2 to com.google.code.gson:gson@2.8.9

## [v1.5.3] - 2021-12-22
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **PODAAC-4059**
  - Upgrade to cumulus-message-adapter-java 1.3.7 to address [log4j vulnerability](https://nvd.nist.gov/vuln/detail/CVE-2021-45105) 

## [v1.5.2] - 2021-12-15
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **PODAAC-4046**
  - Upgrade to cumulus-message-adapter-java 1.3.5 to address log4j vulnerability

## [v1.5.1] - 2021-07-21
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.11.1016 to com.amazonaws:aws-java-sdk-s3@1.12.28

## [v1.5.0] - 2021-05-12
### Added
- **PODAAC-3208**
    - Added 'source_bucket' key to granule file
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **Snyk**
    - Upgrade aws-java-sdk-s3@1.11.955 to com.amazonaws:aws-java-sdk-s3@1.11.1016
    - Upgrade commons-io:commons-io:2.6 to 2.7

## [v1.4.3] - 2021-02-17
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.11.903 to com.amazonaws:aws-java-sdk-s3@1.11.955

## [v1.4.2] - 2020-12-07
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
- **Snyk**
  - Upgrade aws-java-sdk-s3@1.11.893 to com.amazonaws:aws-java-sdk-s3@1.11.903

## [v1.4.1] - 2020-11-19
### Added
### Changed
### Deprecated
### Removed
### Fixed
- **PODAAC-2775**
  - Upgrade to CMA-java v1.3.2 to fix timeout on large messages.
### Security

## [v1.4.0] - 2020-11-04

### Added
- **PODAAC-2541**
  - a build script under /builder directory, and a jenkins job to build and push release to public github.
- **PODAAC-2547**
   - Added support for filegroups in input CNM message
- **PODAAC-2639**
   - Added dataType and version to output message
### Changed
- **PODAAC-2553**
  - Upgrade aws s3 dependency to com.amazonaws:aws-java-sdk-s3@1.11.660 to fix Snyk errors

### Deprecated

### Removed

### Fixed

### Security

## [v1.3.2] - 2020-06-30

### Added

### Changed

- **PCESA-2166**
  - Updated lambda to use CMA AdapterLogger for logging to Elasticsearch.

### Deprecated

### Removed

### Fixed

### Security