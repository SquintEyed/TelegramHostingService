package org.example.controller;

import lombok.extern.log4j.Log4j;
import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

@Log4j
@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/get-doc/{doc_id}")
    public void getDoc(@PathVariable(name = "doc_id") String docId, HttpServletResponse response) {

        AppDocument appDocument = fileService.getDocument(docId);

        if (Objects.isNull(appDocument)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.setContentType(MediaType.parseMediaType(appDocument.getMimeType()).toString());
        response.setHeader("Content-disposition","attachment; filename=" + appDocument.getDocName());
        response.setStatus(HttpServletResponse.SC_OK);

        try {
            OutputStream output = response.getOutputStream();
            output.write(appDocument.getBinaryContent().getFileAsArrayOfBytes());
            output.close();
        }

        catch (IOException ex) {
            log.error(ex);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-photo/{photo_id}")
    public void getPhoto(@PathVariable(name = "photo_id") String photoId, HttpServletResponse response) {

        AppPhoto appPhoto = fileService.getPhoto(photoId);

        if (Objects.isNull(response)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.setHeader("Content-disposition", "attachment;");
        response.setContentType(MediaType.IMAGE_JPEG.toString());
        response.setStatus(HttpServletResponse.SC_OK);

        try {
            OutputStream output = response.getOutputStream();
            output.write(appPhoto.getBinaryContent().getFileAsArrayOfBytes());
            output.close();
        }

        catch (IOException ex) {
            log.error(ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
