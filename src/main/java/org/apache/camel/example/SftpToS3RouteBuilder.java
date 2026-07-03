package org.apache.camel.example;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class SftpToS3RouteBuilder extends RouteBuilder {

    @Override
    public void configure() {
        from("{{app.sftp.uri}}")
            .routeId("sftp-to-s3-amqp")
            .process(this::prepareS3Payload)
            .marshal().json(JsonLibrary.Jackson)
            .setHeader("CamelAwsS3Key", exchangeProperty("s3ObjectKey"))
            .to("{{app.s3.uri}}")
            .process(this::prepareEvent)
            .marshal().json(JsonLibrary.Jackson)
            .to("{{app.amqp.uri}}");
    }

    private void prepareS3Payload(Exchange exchange) {
        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME_ONLY, String.class);
        if (fileName == null || fileName.isBlank()) {
            fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
        }
        if (fileName != null && !fileName.isBlank()) {
            fileName = fileName.replace('\\', '/');
            int separatorIndex = fileName.lastIndexOf('/');
            if (separatorIndex >= 0 && separatorIndex + 1 < fileName.length()) {
                fileName = fileName.substring(separatorIndex + 1);
            }
        } else {
            fileName = exchange.getExchangeId();
        }

        exchange.setProperty("sourceFileName", fileName);
        exchange.setProperty("s3ObjectKey", fileName + ".json");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceFileName", fileName);
        payload.put("payload", exchange.getIn().getBody(String.class));

        exchange.getIn().setBody(payload);
    }

    private void prepareEvent(Exchange exchange) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("bucket", exchange.getContext().resolvePropertyPlaceholders("{{app.s3.bucket-name}}"));
        event.put("key", exchange.getProperty("s3ObjectKey", String.class));
        event.put("sourceFileName", exchange.getProperty("sourceFileName", String.class));

        exchange.getIn().setBody(event);
    }
}
