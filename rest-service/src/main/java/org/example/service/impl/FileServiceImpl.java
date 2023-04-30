package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppDocumentDao;
import org.example.dao.AppPhotoDao;
import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.exceptions.BadRequestException;
import org.example.service.FileService;
import org.example.utils.CryptoTool;
import org.springframework.stereotype.Service;


@Service
@Log4j
public class FileServiceImpl implements FileService {

    private final CryptoTool cryptoTool;
    private final AppPhotoDao appPhotoDao;
    private final AppDocumentDao appDocumentDao;

    public FileServiceImpl(CryptoTool cryptoTool, AppPhotoDao appPhotoDao, AppDocumentDao appDocumentDao) {
        this.cryptoTool = cryptoTool;
        this.appPhotoDao = appPhotoDao;
        this.appDocumentDao = appDocumentDao;
    }


    @Override
    public AppPhoto getPhoto(String hashId) {

        Long id = cryptoTool.idOf(hashId);

        return appPhotoDao.findById(id)
                .orElseThrow(
                        () -> new BadRequestException(String.format("Photo with id = %d not found", id)));

    }

    @Override
    public AppDocument getDocument(String hashId) {

        Long id = cryptoTool.idOf(hashId);

        return appDocumentDao.findById(id)
                .orElseThrow(
                        ()-> new BadRequestException("Document with id = " + id + " not found"));
    }
}
