
FROM ksclarke/alpine-maven

# Run non-privileged
RUN addgroup --system "${project.artifactId}" && adduser --system "${project.artifactId}" "${project.artifactId}"
USER ${project.artifactId}
WORKDIR /home/${project.artifactId}

# Copy the project over to the Docker container
COPY --chown=${project.artifactId} maven/ .

# Set up some fake S3 credentials
RUN set +ex && rm -f ./*.jar && mkdir -p .aws .m2/repository && \
    printf "[default] \n" >> ~/.aws/credentials && \
    printf "aws_secret_access_key = FAKE_SECRET_KEY \n" >> ~/.aws/credentials && \
    printf "aws_access_key_id = FAKE_ACCESS_KEY \n" >> ~/.aws/credentials && \
    printf "\n" >> ~/.aws/credentials && \
    printf "[vertx-s3] \n" >> ~/.aws/credentials && \
    printf "aws_secret_access_key = FAKE_SECRET_KEY \n" >> ~/.aws/credentials && \
    printf "aws_access_key_id = FAKE_ACCESS_KEY \n" >> ~/.aws/credentials

# Keep the container running for our tests
CMD tail -f /dev/null
