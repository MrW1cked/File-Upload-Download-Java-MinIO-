package com.back.file_upload.controllers;


import com.back.file_upload.models.FileDataDTO;
import com.back.file_upload.services.FileSystemStorageService;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:63342")
public class FileUploadController {

    private final FileSystemStorageService storageService;
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !Objects.equals(file.getContentType(), "application/pdf") || file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file or File is Bigger Than 25Mb");
        }
        try {
            storageService.uploadMinioFile(file);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (id == null) {
            // If the file id is null return not found
            return ResponseEntity.notFound().build();
        }

        Resource file = storageService.downloadMinioFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);

    }

    @GetMapping("/getAll")
    public ResponseEntity<List<FileDataDTO>> getAllFilesByUserName(@RequestParam String userName) {
        return ResponseEntity.ok(storageService.getAllFilesByUsername(userName));
    }
}
