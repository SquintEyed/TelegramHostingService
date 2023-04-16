package org.example.controller;

import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/get-doc/{doc_id}")
    public ResponseEntity<?> getDoc(@PathVariable(name = "doc_id") String docId) {

        AppDocument appDocument = fileService.getDocument(docId);

        FileSystemResource fileSystemResource = fileService.getFileFileSystemResources(appDocument.getBinaryContent());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(appDocument.getMimeType()))
                .header("Content-disposition", "attachment; filename=" + appDocument.getDocName())
                .body(fileSystemResource);
    }

    @GetMapping("/get-photo/{photo_id}")
    public ResponseEntity<?> getPhoto(@PathVariable(name = "photo_id") String photoId) {

        AppPhoto appPhoto = fileService.getPhoto(photoId);

        FileSystemResource fileSystemResource = fileService.getFileFileSystemResources(appPhoto.getBinaryContent());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header("Content-disposition", "attachment")
                .body(fileSystemResource);
    }
}
