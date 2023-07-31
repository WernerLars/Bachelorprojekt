package de.ude.es.query;

import de.ude.es.data.ModelHolder;
import de.ude.es.handling.MessageHandlerImpl;
import de.ude.es.mqttmessagequeue.gateway.MqttGateway;
import de.ude.es.mqttmessagequeue.message.MessageWithTopic;
import de.ude.es.mqttmessagequeue.message.TopicedMessage;
import de.ude.es.protocolabstraction.rdfconversion.RDFSerializer;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestQueryResolution {
    private MessageHandlerImpl handler;
    private RDFSerializer serializer;
    private ModelHolder holder;
    private MqttGateway gateway;

    @BeforeEach
    void setUp() {
        serializer = mock(RDFSerializer.class);
        holder = mock(ModelHolder.class);
        gateway = mock(MqttGateway.class);
        handler = new MessageHandlerImpl(serializer, holder);
        handler.setMQTTGateway(gateway);
    }

    @Test
    void testReadUriFromRequest() throws IOException {
        EIPMessage eipMessage = new EIPMessage("eip://function/uri", "eip://function/sender", EIPMessage.UriType.GET, EIPMessage.parameters());

        Model response = ModelFactory.createDefaultModel();
        when(serializer.deserialize(any())).thenReturn(eipMessage.createModel());
        when(serializer.serialize(any())).thenReturn("empty");
        when(holder.get(anyString())).thenReturn(response);

        handler.handleMQTTMessage(new MessageWithTopic("topic","byte".getBytes()));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(holder).get(argument.capture());
        assertEquals("eip://function/uri",argument.getValue());
    }

    @Test
    void testSendResponse() throws IOException {
        EIPMessage eipMessage = new EIPMessage("eip://function/uri", "eip://function/sender", EIPMessage.UriType.GET, EIPMessage.parameters());
        Model response = ModelFactory.createDefaultModel();

        when(serializer.deserialize(any())).thenReturn(eipMessage.createModel());
        when(serializer.serialize(any())).thenReturn("data");
        when(holder.get(any())).thenReturn(response);

        handler.handleMQTTMessage(new MessageWithTopic("topic","byte".getBytes()));

        ArgumentCaptor<TopicedMessage> argument = ArgumentCaptor.forClass(TopicedMessage.class);
        verify(gateway).publishMessage(argument.capture());
        assertArrayEquals("data".getBytes(),argument.getValue().getPayload());
    }

    @Test
    void testHolderNotFindingFile() throws IOException {
        EIPMessage eipMessage = new EIPMessage("eip://function/uri", "eup://function/sender", EIPMessage.UriType.GET, EIPMessage.parameters());
        when(serializer.deserialize(any())).thenReturn(eipMessage.createModel());
        when(holder.get(anyString())).thenThrow(new IOException());

        handler.handleMQTTMessage(new MessageWithTopic("topic", "byte".getBytes()));
    }
}
