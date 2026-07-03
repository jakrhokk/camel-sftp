package org.apache.camel.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

class SftpToS3RouteBuilderTest extends CamelTestSupport {

    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {
        Properties properties = new Properties();
        properties.put("app.sftp.uri", "direct:start");
        properties.put("app.s3.uri", "mock:s3");
        properties.put("app.s3.bucket-name", "test-bucket");
        properties.put("app.amqp.uri", "mock:amqp");
        return properties;
    }

    @Override
    protected org.apache.camel.builder.RouteBuilder createRouteBuilder() {
        return new SftpToS3RouteBuilder();
    }

    @Test
    void shouldMarshalAndPublishEventAfterS3Store() throws Exception {
        MockEndpoint s3 = getMockEndpoint("mock:s3");
        MockEndpoint amqp = getMockEndpoint("mock:amqp");

        s3.expectedMessageCount(1);
        s3.expectedHeaderReceived("CamelAwsS3Key", "example.txt.json");
        s3.expectedBodiesReceived("{\"sourceFileName\":\"example.txt\",\"payload\":\"hello world\"}");

        amqp.expectedMessageCount(1);
        amqp.expectedBodiesReceived("{\"bucket\":\"test-bucket\",\"key\":\"example.txt.json\",\"sourceFileName\":\"example.txt\"}");

        NotifyBuilder notify = new NotifyBuilder(context).whenDone(1).create();

        template.sendBodyAndHeader("direct:start", "hello world", Exchange.FILE_NAME, "example.txt");

        assertTrue(notify.matches(5, TimeUnit.SECONDS));
        MockEndpoint.assertIsSatisfied(context);
    }
}
