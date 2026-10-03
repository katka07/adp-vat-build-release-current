# Jenkins Setup Required by Jenkinsfile

The lab uses current supported CI tooling while preserving the Build/Release process being practiced.

Configure these Jenkins managed tools:

```text
JDK tool name:   jdk25
Maven tool name: maven-3.10.0
```

Use a current Jenkins LTS. At the time this project was refreshed, Jenkins LTS 2.568.3 is tested with JDK 21 and 25.

Install/configure plugins used by the Jenkinsfile:

```text
Pipeline / Declarative Pipeline
Credentials Binding
SSH Agent
JUnit
SonarQube Scanner for Jenkins
```

Install these command-line tools on the release agent:

```text
git
docker
ansible
jf (JFrog CLI)
sha256sum
```

Create Jenkins credentials:

```text
jfrog-vat-ci   Username with access-token/password credential
vat-deploy-ssh SSH private-key credential for QA/Production hosts
```

Configure these non-secret global/folder environment variables:

```text
JFROG_URL
JFROG_MAVEN_VIRTUAL_REPO
JFROG_SNAPSHOT_REPO
JFROG_CANDIDATE_REPO
JFROG_PRODUCTION_REPO
```

Configure the SonarQube server in Jenkins with the name:

```text
sonarqube
```

The example inventory uses RFC 5737 documentation addresses. Replace them with your lab hosts before deployment.
