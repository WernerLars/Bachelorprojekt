package de.ude.es.databaseTwin;

import de.ude.es.data.ModelHolderImpl;
import de.ude.es.handling.MessageHandlerImpl;
import de.ude.es.mqttmessagequeue.gateway.MqttGateway;
import de.ude.es.mqttmessagequeue.gateway.MqttGatewayImpl;
import de.ude.es.mqttmessagequeue.message.MessageWithTopic;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage;
import de.ude.es.protocolabstraction.rdfconversion.RDFSerializerImpl;

public class StartDatabaseTwin {
    private static final String BROKER = "localhost:1883";
    private static final String GATEWAY_NAME = "database";

    public static void main(String[] args) {
        MessageHandlerImpl handler = new MessageHandlerImpl(new ModelHolderImpl());
        MqttGateway gateway = new MqttGatewayImpl(BROKER, GATEWAY_NAME, handler);
        gateway.startMqtt();
        gateway.subscribeToTopic(GATEWAY_NAME);


        String test = new RDFSerializerImpl().serialize(new EIPMessage("eip/test/uri","sender", EIPMessage.UriType.GET).createModel());
        gateway.receiveMessage(new MessageWithTopic("database",test.getBytes()));
    }
}
