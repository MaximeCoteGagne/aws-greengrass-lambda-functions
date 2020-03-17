package com.awslabs.aws.iot.greengrass.cdd.communication;

import com.amazonaws.greengrass.javasdk.IotDataClient;
import com.amazonaws.greengrass.javasdk.LambdaClient;
import com.amazonaws.greengrass.javasdk.model.*;
import com.awslabs.aws.iot.greengrass.cdd.helpers.JsonHelper;
import com.awslabs.aws.iot.greengrass.cdd.providers.interfaces.EnvironmentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class GreengrassCommunication implements Communication {
    private final Logger log = LoggerFactory.getLogger(GreengrassCommunication.class);
    private static final String EMPTY_CUSTOM_CONTEXT = "{}";
    private static final String ENCODED_EMPTY_CUSTOM_CONTEXT = Base64.getEncoder().encodeToString(EMPTY_CUSTOM_CONTEXT.getBytes());
    @Inject
    IotDataClient iotDataClient;
    @Inject
    LambdaClient lambdaClient;
    @Inject
    EnvironmentProvider environmentProvider;
    @Inject
    JsonHelper jsonHelper;

    @Inject
    public GreengrassCommunication() {
    }

    @Override
    public void publish(String topic, Object object) throws GGIotDataException, GGLambdaException {
        try {
            log.info("Publish object 1");
            PublishRequest publishRequest = new PublishRequest().withTopic(topic).withPayload(ByteBuffer.wrap(jsonHelper.toJson(object).getBytes()));
            log.info("Publish object 2");

            iotDataClient.publish(publishRequest);
            log.info("Publish object 3");
        } catch (Exception e) {
            log.info("Publish object exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void publish(String topic, byte[] bytes) throws GGIotDataException, GGLambdaException {
        PublishRequest publishRequest = new PublishRequest().withTopic(topic).withPayload(ByteBuffer.wrap(bytes));

        iotDataClient.publish(publishRequest);
    }

    @Override
    public Optional<byte[]> invokeByName(String functionName, Optional<Map> customContext, byte[] binaryData) {
        Optional<String> functionArn = environmentProvider.getLocalLambdaArn(functionName);

        if (!functionArn.isPresent()) {
            log.error("Attempted to invoke Lambda by name [" + functionName + "] but its ARN is not in the environment");
            return Optional.empty();
        }

        String encodedClientContext = ENCODED_EMPTY_CUSTOM_CONTEXT;

        if (customContext.isPresent()) {
            encodedClientContext = Base64.getEncoder().encodeToString(jsonHelper.toJson(customContext.get()).getBytes());
        }

        final InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionArn(functionArn.get())
                .withInvocationType(InvocationType.RequestResponse)
                .withClientContext(encodedClientContext)
                .withPayload(ByteBuffer.wrap(binaryData));

        try {
            final InvokeResponse response;
            response = lambdaClient.invoke(invokeRequest);
            final byte[] bytes = response.getPayload().array();

            return Optional.ofNullable(bytes);
        } catch (GGLambdaException e) {
            log.error("Lambda invoke failed [" + e.getMessage() + "]");
            return Optional.empty();
        }
    }
}
