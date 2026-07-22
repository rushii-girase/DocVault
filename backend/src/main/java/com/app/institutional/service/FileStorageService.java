package com.app.institutional.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private S3Client s3Client;
    private boolean s3Enabled = false;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("document_uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @PostConstruct
    public void init() {
        if (!"disabled".equalsIgnoreCase(bucketName) && !"none".equalsIgnoreCase(accessKey)) {
            try {
                this.s3Client = S3Client.builder()
                        .region(Region.of(awsRegion))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        ))
                        .build();
                this.s3Enabled = true;
                System.out.println("AWS S3 client initialized successfully. Bucket: " + bucketName);
            } catch (Exception e) {
                System.err.println("Failed to initialize AWS S3 client. Falling back to local storage. Error: " + e.getMessage());
            }
        } else {
            System.out.println("AWS S3 is disabled. Falling back to local storage.");
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Generate unique file name
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String storedFileName = UUID.randomUUID().toString() + fileExtension;

            // If S3 is enabled, upload to S3
            if (s3Enabled) {
                try {
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(storedFileName)
                            .contentType(file.getContentType())
                            .build();

                    s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                    return "s3://" + storedFileName;
                } catch (Exception ex) {
                    System.err.println("S3 upload failed. Falling back to local storage. Error: " + ex.getMessage());
                }
            }

            // Fallback: Local Storage
            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return storedFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String storedFileName) {
        try {
            if (storedFileName.startsWith("s3://") && s3Enabled) {
                String s3Key = storedFileName.replace("s3://", "");
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build();
                ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getObjectRequest);
                return new InputStreamResource(s3InputStream) {
                    @Override
                    public String getFilename() {
                        return s3Key;
                    }
                    @Override
                    public long contentLength() {
                        return s3InputStream.response().contentLength();
                    }
                };
            } else {
                // Local storage fallback (or in case S3 starts with s3:// but S3 client is disabled)
                String cleanFileName = storedFileName.startsWith("s3://") ? storedFileName.replace("s3://", "") : storedFileName;
                Path filePath = this.fileStorageLocation.resolve(cleanFileName).normalize();
                if (Files.exists(filePath)) {
                    return new UrlResource(filePath.toUri());
                } else {
                    throw new RuntimeException("File not found " + storedFileName);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found " + storedFileName, ex);
        }
    }
}
