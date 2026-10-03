# Tools and Installation

This lab intentionally uses a **current supported toolchain** rather than recreating 2017 software versions. The workflow models the Build/Release responsibilities from that period, while the tools are suitable for learning today.

## Recommended lab baseline (October 2026)

```text
Git                         current supported release
Java                        25 LTS
Maven                       3.10.0
JUnit                       6.1.2
Maven Compiler Plugin       3.16.0
Maven Surefire/Failsafe     3.6.0
Jenkins                     current LTS (2.568.3 at project refresh)
Docker Engine               29.x
Ansible Core                 2.21.x
JFrog Artifactory / CLI     current supported release
SonarQube                   current supported release
Graphviz                    current distro-supported release
```

The POM enforces Java 25 and Maven 3.10+ for reproducible local/CI behavior.

## Required on a build/release workstation

```text
Git
JDK 25
Maven 3.10+
Ansible Core 2.21+
JFrog CLI
Docker Engine 29+   (optional runtime-image path)
Graphviz             (only to regenerate diagrams)
```

Jenkins agents additionally need SSH connectivity to QA/Production hosts plus access to JFrog Artifactory and SonarQube.

## Ubuntu-style base packages

```bash
sudo apt-get update
sudo apt-get install -y git curl unzip ca-certificates gnupg graphviz python3 python3-pip
```

Install Java 25, Maven 3.10+, Docker, Ansible Core, and JFrog CLI from their vendor-supported packages/repositories for your OS. For a corporate lab, prefer centrally managed packages or Jenkins managed tools rather than downloading binaries ad hoc during every build.

## Verify

```bash
java -version
javac -version
mvn -version
git --version
ansible --version
jf --version
docker --version
```

Expected Java major:

```text
25
```

Expected Maven baseline:

```text
3.10.x or newer 3.x
```

## Jenkins managed-tool names expected by the included Jenkinsfile

```text
JDK tool name:   jdk25
Maven tool name: maven-3.10.0
```

The Jenkins controller itself should be on a current supported LTS and may run on a supported controller JDK independently of the JDK selected for the application build.
