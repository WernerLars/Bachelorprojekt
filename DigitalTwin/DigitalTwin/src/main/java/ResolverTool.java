import de.ude.es.protocolabstraction.rdfconversion.RDFSerializerImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ResolverTool {

    public static void main(String[] args) {
        URI2Mappings[] uris2Mappings = new URI2Mappings[]{new URI2Mappings("userTwin", "/receiveData"),
                new URI2Mappings("decoderTwin", "/decode"),
                new URI2Mappings("iotDeviceTwin1", "/requestData"),
                new URI2Mappings("iotDeviceTwin2", "/requestData"),
                new URI2Mappings("iotDeviceTwin3", "/requestData"),
                new URI2Mappings("iotDeviceTwin4", "/requestData"),
                new URI2Mappings("iotDeviceTwin5", "/requestData"),
                new URI2Mappings("area1compositeTwin", "/requestData", "/requestChildData", "/receiveIOTDeviceData"),
                new URI2Mappings("iotDeviceTwin6", "/requestData"),
                new URI2Mappings("iotDeviceTwin7", "/requestData"),
                new URI2Mappings("iotDeviceTwin8", "/requestData"),
                new URI2Mappings("iotDeviceTwin9", "/requestData"),
                new URI2Mappings("iotDeviceTwin10", "/requestData"),
                new URI2Mappings("area2compositeTwin", "/requestData", "/requestChildData", "/receiveIOTDeviceData"),
                new URI2Mappings("iotDeviceTwin11", "/requestData"),
                new URI2Mappings("iotDeviceTwin12", "/requestData"),
                new URI2Mappings("iotDeviceTwin13", "/requestData"),
                new URI2Mappings("iotDeviceTwin14", "/requestData"),
                new URI2Mappings("iotDeviceTwin15", "/requestData"),
                new URI2Mappings("area3compositeTwin", "/requestData", "/requestChildData", "/receiveIOTDeviceData")};

        for (URI2Mappings uri2Mappings : uris2Mappings) {
            createMapping(uri2Mappings);
        }
    }

    private static void createMapping(URI2Mappings uri2Mappings) {
        for (String mapping : uri2Mappings.getMappings()) {
            if (mapping.equals("/requestChildData")) {
                createAreaTwinMapppedToIoTDevicesMapping(uri2Mappings.getUri());
            } else {
                Model model = ModelFactory.createDefaultModel();
                String twinPath = "eip://" + uri2Mappings.getUri() + mapping;
                RDFNode[] content = {model.createLiteral(twinPath)};
                model.add(new ResourceImpl(twinPath), new PropertyImpl("eip://", "mappedTo"),
                        model.createList(content));
                saveToFile(model, twinPath.replaceFirst("://", "/"));
            }
        }
    }

    private static void createAreaTwinMapppedToIoTDevicesMapping(String uri) {
        Model model = ModelFactory.createDefaultModel();
        String mapping = "/requestChildData";

        int areaTwinCode = Integer.valueOf(uri.replaceAll("\\D+", "")).intValue();
        String twinPath = "eip://" + uri + mapping;

        RDFNode[] content = new RDFNode[5];
        int index = 0;
        for (int i = (areaTwinCode - 1) * 5 + 1; i < (areaTwinCode - 1) * 5 + 6; i++) {
            content[index] = model.createLiteral("eip://iotDeviceTwin" + i + "/requestData");
            index++;
        }
        model.add(new ResourceImpl(twinPath), new PropertyImpl("eip://", "mappedTo"), model.createList(content));
        saveToFile(model, twinPath.replaceFirst("://", "/"));
    }

    public static void saveToFile(Model model, String path) {
        RDFSerializerImpl serialiser = new RDFSerializerImpl();
        String jsonString = serialiser.serialize(model);
        String localStoragePath = "C://resolver";
        Path storagePath = Paths.get(localStoragePath, path);
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        storagePath = storagePath.resolve("storage.data");
        try {
            Files.write(storagePath, jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class URI2Mappings {
        private final String uri;
        private final List<String> mappings = new ArrayList<String>();

        URI2Mappings(String uri, String... mappings) {
            this.uri = uri;
            if (mappings != null) {
                for (String mapping : mappings) {
                    this.mappings.add(mapping);
                }
            }
        }

        public List<String> getMappings() {
            return mappings;
        }

        public String getUri() {
            return uri;
        }
    }
}
