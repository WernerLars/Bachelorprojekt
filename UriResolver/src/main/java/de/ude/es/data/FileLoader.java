package de.ude.es.data;

import java.io.IOException;

public interface FileLoader {
    byte[] loadFile(String storage, String uri) throws IOException;

    void createFile(String storage, String uri, byte[] toStore) throws IOException;
}
