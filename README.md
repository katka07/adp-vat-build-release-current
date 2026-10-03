# ADP-style VAT Build & Release Platform

Training project that models the Build & Release workflow from your ADP-era experience while intentionally using a current supported Java/Maven/Jenkins/Ansible/Docker toolchain for hands-on learning today.

## Toolchain philosophy

The **process** is historical Build/Release: compile, qualify, publish, QA/SQA approve, promote, deploy, and rollback. The **lab tools** are current: Java 25 LTS, Maven 3.10+, JUnit 6, current Jenkins LTS, Docker 29.x, and Ansible Core 2.21+. Do not claim those exact modern versions were used in the historical job.

## Official release artifact

The product artifact is a versioned executable JAR:

```text
vat-processing-service-<version>.jar
```

Docker is optional runtime packaging. QA and Production deploy the exact JAR promoted through JFrog Artifactory.

## End-to-end release model

```text
Git commit / release tag
        |
        v
Jenkins
        |
        v
mvn clean deploy -Drevision=<version>
        |
        +-- compile
        +-- Surefire unit tests
        +-- package versioned JAR
        +-- Failsafe JAR smoke tests
        +-- Failsafe engineering integration tests
        +-- deploy candidate to JFrog
        |
        v
SonarQube quality gate
        |
        v
Ansible -> QA
        |
        v
Unified QA/SQA qualification stage
  - Maven qualification tests
  - SQA regression/approval
        |
        v
JFrog candidate -> production repository promotion
        |
        v
Ansible -> Production
        |
        v
safe post-deploy verification
```

Rollback never rebuilds source. Jenkins/Ansible redeploy a previously approved immutable JAR from the production Artifactory repository.

## Production versioning

The POM uses Maven CI-friendly versions:

```xml
<version>${revision}</version>
```

A harmless local default exists in `.mvn/maven.config`:

```text
-Drevision=0.0.0-SNAPSHOT
```

Production versions come from an exact Git tag such as `v2.6.4` or the Jenkins `RELEASE_VERSION` parameter:

```bash
mvn -Drevision=2.6.4 clean verify
```

There is no production `1.0.0` hardcoded into the POM.

## Important files

- `pom.xml` - Maven lifecycle, dynamic versioning and JFrog repository definitions.
- `Jenkinsfile` - release, unified QA/SQA, production and rollback pipeline.
- `Dockerfile` - optional hardened runtime image for the already-qualified JAR.
- `config/settings-jfrog.xml.example` - credentials mapping for Maven/JFrog.
- `ansible/` - QA/prod deployment, verification and rollback.
- `qa-sqa-tests/` - Maven-driven tests used inside the unified QA/SQA qualification stage against the exact JFrog artifact.
- `MANUAL-COMMANDS.md` - commands to learn the flow before running Jenkins.
- `docs/JFROG-INTEGRATION.md` - Artifactory setup and promotion model.
- `docs/ROLLBACK.md` - rollback design.
