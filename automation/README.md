# Automation

A complete reference `Jenkinsfile` is now included because this revision is intended to show the full Build & Release path.

The pipeline covers:

1. checkout;
2. version resolution from Git tag / Jenkins parameter;
3. Maven build, unit tests, packaged-JAR smoke tests and engineering integration tests;
4. Maven deploy to the JFrog candidate/snapshot repository;
5. SonarQube quality gate;
6. optional hardened Docker image creation from the qualified JAR;
7. Ansible QA deployment;
8. unified QA/SQA stage against the exact Artifactory candidate, including Maven qualification and approval;
9. JFrog promotion to the production-approved repository;
10. serial Ansible production deployment;
11. safe verification;
12. automatic rollback to the immediately previous release on deployment verification failure;
13. explicit rollback to any previously approved Artifactory version.

Change Jenkins tool names, credentials IDs, inventory hosts and environment variables to match your lab.
