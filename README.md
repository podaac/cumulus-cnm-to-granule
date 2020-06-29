# `cnm-to-granule` task

## Installation

To build the Lambda code:

```shell
mvn clean dependency:copy-dependencies
gradle build
```

The build artifact will be generated in `build/distributions` and can be uploaded directly to AWS Lambda as the source code.

## Due to this repo is currently in GitHub.  The following sample command might be necessary to push change to up stream
git push --set-upstream origin feature/PCESA-2166


## [v1.3.2] - 2020-06-29

### Added

### Changed

- **PCESA-2166**
  - Updated lambda to use CMA AdapterLogger for logging to Elasticsearch.

### Deprecated

### Removed

### Fixed

### Security
