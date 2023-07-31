package de.ude.es.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileLoaderImpl implements FileLoader {
    private Logger logger = LoggerFactory.getLogger(FileLoaderImpl.class);

    @Override
    public byte[] loadFile(String storage, String uri) throws IOException {
        uri = uri.replace("://","/");
        Path file = Paths.get(storage, uri, "storage.data");
        logger.debug("loaded file from {}",file.toUri().toURL().toString());
        return Files.readAllBytes(file);
    }

    @Override
    public void createFile(String storage, String uri, byte[] toStore) throws IOException {
        uri = uri.replace("://","/");
        Path storagePath = Paths.get(storage, uri);
        Files.createDirectories(storagePath);
        storagePath = storagePath.resolve("storage.data");
        Files.write(storagePath, toStore);
    }
}
