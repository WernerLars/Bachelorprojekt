package de.ude.es.storage;

import de.ude.es.data.FileLoaderImpl;
import de.ude.es.protocolabstraction.rdfconversion.RDFSerializerImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPersistentStorage {
    private Model testModel;

    private String storage = ".\\src\\test\\resources\\dataStorage\\";

    @BeforeEach
    void setUp() {
        testModel = ModelFactory.createDefaultModel();
        testModel.add(new ResourceImpl("http://this/is/uri"), new PropertyImpl("http://hasValue"),"konichiwa");
    }

    @Test
    void createTestFile() throws IOException {
        String data = new RDFSerializerImpl().serialize(testModel);
        new FileLoaderImpl().createFile(storage, "test/creation/uri/function", data.getBytes());
    }

    @Test
    void testCreateFileWithNamespace() throws IOException {
        String data = new RDFSerializerImpl().serialize(testModel);
        new FileLoaderImpl().createFile(storage, "eip://test/creation/uri/function", data.getBytes());
    }

    @Test
    void testLoadFile() throws IOException {
        byte[] data = {50,60,70,80,90};
        String uri = "eip://test/loading/uri/function";
        new FileLoaderImpl().createFile(storage, uri , data);
        byte[] result = new FileLoaderImpl().loadFile(storage, uri);

        assertArrayEquals(data,result);
    }

}
