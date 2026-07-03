# camel-sftp

Apache Camel service that:

1. Consumes files from an SFTP endpoint.
2. Marshals each file into JSON and stores it in S3.
3. Publishes an AMQP event (for RabbitMQ) with bucket/key metadata.

## Build and test

```bash
mvn test
```

## Runtime configuration

Default properties are in `/home/runner/work/camel-sftp/camel-sftp/src/main/resources/application.properties`:

- `app.sftp.uri` - SFTP consumer endpoint
- `app.s3.uri` - S3 producer endpoint
- `app.s3.bucket-name` - bucket name used in event payload
- `app.amqp.uri` - AMQP destination for RabbitMQ
