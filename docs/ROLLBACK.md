# Production Rollback Model

## Principle

Rollback means redeploying a previously approved immutable artifact. It does not mean rebuilding an old Git commit.

Production layout:

```text
/opt/vat/
  releases/
    2.6.2/
      vat-processing-service-2.6.2.jar
      release.properties
    2.6.3/
      vat-processing-service-2.6.3.jar
      release.properties
    2.6.4/
      vat-processing-service-2.6.4.jar
      release.properties
  current  -> /opt/vat/releases/2.6.4
  previous -> /opt/vat/releases/2.6.3
```

Ansible preserves the currently active release as `previous` before switching `current`.

## Automatic rollback during a failed production deployment

If post-deployment verification fails in the Jenkins release pipeline:

```text
2.6.3 active
   -> deploy 2.6.4
   -> verification fails
   -> ansible/rollback-previous.yml
   -> current points back to 2.6.3
```

## Planned rollback later

Use the Jenkins rollback action and give an explicitly approved version:

```text
ACTION=ROLLBACK_PROD
ROLLBACK_VERSION=2.6.2
```

Jenkins calls `ansible/rollback.yml`. The role downloads `2.6.2` from the production-approved JFrog repository if it is not already staged, verifies its embedded version, then changes `current`.

The production repository is the allow-list: versions that never passed QA/SQA should not be rollback targets.
