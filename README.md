# vertx-super-s3 &nbsp;[![Build Status](https://api.travis-ci.org/ksclarke/vertx-super-s3.svg?branch=master)](https://travis-ci.org/ksclarke/vertx-super-s3) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/9d91580617f3424ba17f0738746c3991)](https://www.codacy.com/app/ksclarke/vertx-super-s3?utm_source=github.com&utm_medium=referral&utm_content=ksclarke/vertx-super-s3&utm_campaign=Badge_Coverage) [![Known Vulnerabilities](https://snyk.io/test/github/ksclarke/vertx-super-s3/badge.svg)](https://snyk.io/test/github/ksclarke/vertx-super-s3) [![Maven](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/info/freelibrary/vertx-super-s3/maven-metadata.xml.svg?colorB=brightgreen)](http://mvnrepository.com/artifact/info.freelibrary/vertx-supers3) [![Javadocs](http://javadoc.io/badge/info.freelibrary/vertx-super-s3.svg)](http://projects.freelibrary.info/vertx-super-s3/javadocs.html)

An S3 client for the Vert.x toolkit, vertx-super-s3 is a fork of Anand Gupta's [SuperS3t](https://github.com/spartango/SuperS3t/) project (which was named after the [JetS3t](http://www.jets3t.org/) library, another S3 library that provides a nice, synchronous, Java implementation). The SuperS3t code was originally integrated into my own [vertx-pairtree](https://github.com/ksclarke/vertx-pairtree) project, but I later split out so that it could be used by some of my other projects. It now lives on its own. Thanks to Anand Gupta for making the original code available under a MIT license. My modifications are available under the same license.

### Getting Started

To check out and build the project, type the following on the command line:

    git clone https://github.com/ksclarke/vertx-super-s3.git
    cd vertx-super-s3
    mvn install

This will put the vertx-super-s3 library in your local Maven repository. Projects built on your local machine will be able to pull it from the local repository when they reference it as a dependency. You can, of course, reference vertx-super-s3 as a dependency without building it locally. When you add it as a dependency, the library will be pulled from the central Maven repository if it can't be found in the local repository.

To add vertx-super-s3 as a dependency, add the following to your POM file (supplying the version you'd like to use):

    <dependency>
      <groupId>info.freelibrary</groupId>
      <artifactId>vertx-super-s3</artifactId>
      <version>${vertx.s3.version}</version>
    </dependency>

### Running Tests

By default, the integration tests aren't run. To be able to run the tests, you'll need to create an S3 bucket and authorization credentials that have permission to read and write from/to that bucket. A sample IAM policy is available in the `src/test/resources` directory. You'll need to change the bucket name to something unique since bucket names can not be duplicated in S3.

Once you have the S3 configuration working you'll need to put the authorization information in two places on your local system. This is because vertx-super-s3 tests being able to find the information via the system properties and via the AWS credentials file. You could pass the authorization information on the command line but that's a pain to do each time you want to run the tests.

Instead, to make the information available via the system properties, you should put the following values in your system's Maven [settings.xml](https://maven.apache.org/settings.html) file. You'll want to put them in a profile's system properties section. You can name the profile `s3it` since that's the one you'll use to run the integration tests. If you need to learn more about settings.xml files, read the Maven documentation linked above.

These are the system properties you'll need to put into your settings.xml profile:

    <vertx.s3.bucket>YOUR_S3_BUCKET_NAME</vertx.s3.bucket>
    <vertx.s3.access_key>YOUR_ACCESS_KEY</vertx.s3.access_key>
    <vertx.s3.secret_key>YOUR_SECRET_KEY</vertx.s3.secret_key>

You'll also need to put the authorization information in the system AWS credentials file. By default, this is usually located at `~/.aws/credentials`. To add the expected profile, you'll need to add:

    [vertx-s3]
    aws_secret_access_key = YOUR_SECRET_KEY
    aws_access_key_id = YOUR_ACCESS_KEY

If you do not already have a `default` profile in this credentials file, you will also need to add:

    [default]
    aws_secret_access_key = YOUR_SECRET_KEY
    aws_access_key_id = YOUR_ACCESS_KEY

If you do have a default you don't need to change it's values. It's presence is detected by some of the tests, but they don't care about the values found (i.e., they don't try to use it to communicate with S3).

The test bucket name will be taken from the settings.xml file. Once all this is done, the build can be run with the integration tests by supplying the `s3it` profile name at build time:

    mvn install -Ps3it

### Running the Tests on Travis

To run the tests on Travis, using the supplied .travis.yml configuration file, you'll also need to create some additional S3 buckets, one for each JDK you're testing against. To create these additional buckets, the JDK name from the Travis configuration should be appended onto the name of the default S3 bucket to create the new bucket (e.g. YOUR_S3_BUCKET_NAME-openjdk11). Of course, you'd also need to encrypt your own S3 authorization information.

### Contact

If you have questions about vertx-super-s3 <a href="mailto:ksclarke@ksclarke.io">feel free to ask</a> or, if you encounter a problem, please feel free to [open a ticket](https://github.com/ksclarke/vertx-super-s3/issues "GitHub Issue Queue") in the project's issues queue.
