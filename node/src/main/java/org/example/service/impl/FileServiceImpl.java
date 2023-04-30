package org.example.service.impl;

import org.example.dao.AppDocumentDao;
import org.example.dao.AppPhotoDao;
import org.example.dao.BinaryContentDao;
import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.entity.BinaryContent;
import org.example.enums.LinkType;
import org.example.exception.UploadFileException;
import org.example.service.FileService;
import org.example.utils.CryptoTool;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


@Service
public class FileServiceImpl implements FileService {

    @Value("${token}")
    private String token;

    @Value("${link.address}")
    private String linkAddress;

    @Value("${service.file_info.uri}")
    private String fileInfoUri;

    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private final CryptoTool cryptoTool;
    private final AppPhotoDao appPhotoDao;
    private final AppDocumentDao appDocumentDao;
    private final BinaryContentDao binaryContentDao;

    public FileServiceImpl(CryptoTool cryptoTool,
                           AppPhotoDao appPhotoDao,
                           AppDocumentDao appDocumentDao,
                           BinaryContentDao binaryContentDao) {

        this.cryptoTool = cryptoTool;
        this.appPhotoDao = appPhotoDao;
        this.appDocumentDao = appDocumentDao;
        this.binaryContentDao = binaryContentDao;
    }


    @Override
    public AppPhoto processPhoto(Message externalMessage) {

        int photoSizeCount = externalMessage.getPhoto().size();

        int photoIndex = photoSizeCount > 1 ? photoSizeCount - 1 : 0;

        PhotoSize telegramPhoto = externalMessage.getPhoto().get(photoIndex);

        String fieldId = telegramPhoto.getFileId();

        ResponseEntity<String> response = getFilePath(fieldId);

        if (response.getStatusCode() == HttpStatus.OK) {

            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);

            AppPhoto transientAppPhoto = AppPhoto.builder()
                    .binaryContent(persistentBinaryContent)
                    .fileSize(telegramPhoto.getFileSize())
                    .telegramFileId(telegramPhoto.getFileId())
                    .build();

            return appPhotoDao.save(transientAppPhoto);
        }

        throw new UploadFileException("Bad response from telegram service " + response);
    }

    @Override
    public AppDocument processDoc(Message externalMessage) {

        Document telegramDoc = externalMessage.getDocument();

        String fileId = telegramDoc.getFileId();

        ResponseEntity<String> response = getFilePath(fileId);

        if (response.getStatusCode() == HttpStatus.OK) {

            BinaryContent persistBinaryContent = getPersistentBinaryContent(response);

            AppDocument transientAppDocument = AppDocument.builder()
                    .docName(telegramDoc.getFileName())
                    .telegramFileId(telegramDoc.getFileId())
                    .mimeType(telegramDoc.getMimeType())
                    .fileSize(telegramDoc.getFileSize())
                    .binaryContent(persistBinaryContent)
                    .build();

            return appDocumentDao.save(transientAppDocument);
        }

        throw new UploadFileException("Bad response from telegram service " + response);
    }

    @Override
    public String generatedLink(Long docId, LinkType linkType) {

        String prefixLink = cryptoTool.hashOf(docId);

        return "http://" + linkAddress + "/" + linkType + prefixLink;

    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {

        JSONObject jsonObject = new JSONObject(response.getBody());
        String filePath = jsonObject
                .getJSONObject("result")
                .getString("file_path");

        byte[] fileBytes = downloadFile(filePath);

        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileBytes)
                .build();

        return binaryContentDao.save(transientBinaryContent);
    }

    private byte[] downloadFile(String fiePath) {

        String finalUri = fileStorageUri.replace("{token}", token).replace("{filePath}", fiePath);

        URL urlObj = null;

        int i;

        ArrayList<Integer> ints = new ArrayList<>();

        try {
            urlObj = new URL(finalUri);
        }

        catch(MalformedURLException ex) {
            throw new UploadFileException(ex.getMessage());
        }

        try {
            InputStream input = urlObj.openStream();

            while ((i = input.read())!= -1) {
                ints.add(i);
            }

            byte [] finalBytes= new byte[ints.size()];

            for (int j = 0; j < ints.size(); j++) {
                finalBytes[j] = (byte)(int)(ints.get(j));
            }

            return  finalBytes;
        }

        catch (IOException ex) {
            throw new UploadFileException(ex.getMessage());
        }
    }

    private ResponseEntity<String> getFilePath(String fileId) {

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> request = new HttpEntity<>(new HttpHeaders());

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token,
                fileId
        );
    }
}
