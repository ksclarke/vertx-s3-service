# This container runs tests related to the AWS credentials file

FROM ksclarke/maven-graalvm-musl:${build.image.version}

# Configure the download URL of the simple static file server
ARG SFS_DOWNLOAD_URL="https://github.com/halverneus/static-file-server/releases/download"

# Set the location of the data directory
ENV HOME_DIR="/home/${project.artifactId}"
ENV DATA_DIR="/usr/local/data"

# Set up user, data directory, and workspace
RUN useradd -U -r -m "${project.artifactId}"

# Working directories (where things happen)
WORKDIR "$DATA_DIR"
WORKDIR "$HOME_DIR"

# Set up the static file server's root directory
RUN chown -R "${project.artifactId}" "$DATA_DIR"

# Copy the project over to the Docker container
COPY --chown="${project.artifactId}" project-copy/ maven/
COPY --chown="${project.artifactId}" target-copy/ maven/

USER "${project.artifactId}"

# Set up some fake S3 credentials
RUN set +ex && mkdir -p ~/.aws ~/.m2/repository && \
    printf "[default] \n" >> ~/.aws/credentials && \
    printf "aws_secret_access_key = FAKE_SECRET_KEY \n" >> ~/.aws/credentials && \
    printf "aws_access_key_id = FAKE_ACCESS_KEY \n" >> ~/.aws/credentials && \
    printf "\n" >> ~/.aws/credentials && \
    printf "[vertx-s3] \n" >> ~/.aws/credentials && \
    printf "aws_secret_access_key = FAKE_SECRET_KEY \n" >> ~/.aws/credentials && \
    printf "aws_access_key_id = FAKE_ACCESS_KEY \n" >> ~/.aws/credentials

# Download the static file server
RUN curl -sL "$SFS_DOWNLOAD_URL/${sfs.version}/static-file-server-${sfs.version}-linux-amd64" > sfs && \
    chmod 755 sfs

# Run the profile tests that use our newly configured fake S3 credentials
RUN cd maven && mvn -l "$DATA_DIR/maven.log" -fn -ntp surefire:test -Dtest=S3ClientProfilesTest

# Override the base image's entrypoint
ENTRYPOINT ["/usr/bin/env"]

# Run the static file server
CMD FOLDER="$DATA_DIR" SHOW_LISTING="true" PORT="${sfs.port}" "$HOME_DIR/sfs"
