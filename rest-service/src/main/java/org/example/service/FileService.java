package org.example.service;

import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

import java.util.Optional;

public interface FileService {

    AppPhoto getPhoto(String photoId);

    AppDocument getDocument(String docId);

    FileSystemResource getFileFileSystemResources(BinaryContent binaryContent);
}
