# JFrog Artifactory Integration

## Recommended repositories

Create these Maven repositories in Artifactory (names are examples):

```text
maven-virtual             virtual repository used for dependency/plugin resolution
vat-snapshot-local        local repository for SNAPSHOT builds
vat-candidate-local       local repository for release candidates
vat-release-local         local repository containing QA/SQA-approved production artifacts
```

Export the names to the build environment/Jenkins global configuration:

```bash
export JFROG_URL='https://company.jfrog.io'
export JFROG_MAVEN_VIRTUAL_REPO='maven-virtual'
export JFROG_SNAPSHOT_REPO='vat-snapshot-local'
export JFROG_CANDIDATE_REPO='vat-candidate-local'
export JFROG_PRODUCTION_REPO='vat-release-local'
```

Credentials are not stored in `pom.xml`:

```bash
export JFROG_USERNAME='ci-user'
export JFROG_TOKEN='<access-token>'
```

Maven reads them from `config/settings-jfrog.xml.example`.

## What the POM does

`pom.xml` contains:

- `repositories` and `pluginRepositories` pointing at the JFrog Maven virtual repository;
- `distributionManagement/repository` pointing at the candidate repository for non-SNAPSHOT versions;
- `distributionManagement/snapshotRepository` pointing at the snapshot repository;
- a property for the production repository, which is deliberately not a Maven deploy target.

The production repository is populated only by promotion after QA/SQA approval.

## Release build

```bash
mvn -s config/settings-jfrog.xml.example \
  -Drevision=2.6.4 \
  clean deploy
```

This publishes Maven coordinates:

```text
com.example.vat:vat-processing-service:2.6.4
```

to the candidate repository after Maven's tests and `verify` phase pass.

## Promote the exact artifact

Configure JFrog CLI:

```bash
jf config add vat-ci \
  --url="$JFROG_URL" \
  --access-token="$JFROG_TOKEN" \
  --interactive=false
```

Promote/copy the exact Maven version directory:

```bash
VERSION=2.6.4
PATH_IN_REPO="com/example/vat/vat-processing-service/${VERSION}"

jf rt cp \
  "${JFROG_CANDIDATE_REPO}/${PATH_IN_REPO}/" \
  "${JFROG_PRODUCTION_REPO}/${PATH_IN_REPO}/" \
  --server-id=vat-ci
```

This copies the already-tested bytes. Do not rebuild after QA/SQA approval.
