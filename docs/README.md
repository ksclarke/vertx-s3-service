# Getting Started Using Vertx-S3-Service

The [project's tests](https://github.com/ksclarke/vertx-super-s3/tree/master/src/test/java/info/freelibrary/vertx/s3) are a good place to looks for examples of how to use vertx-super-s3's S3Client. A simple (somewhat artificial) example, though, is also given below:

```
final S3Client s3Client = new S3Client(vertx(), new S3ClientOptions("vertx-s3"));
final String fileName = "ucla-library-logo.png";
final String bucket = "presentation-materials";
final String localPath = Paths.get(System.getProperty("java.io.tmpdir"), fileName).toString();

// Download a file from an S3 bucket and write it to our temporary files directory
s3Client.get(bucket, fileName)
    .compose(response -> vertx().fileSystem().open(localPath, new OpenOptions())
    .compose(file -> response.pipeTo(file)
    .onSuccess(result -> {
        LOGGER.info("Successfully downloaded S3 object to: {}", path);
    }).onFailure(error -> {
        LOGGER.error(error, error.getMessage());
    })));
```

More examples coming!
