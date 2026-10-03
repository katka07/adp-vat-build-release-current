# Manual Commands Before Jenkins

This edition expects the current lab baseline: Java 25 LTS and Maven 3.10+. The POM enforces those versions.

Run the flow manually first. Jenkins automates these same boundaries later.

## 1. Toolchain

```bash
java -version
javac -version
mvn -version
git --version
ansible --version
jf --version
docker --version
```

## 2. Versioning

The local default is `0.0.0-SNAPSHOT` from `.mvn/maven.config`.

Inspect it:

```bash
mvn help:evaluate -Dexpression=project.version -q -DforceStdout
```

Simulate a production release version without editing `pom.xml`:

```bash
VERSION=2.6.4
mvn -Drevision="$VERSION" help:evaluate -Dexpression=project.version -q -DforceStdout
```

A production Jenkins build normally derives `VERSION` from a Git tag:

```bash
git tag v2.6.4
git describe --tags --exact-match HEAD
```

## 3. Build and engineering qualification

```bash
VERSION=2.6.4
mvn -Drevision="$VERSION" clean verify
```

This runs compile, unit tests, packaging, JAR smoke tests and engineering integration tests through Maven.

Artifact:

```bash
ls -lh "target/vat-processing-service-${VERSION}.jar"
java -jar "target/vat-processing-service-${VERSION}.jar" version
```

## 4. Configure JFrog

```bash
export JFROG_URL='https://company.jfrog.io'
export JFROG_MAVEN_VIRTUAL_REPO='maven-virtual'
export JFROG_SNAPSHOT_REPO='vat-snapshot-local'
export JFROG_CANDIDATE_REPO='vat-candidate-local'
export JFROG_PRODUCTION_REPO='vat-release-local'
export JFROG_USERNAME='ci-user'
export JFROG_TOKEN='<token>'
```

Never commit the token.

## 5. Build, test and publish candidate

```bash
mvn -s config/settings-jfrog.xml.example \
  -Drevision="$VERSION" \
  clean deploy
```

## 6. Optional Docker runtime image

The Docker image contains the already-qualified JAR; it does not compile source.

```bash
docker build \
  --build-arg JAR_FILE="target/vat-processing-service-${VERSION}.jar" \
  --build-arg APP_VERSION="$VERSION" \
  --build-arg VCS_REF="$(git rev-parse HEAD)" \
  --build-arg BUILD_DATE="$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  -t "vat-processing-service:${VERSION}" .

docker run --rm "vat-processing-service:${VERSION}" version
```

## 7. Deploy exact candidate to QA with Ansible

Replace the QA inventory host first.

```bash
export ARTIFACT_SHA256="$(sha256sum target/vat-processing-service-${VERSION}.jar | awk '{print $1}')"

ansible -i ansible/inventory/qa.ini qa -m ping

ansible-playbook \
  -i ansible/inventory/qa.ini \
  ansible/deploy.yml \
  -e "release_version=${VERSION}" \
  -e "jfrog_repo=${JFROG_CANDIDATE_REPO}"

ansible-playbook \
  -i ansible/inventory/qa.ini \
  ansible/verify.yml
```

## 8. Run unified QA/SQA qualification

This is the single QA/SQA stage. Maven downloads the exact candidate JAR from Artifactory and runs the qualification suite. The SQA team can continue its regression/business checks against that same deployed candidate and records the approval at this same stage.

```bash
export JFROG_QA_SOURCE_REPO="$JFROG_CANDIDATE_REPO"

mvn -s config/settings-jfrog.xml.example \
  -f qa-sqa-tests/pom.xml \
  -Dvat.version="$VERSION" \
  clean verify
```

After the Maven qualification passes, complete any SQA regression/business checks against this same candidate. Record one combined **QA/SQA approval**. Build & Release waits for that approval before promotion. Do not rebuild the JAR.

## 9. Promote exact QA/SQA-approved candidate to production repository

```bash
jf config add vat-ci \
  --url="$JFROG_URL" \
  --access-token="$JFROG_TOKEN" \
  --interactive=false

MAVEN_PATH="com/example/vat/vat-processing-service/${VERSION}"

jf rt cp \
  "${JFROG_CANDIDATE_REPO}/${MAVEN_PATH}/" \
  "${JFROG_PRODUCTION_REPO}/${MAVEN_PATH}/" \
  --server-id=vat-ci
```

## 10. Production deployment

```bash
ansible -i ansible/inventory/prod.ini prod -m ping

ansible-playbook \
  -i ansible/inventory/prod.ini \
  ansible/deploy.yml \
  -e "release_version=${VERSION}" \
  -e "jfrog_repo=${JFROG_PRODUCTION_REPO}"

ansible-playbook \
  -i ansible/inventory/prod.ini \
  ansible/verify.yml
```

## 11. Rollback to an explicitly approved version

```bash
ROLLBACK_VERSION=2.6.3

ansible-playbook \
  -i ansible/inventory/prod.ini \
  ansible/rollback.yml \
  -e "rollback_version=${ROLLBACK_VERSION}"

ansible-playbook \
  -i ansible/inventory/prod.ini \
  ansible/verify.yml
```

For an immediate failure during a deployment, restore the release that was active just before the change:

```bash
ansible-playbook \
  -i ansible/inventory/prod.ini \
  ansible/rollback-previous.yml
```
