title=Welcome to the Vert.x Pairtree Project
date=2016-08-03
type=page
status=published
~~~~~~

An S3 client for the Vert.x toolkit, vertx-super-s3 is a fork of Anand Gupta's [SuperS3t](https://github.com/spartango/SuperS3t/) project. The SuperS3t code was originally integrated into my [vertx-pairtree](https://github.com/ksclarke/vertx-pairtree) project, but later split out so that it could be used independently of the vertx-pairtree wrapper. I appreciate Anand Gupta making the original source code available under an open source library. I'm releasing my modifications under the same license he used.


### Getting Started

To check out and build the project, type on the command line:

    git clone https://github.com/ksclarke/vertx-super-s3.git
    cd vertx-super-s3
    mvn install

This will run the build and the unit tests for the file system back end. Right now, the only tests for the S3 back end are integration tests. To run a build with them, you will need to put the following properties in your [settings.xml file](https://maven.apache.org/settings.html) and run the build with the `s3it` profile:

    <vertx.s3.bucket>YOUR_S3_BUCKET_NAME</vertx.s3.bucket>
    <vertx.s3.access_key>YOUR_ACCESS_KEY</vertx.s3.access_key>
    <vertx.s3.secret_key>YOUR_SECRET_KEY</vertx.s3.secret_key>

Or, you can supply the required properties on the command line (with the same `s3it` profile) when you build the project:

    mvn install -Ps3it -Dvertx.s3.bucket=YOUR_S3_BUCKET_NAME \
      -Dvertx.s3.access_key=YOUR_ACCESS_KEY \
      -Dvertx.s3.secret_key=YOUR_SECRET_KEY

The YOUR_S3_BUCKET_NAME, YOUR_ACCESS_KEY and YOUR_SECRET_KEY values obviously need to be replaced with real values. Within AWS' Identity and Access Management service you can configure a user that has permission to perform actions on the S3 bucket you've created for this purpose. For an example of the IAM inline user policy for an S3 bucket, consult the [example JSON file](https://github.com/ksclarke/vertx-super-s3/blob/master/src/test/resources/sample-iam-policy.json) in the project's `src/test/resources` directory.

You can name the S3 bucket whatever you want (and change its system property to match), but make sure the bucket is only used for these integration tests. The tests will delete all the contents of the bucket as a part of the test tear down. When these tests are run in Travis, a JDK name is appended onto the S3 bucket name so that the tests can be run concurrently (e.g. YOUR_S3_BUCKET_NAME-openjdk11). These buckets also need to be created ahead of time in order for the tests to pass.

If you want to put your test S3 bucket in a region other than the standard us-east-1, you will also need to supply a `vertx.s3.region` argument. For example:

    mvn install -Ps3it -Dvertx.s3.bucket=YOUR_S3_BUCKET_NAME \
      -Dvertx.s3.access_key=YOUR_ACCESS_KEY \
      -Dvertx.s3.secret_key=YOUR_SECRET_KEY \
      -Dvertx.s3.region="us-west-2"

It can also be supplied through your settings.xml file. At this point, only regions that support signature version 2 authentication are supported. To see the valid S3 region endpoints, consult [AWS' documentation](http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region).

Lastly, if you don't want to build it yourself, the library can be downloaded from the Maven central repository by putting the following in your project's [pom.xml file](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html):

    <dependency>
      <groupId>info.freelibrary</groupId>
      <artifactId>vertx-super-s3</artifactId>
      <version>${vertx.s3.version}</version>
    </dependency>

### Contact

If you have questions about vertx-super-s3 <a href="mailto:ksclarke@ksclarke.io">feel free to ask</a> or, if you encounter a problem, please feel free to [open a ticket](https://github.com/ksclarke/vertx-super-s3/issues "GitHub Issue Queue") in the project's issues queue.
