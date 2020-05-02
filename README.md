# vertx-super-s3 &nbsp;[![Build Status](https://api.travis-ci.org/ksclarke/vertx-super-s3.svg?branch=master)](https://travis-ci.org/ksclarke/vertx-super-s3) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/9d91580617f3424ba17f0738746c3991)](https://www.codacy.com/app/ksclarke/vertx-super-s3?utm_source=github.com&utm_medium=referral&utm_content=ksclarke/vertx-super-s3&utm_campaign=Badge_Coverage) [![Vulnerabilities](https://img.shields.io/snyk/vulnerabilities/github/ksclarke/vertx-super-s3)](https://snyk.io/test/github/ksclarke/vertx-super-s3) [![Maven](https://img.shields.io/maven-metadata/v/https/repo1.maven.org/maven2/info/freelibrary/vertx-super-s3/maven-metadata.xml.svg?colorB=brightgreen)](https://search.maven.org/artifact/info.freelibrary/vertx-super-s3) [![Javadocs](http://javadoc.io/badge/info.freelibrary/vertx-super-s3.svg)](http://projects.freelibrary.info/vertx-super-s3/javadocs.html)

This project provides an S3 client for the [Vert.x](https://vertx.io/) toolkit.

### About the Project

Vertx-Super-S3 started as a fork of [SuperS3t](https://github.com/spartango/SuperS3t/), which originally took the inspiration for its name from [JetS3t](http://www.jets3t.org/). While breaking with the tradition of punny names, vertx-super-s3 is still released under the [same license](https://github.com/ksclarke/vertx-super-s3/blob/master/LICENSE.txt) as its predecessor.

Like its predecessor, vertx-super-s3 is essentially a shim over Vert.x's native HttpClient. It's value is that it provides some conveniences for interacting with AWS' S3 service. These include: the ability to sign S3 requests (using version two or four of AWS' signing protocol), convenience classes to help deal with the listing of S3 buckets, a simplified way to set user metadata on S3 objects, and methods for authenticating S3 connections without requiring the use of the AWS S3 Java SDK.

Currently, vertx-super-s3 works with Vert.x version 3.x. It is not (yet) compatible with the upcoming 4.x Vert.x release. Support for the 4.x Vert.x release is planned.

### Building the Project

To check out and build the project, type the following on the command line:

    git clone https://github.com/ksclarke/vertx-super-s3.git
    cd vertx-super-s3
    mvn package

To generate the project's Javadocs, run:

    mvn javadoc:javadoc

Javadocs for the latest release can also be seen at the project's [website](http://projects.freelibrary.info/vertx-super-s3/javadocs.html).

### Using the Project

To add vertx-super-s3 as a dependency, add the following to your POM file (supplying the version you'd like to use):

    <dependency>
      <groupId>info.freelibrary</groupId>
      <artifactId>vertx-super-s3</artifactId>
      <version>${vertx.s3.version}</version>
    </dependency>

### Running the Tests

There are three types of tests: unit, functional, and integration. By default (i.e., when running the `mvn package` command), only the unit tests are run. These do not require any special setup.

In order to be able to run the functional tests, you must have [Docker installed](https://docs.docker.com/get-docker/) (and operational) on your system. Once this requirement is met, the following command can be used to build the project and run both the unit and functional tests:

    mvn verify
To run the project's integration tests, a real AWS account and S3 bucket must be used. To see what sort of permissions this account must be granted, check out the sample IAM policy in the `src/test/resources` directory. This account's credentials will also need to be put into your local `~/.aws/credentials` file (using the profile name `vertx-s3`). More detailed instructions for [how to set this up](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html) can be found in AWS' documentation. The result should look something like:

    [vertx-s3]
    aws_secret_access_key = YOUR_SECRET_KEY
    aws_access_key_id = YOUR_ACCESS_KEY

Lastly, you will need to create an S3 bucket that the tests will use. Bucket names must be globally unique so the defaults in this project will not work for you. There are two variables in the project's POM file that are important:

    <test.s3.bucket>YOUR_BUCKET_NAME</test.s3.bucket>
    <test.s3.region>us-east-1</test.s3.region>

Once your S3 bucket has been created, its property value can be overridden, at build time, on the command line (for instance: `mvn verify -Dtest.s3.bucket=YOUR_BUCKET_NAME`). Alternatively, the bucket name can also be set in a profile property in your local `settings.xml` file. Consult the Maven [settings documentation](https://books.sonatype.com/mvnref-book/reference/appendix-settings-sect-details.html) for more detailed information about the Maven settings file, if needed.

After the integration test setup has been done all three types of tests can be run along with the build by typing:

    mvn verify -Ps3it

or

    mvn verify -Ps3it -Dtest.s3.bucket=YOUR_BUCKET_NAME

(depending on whether you set the bucket name and default region in the Maven settings file or not).

That's about it. Of course, you don't need to run the functional and integration tests if you don't want to. The standard build (using `mvn package`) will produce a Jar file that can be used for local testing.

### Getting Started

The [project's tests](https://github.com/ksclarke/vertx-super-s3/tree/master/src/test/java/info/freelibrary/vertx/s3) are a good place to looks for examples of how to use vertx-super-s3's S3Client. A simple (somewhat artificial) example, though, is also given below:

```
final Vertx vertx = Vertx.vertx();
final S3Client s3Client = new S3Client(vertx, new Profile("vertx-s3"));
final String fileName = "ucla-library-logo.png";
final Future<File> future = Future.future();

// Do something with the result of our S3 download
future.setHandler(download -> {
    if (download.succeeded()) {
        LOGGER.info("Successfully downloaded: {}", download.result());
    } else {
        LOGGER.error("Download failed: {}", download.cause().getMessage());
    }
});

// Do our S3 download
s3Client.get("presentation-materials", fileName, get -> {
    final int statusCode = get.statusCode();

    if (statusCode == HTTP.OK) {
        get.bodyHandler(body -> {
            final Path path = Paths.get(System.getProperty("java.io.tmpdir"), fileName);

            // Write our S3 file to our local file system
            vertx.fileSystem().writeFile(path.toString(), body, write -> {
                if (write.succeeded()) {
                    future.complete(path.toFile());
                } else {
                    future.fail(write.cause());
                }
            });
        });
    } else {
        future.fail(LOGGER.getMessage("Unexpected status code: {} [{}]", statusCode, get.statusMessage()));
    }
}, error -> {
    future.fail(error);
});
```

### Contact

If you have questions about vertx-super-s3 <a href="mailto:ksclarke@ksclarke.io">feel free to ask</a>; also, if you encounter a problem with the library or have suggestions about how to improve on it, please feel free to [open a ticket](https://github.com/ksclarke/vertx-super-s3/issues "GitHub Issue Queue") in the project's issues queue.

Thanks for your interest in this project.
