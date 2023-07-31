package de.ude.es.data;

import org.apache.jena.rdf.model.Model;

import java.io.IOException;

public interface ModelHolder {
    Model get(String uri) throws IOException;

    void clearCache();
}
