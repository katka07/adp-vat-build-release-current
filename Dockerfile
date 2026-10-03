# Runtime image for the already-qualified Maven artifact.
# The immutable JAR in JFrog Artifactory remains the official release artifact.
FROM eclipse-temurin:25-jre-noble

ARG JAR_FILE
ARG APP_VERSION=unknown
ARG VCS_REF=unknown
ARG BUILD_DATE=unknown

LABEL org.opencontainers.image.title="vat-processing-service" \
      org.opencontainers.image.description="VAT processing release artifact runtime" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.created="${BUILD_DATE}"

RUN groupadd --system --gid 10001 vat \
 && useradd --system --uid 10001 --gid vat --home-dir /opt/vat --shell /usr/sbin/nologin vat \
 && mkdir -p /opt/vat \
 && chown -R vat:vat /opt/vat

WORKDIR /opt/vat
COPY --chown=vat:vat ${JAR_FILE} /opt/vat/app.jar

USER 10001:10001

ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

STOPSIGNAL SIGTERM
ENTRYPOINT ["java", "-jar", "/opt/vat/app.jar"]
CMD ["version"]
