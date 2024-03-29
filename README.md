# vertx-s3-service

<hr/>

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a39d9efc281a4001b9779964b9fd814c)](https://www.codacy.com/gh/ksclarke/vertx-s3-service/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ksclarke/vertx-s3-service&amp;utm_campaign=Badge_Grade) [![Known Vulnerabilities](https://snyk.io/test/github/ksclarke/vertx-s3-service/badge.svg)](https://snyk.io/test/github/ksclarke/vertx-s3-service) [![Maven](https://img.shields.io/maven-metadata/v/https/repo1.maven.org/maven2/info/freelibrary/vertx-s3-service/maven-metadata.xml.svg?colorB=brightgreen)](https://search.maven.org/artifact/info.freelibrary/vertx-s3-service) [![Javadocs](http://javadoc.io/badge/info.freelibrary/vertx-s3-service.svg)](https://javadoc.io/doc/info.freelibrary/vertx-s3-service)

### About the Project

Vertx-S3-Service provides a simple, high-level S3 client and service interface for the [Vert.x](https://vertx.io/) toolkit. It's essentially a shim over the Vert.x HttpClient and, then, a Vert.x Service interface layered on top of that.

For convenience, either the S3 client or the Vert.x service interface can be used. The client supports callbacks and futures. The service just supports futures. For working examples of how to use Vertx-S3-Service, take a look at the fledgling [Getting Started](docs/README.md) documentation.

_Historical Note:_ This project's S3 client started as a fork of [SuperS3t](https://github.com/spartango/SuperS3t). But, with the shift to using a Vert.x Service interface, it has grown into something different. The original source code from the SuperS3t project is kept, untouched, in the repo's `supers3t` branch and the license for that code can be found in the project's [docs/third_party_licenses](docs/third_party_licenses/supers3t-license.txt) directory. 

### Building the Project

Prerequites for building the project include:

* A [JDK](https://adoptium.net/) ( &gt;= 11 ) - This runs the code
* [Maven](https://maven.apache.org/) ( &gt;= 3.6 ) - This builds the code
* [Docker](https://www.docker.com/get-started) ( &gt;= 20.10.8 ) - This tests the code

To check out and build the project, type the following on the command line:

    git clone https://github.com/ksclarke/vertx-s3-service.git
    cd vertx-s3-service
    mvn verify

To generate the project's Javadocs, run:

    mvn javadoc:javadoc

Javadocs for the latest release can also be found online at [https://javadoc.io/doc/info.freelibrary/vertx-s3-service](https://javadoc.io/doc/info.freelibrary/vertx-s3-service).

### Using the Project

To add vertx-s3-service as a dependency of your Maven build project, add the following to your POM file (supplying the version you'd like to use):

    <dependency>
      <groupId>info.freelibrary</groupId>
      <artifactId>vertx-s3-service</artifactId>
      <version>${vertx.s3.version}</version>
    </dependency>

### Running the Tests

There are three types of tests: unit, functional, and integration. By default (when running the `mvn verify` command), only the unit and functional tests are run. The functional tests run against a Dockerized S3-compatible environment.

To run the project's integration tests, a real AWS account and S3 bucket must be used. To see what sort of permissions this account must be granted, check out the sample IAM policy in the `src/test/resources` directory. Your account's credentials will also need to be put into your local `~/.aws/credentials` file and associated with a `vertx-s3` profile.

More detailed instructions for how to set this up can be found in AWS' [documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html). The result should look something like:

    [vertx-s3]
    aws_secret_access_key = YOUR_SECRET_KEY
    aws_access_key_id = YOUR_ACCESS_KEY

Lastly, you will need to create an S3 bucket that the tests will use. Bucket names must be globally unique so the defaults in this project will not work for you. There are two variables in the project's POM file that are important:

    <test.s3.bucket>YOUR_BUCKET_NAME</test.s3.bucket>
    <test.s3.region>us-east-1</test.s3.region>

Once your S3 bucket has been created, the default property values in the build can be overridden from the command line (for instance: `mvn verify -Dtest.s3.bucket=YOUR_BUCKET_NAME`). Alternatively, the bucket and region names can also be set in a profile in your local `settings.xml` file. Consult the Maven [settings documentation](https://books.sonatype.com/mvnref-book/reference/appendix-settings-sect-details.html) for more detailed information about the Maven settings file, if needed.

After the integration test setup has been done all three types of tests can be run along with the build by typing:

    mvn verify -P s3_it

or

    mvn verify -P s3_it -Dtest.s3.bucket=YOUR_BUCKET_NAME -Dtest.s3.region=YOUR_REGION

The first would be an example of running the build with the additional profile values put into your settings.xml file. The second is an example of supplying the required values on the command line.

That's about it. Of course, you don't need to run the integration tests if you don't want to. The standard build (using `mvn verify`) will produce a Jar file that can be used for local testing.

### Contact

If you have questions about vertx-s3-service <a href="https://github.com/ksclarke/vertx-s3-service/discussions">feel free to ask</a>; also, if you encounter a problem with the library or have suggestions about how to improve on it, please feel free to [open a ticket](https://github.com/ksclarke/vertx-s3-service/issues "GitHub Issue Queue") in the project's issues queue.

Thanks for your interest in this project.
