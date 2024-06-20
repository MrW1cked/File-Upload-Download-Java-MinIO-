package com.back.file_upload.services;


import com.back.file_upload.config.StorageProperties;
import com.back.file_upload.exceptions.StorageException;
import com.back.file_upload.models.FileDataDTO;
import com.back.file_upload.repositories.FileDataRepository;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSystemStorageService {

    private final Path rootLocation;
    private final FileDataRepository fileDataRepository;
    private final static String FILE_EXTENSION_PDF = ".pdf";
    private final static String MINIO_BUCKET_NAME = System.getenv("MINIO_BUCKET_NAME");
    private final static String MINIO_ENDPOINT = System.getenv("MINIO_ENDPOINT");
    private final static String MINIO_ACCESS_KEY = System.getenv("MINIO_ACCESS_KEY");
    private final static String MINIO_SECRET_KEY = System.getenv("MINIO_SECRET_KEY");

    @Autowired
    public FileSystemStorageService(StorageProperties properties, FileDataRepository fileDataRepository) {
        this.fileDataRepository = fileDataRepository;

        if (properties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location can not be Empty.");
        }

        this.rootLocation = Paths.get(properties.getLocation());
    }

    private void createBucket(MinioClient minioClient) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // Check if the bucket already exists.
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(MINIO_BUCKET_NAME).build());

        // If the bucket does not exist, we create it.
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(MINIO_BUCKET_NAME).build());
        } else {
            // If the bucket already exists, we log this information.
            log.info("Bucket exists! Continue with the existing bucket...");
        }
    }

    private MinioClient createMinioClientAndConnectToBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // Create a minioClient with the MinIO server playground, its access key and secret key.
        MinioClient minioClient = MinioClient.builder()
                .endpoint(MINIO_ENDPOINT)
                .credentials(MINIO_ACCESS_KEY, MINIO_SECRET_KEY)
                .build();

        // Create a bucket if it does not exist.
        createBucket(minioClient);

        return minioClient;
    }

    public Resource downloadMinioFile(String fileId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // Create a minioClient with the MinIO server playground, its access key and secret key.
        MinioClient minioClient = createMinioClientAndConnectToBucket();

        // Check if the file exists using the bucket name and the file name.
        Optional<FileDataDTO> optionalFileDataDTO = fileDataRepository.findById(fileId);
        if (optionalFileDataDTO.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }

        FileDataDTO FileDataDTO = optionalFileDataDTO.get();
        String fileDownloadName = FileDataDTO.getId() + FILE_EXTENSION_PDF;

        deleteFileInTemporaryStorageIfExists(fileDownloadName);

        minioClient.downloadObject(DownloadObjectArgs.builder()
                .bucket(MINIO_BUCKET_NAME)
                .object(fileDownloadName)
                .filename(rootLocation + "/" + fileDownloadName)
                .build());

        Path file = load(fileId + FILE_EXTENSION_PDF);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable()) {
            setFileAsSeenByUser(optionalFileDataDTO.get());
            return resource;
        } else {
            throw new FileNotFoundException(
                    "Could not read file: " + fileId);
        }
    }

    public void uploadMinioFile(MultipartFile file) {
        try {
            MinioClient minioClient = createMinioClientAndConnectToBucket();

            String fileUUIDName = generateRandomFileName();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(MINIO_BUCKET_NAME)
                            .contentType(file.getContentType())
                            .object(fileUUIDName + ".pdf") //object
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());

            createFileDataAndSaveInDatabase(fileUUIDName, file.getOriginalFilename());

            log.info("File uploaded successfully");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateRandomFileName() {
        return UUID.randomUUID().toString();
    }

    private void createFileDataAndSaveInDatabase(String fileId, String fileName) {

        FileDataDTO fileDataDTO = FileDataDTO.builder()
                .id(fileId)
                .username("ADMIN") // This is a hardcoded value, you should replace it with the actual username
                .fileName(fileName)
                .uploadTime(LocalDateTime.now())
                .seenByUser(false)
                .build();
        fileDataRepository.save(fileDataDTO);
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    private void setFileAsSeenByUser(FileDataDTO FileDataDTO) {
        FileDataDTO.setSeenByUser(true);
        fileDataRepository.save(FileDataDTO);
    }

    public void deleteFileInTemporaryStorageIfExists(String fileDownloadName) throws IOException {
        Path path = Paths.get(rootLocation + "/" + fileDownloadName);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    public List<FileDataDTO> getAllFilesByUsername(String username) {
        return fileDataRepository.findAllByUsername(username);
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
