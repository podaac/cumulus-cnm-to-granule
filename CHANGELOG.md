# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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