package de.ude.es.handling;

import org.apache.jena.rdf.model.Model;

public interface MessageHandler {
    void send(Model model, String receiverURI);
}
