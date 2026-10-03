# VAT Processing Service - QA/SQA Qualification Record

## Build identity

- Version: `<release-version>`
- Git commit: `<git-sha>`
- Jenkins build: `<build-number>`
- JFrog candidate coordinates: `com.example.vat:vat-processing-service:<release-version>`
- SHA-256: `<artifact-sha256>`

## Build & Release qualification completed

- Maven compile
- Surefire unit/engineering tests
- Executable JAR packaging
- Failsafe packaged-JAR smoke tests
- Failsafe engineering integration tests
- SonarQube quality gate
- JFrog candidate publication
- QA deployment
- Unified QA/SQA Maven qualification

## Artifact under QA/SQA test

```text
vat-processing-service-<release-version>.jar
```

QA/SQA should test this exact Artifactory artifact. Production promotion copies the same artifact bytes; there is no rebuild after the combined QA/SQA approval.
