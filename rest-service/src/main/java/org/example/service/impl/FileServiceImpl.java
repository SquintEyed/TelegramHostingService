package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.example.dao.AppDocumentDao;
import org.example.dao.AppPhotoDao;
import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.entity.BinaryContent;
import org.example.exceptions.BadRequestException;
import org.example.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Log4j
public class FileServiceImpl implements FileService {

    private final AppPhotoDao appPhotoDao;
    private final AppDocumentDao appDocumentDao;

    public FileServiceImpl(AppPhotoDao appPhotoDao, AppDocumentDao appDocumentDao) {
        this.appPhotoDao = appPhotoDao;
        this.appDocumentDao = appDocumentDao;
    }


    @Override
    public AppPhoto getPhoto(String photoId) {

        Long id = Long.parseLong(photoId);

        return appPhotoDao.findById(id)
                .orElseThrow(
                        () -> new BadRequestException(String.format("Photo with id = %d not found",id))
                );

    }

    @Override
    public AppDocument getDocument(String docId) {

        Long id = Long.parseLong(docId);

        return appDocumentDao.findById(id)
                .orElseThrow(
                        ()-> new BadRequestException("Document with id = " + id + " not found")
                );
    }

    @Override
    public FileSystemResource getFileFileSystemResources(BinaryContent binaryContent) {

        try {
            File tempFile = File.createTempFile("tempFile", "bin");
            tempFile.deleteOnExit();
            FileUtils.writeByteArrayToFile(tempFile, binaryContent.getFileAsArrayOfBytes());
            return new FileSystemResource(tempFile);
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
