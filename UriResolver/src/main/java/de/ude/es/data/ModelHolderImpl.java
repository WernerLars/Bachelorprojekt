package de.ude.es.data;

import de.ude.es.protocolabstraction.rdfconversion.RDFSerializer;
import de.ude.es.protocolabstraction.rdfconversion.RDFSerializerImpl;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelHolderImpl implements ModelHolder {
    private RDFSerializer serializer;
    private Map<String, Model> cache;
    private FileLoader loader;

    private String storage = "C://resolver";

    private Logger logger = LoggerFactory.getLogger(ModelHolderImpl.class);

    public ModelHolderImpl() {
        cache = new ConcurrentHashMap<>();
        serializer = new RDFSerializerImpl();
        loader = new FileLoaderImpl();
    }

    public ModelHolderImpl(String storage) {
        this();
        this.storage = storage;
    }

    /**
     * Constructor for Dependency Injection
     **/
    public ModelHolderImpl(Map<String, Model> cache, RDFSerializer serializer, FileLoader loader) {
        this.cache = cache;
        this.serializer = serializer;
        this.loader = loader;
    }

    @Override
    public Model get(String uri) throws IOException {
        if (cache.containsKey(uri)) return cache.get(uri);
        logger.info("cache miss for {}", uri);
        Model response = serializer.deserialize(new String(loader.loadFile(storage, uri)));
        cache.put(uri, response);
        return response;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
