# Validation

This revision keeps the Build/Release process but moves the lab to a current supported toolchain.

Configured toolchain:

```text
Java 25 LTS
Maven 3.10+
JUnit 6.1.2
Maven Compiler Plugin 3.16.0
Maven Surefire/Failsafe 3.6.0
Maven Jar Plugin 3.5.1
Maven Deploy Plugin 3.2.0
Flatten Maven Plugin 1.8.0
Jenkins current LTS / Java-25 build agent
Docker Engine 29.x / Eclipse Temurin 25 JRE image
Ansible Core 2.21+
```

Validated in the packaging environment:

- `pom.xml`, `qa-sqa-tests/pom.xml`, and `config/settings-jfrog.xml.example` parse as valid XML.
- Main Java sources compile successfully on the available JDK 21 compiler, confirming no syntax regression. The actual Maven build is configured and enforced for Java 25.
- Main and QA/SQA tests were migrated from JUnit 4 imports/annotations to JUnit Jupiter 6 style.
- POM plugin versions were refreshed to the current Maven 3.x plugin line used by this lab.
- Jenkinsfile now expects managed tools `jdk25` and `maven-3.10.0` and a `linux-java25-release` agent.
- Dockerfile uses the current Java 25 Eclipse Temurin JRE line and runs as a non-root UID.
- No Java 8 / JDK 8 / Maven 3.8 / JUnit 4 assumptions remain in the source tree.
- QA/SQA remains a single combined qualification/approval stage.
- Rollback still redeploys a previously approved immutable JAR; no rollback rebuild is introduced.

Not executable in this packaging environment:

- Maven 3.10+ (`mvn` is not installed here), so the full JUnit/Surefire/Failsafe lifecycle and `mvn deploy` must be run in your lab.
- Java 25 itself is not installed here; the available compiler is JDK 21.
- Jenkins pipeline execution.
- Authenticated JFrog Artifactory publication/promotion.
- Ansible deployment to your QA/production hosts.
- Docker build/run.

Run `MANUAL-COMMANDS.md` in your lab and validate `mvn clean verify` and `mvn clean deploy` before using the Jenkinsfile unchanged.
