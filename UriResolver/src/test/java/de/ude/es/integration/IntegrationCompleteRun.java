package de.ude.es.integration;

import de.ude.es.data.FileLoaderImpl;
import de.ude.es.data.ModelHolderImpl;
import de.ude.es.handling.MessageHandlerImpl;
import de.ude.es.mqttmessagequeue.gateway.MqttGateway;
import de.ude.es.mqttmessagequeue.gateway.MqttGatewayImpl;
import de.ude.es.mqttmessagequeue.message.MessageWithTopic;
import de.ude.es.mqttmessagequeue.message.TopicedMessage;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage;
import de.ude.es.protocolabstraction.rdfconversion.RDFSerializerImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationCompleteRun {
    private static final String BROKER = "localhost:1883";
    private static final String GATEWAY_NAME = "database";
    private static final String STORAGE = ".\\src\\test\\resources\\dataStorage\\";

    @Test
    void integrationCompleteRun() throws IOException {
        String uri = "eip://integration/uri";

        Model result = ModelFactory.createDefaultModel();
        result.add(new ResourceImpl("eip://this"),new PropertyImpl("eip://","is"),"the/result");
        new FileLoaderImpl().createFile(STORAGE, uri, new RDFSerializerImpl().serialize(result).getBytes());

        MessageHandlerImpl handler = new MessageHandlerImpl(new ModelHolderImpl(STORAGE));
        MqttGateway gateway = new MqttGatewayImpl(BROKER, GATEWAY_NAME, handler);

        String test = new RDFSerializerImpl().serialize(new EIPMessage(uri,"sender", EIPMessage.UriType.GET).createModel());
        gateway.receiveMessage(new MessageWithTopic("database",test.getBytes()));

        handler.handleMQTTMessage(handler.getMessageToHandle());

        TopicedMessage actual = gateway.getMessageToPublish();
        assertEquals("sender",actual.getTopic());
        assertTrue(result.isIsomorphicWith(new RDFSerializerImpl().deserialize(new String(actual.getPayload()))));
    }
}
