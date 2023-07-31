package de.ude.es.query;

import de.ude.es.data.FileLoader;
import de.ude.es.data.ModelHolder;
import de.ude.es.data.ModelHolderImpl;
import de.ude.es.protocolabstraction.rdfconversion.RDFSerializer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestModelLoading {
    private Map<String, Model> cache;
    private RDFSerializer serializer;
    private ModelHolder holder;
    private FileLoader loader;

    @BeforeEach
    void setUp() {
        cache = new ConcurrentHashMap<>();
        serializer = mock(RDFSerializer.class);
        loader = mock(FileLoader.class);

        holder = new ModelHolderImpl(cache, serializer, loader);
    }

    @Test
    void testFileFromCache() throws IOException {
        String uri = "eip://test/function";
        Model model = ModelFactory.createDefaultModel();
        model.add(new ResourceImpl("resources"), new PropertyImpl("test"), new ResourceImpl("lower"));

        cache.put(uri, model);
        Model result = holder.get(uri);
        assertTrue(result.isIsomorphicWith(model));
    }

    @Test
    void testLoadingFile() throws IOException {
        String uri = "eip://test/function";
        Model model = ModelFactory.createDefaultModel();
        model.add(new ResourceImpl("resources"), new PropertyImpl("test"), new ResourceImpl("lower"));

        when(loader.loadFile(any(), any())).thenReturn(new byte[]{20, 20});
        when(serializer.deserialize(any())).thenReturn(model);
        Model result = holder.get(uri);
        assertTrue(result.isIsomorphicWith(model));
    }

    @Test
    void testLoaderNotFindingFile() throws IOException {
        when(loader.loadFile(any(), any())).thenThrow(new IOException());
        assertThrows(IOException.class, () -> holder.get(""));
    }
}
