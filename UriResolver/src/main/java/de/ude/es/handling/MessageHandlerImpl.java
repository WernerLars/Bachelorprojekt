package de.ude.es.handling;

import de.ude.es.data.ModelHolder;
import de.ude.es.mqttmessagequeue.handling.AbstractMqttMessageHandler;
import de.ude.es.mqttmessagequeue.message.MessageWithTopic;
import de.ude.es.mqttmessagequeue.message.TopicedMessage;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage;
import de.ude.es.protocolabstraction.rdfconversion.RDFSerializer;
import de.ude.es.protocolabstraction.rdfconversion.RDFSerializerImpl;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessageHandlerImpl extends AbstractMqttMessageHandler implements MessageHandler {
    private RDFSerializer serializer;
    private ModelHolder holder;

    private Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);

    public MessageHandlerImpl(RDFSerializer serializer, ModelHolder holder) {
        this.serializer = serializer;
        this.holder = holder;
    }

    public MessageHandlerImpl(ModelHolder holder) {
        this.holder = holder;
        this.serializer = new RDFSerializerImpl();
    }

    @Override
    public void handleMQTTMessage(TopicedMessage messageToHandle) {
        try {
            Model request = serializer.deserialize(new String(messageToHandle.getPayload()));

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            request.write(stream, "TURTLE");
            EIPMessage eipMessage = new EIPMessage(request);
            logger.info("received request for {} from {}", eipMessage.value, eipMessage.sender);
            logger.debug("received model: {}", new String(stream.toByteArray()));
            try {
                Model response = holder.get(eipMessage.value);
                send(response, eipMessage.sender);

                stream.reset();
                request.write(stream, "TURTLE");
                logger.info("send response to {}", eipMessage.sender);
                logger.debug("responded with", stream.toByteArray());
            } catch (IOException e) {
                logger.error("File not found for given Uri: " + eipMessage.value);
                logger.debug("", e);
            }
        } catch (Exception e) {
            logger.error("exception occurred handling message", e);
        }
    }

    @Override
    public void send(Model model, String receiverURI) {
        String json = serializer.serialize(model);
        publishMessage(new MessageWithTopic(receiverURI, json.getBytes()));
    }
}