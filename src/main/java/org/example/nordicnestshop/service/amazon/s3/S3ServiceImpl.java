package org.example.nordicnestshop.service.amazon.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.example.nordicnestshop.exception.IncorrectArgumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private static final Pattern S3_PATTERN = Pattern.compile("https://([^.]+)\\.s3\\.amazonaws\\.com/(.+)");
    private static final int KEY_INDEX = 2;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {
        log.info("Starting file upload: {}", file.getOriginalFilename());

        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (!allowedExtensions.contains(fileExtension)) {
            log.warn("File type not allowed: {}", fileExtension);
            throw new IncorrectArgumentException("Invalid file type. Only JPG, JPEG, PNG, and WEBP are allowed.");
        }

        String uniqueFileName = UUID.randomUUID() + ".webp";
        log.info("Generated unique filename: {}", uniqueFileName);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("company", "Nordic");
        metadata.put("environment", "development");

        String contentType = "image/webp";
        String contentDisposition = "inline";

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(uniqueFileName)
                            .metadata(metadata)
                            .contentType(contentType)
                            .contentDisposition(contentDisposition)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String fileUrl = generateUrl(bucketName, uniqueFileName);
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public void deleteFile(String url) {
        log.info("Starting file deletion: {}", url);

        Matcher matcher = S3_PATTERN.matcher(url);
        if (!matcher.matches()) {
            log.error("Invalid S3 URL format: {}", url);
            throw new IncorrectArgumentException("Invalid S3 URL format");
        }
        String keyName = matcher.group(KEY_INDEX);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", url);
        } catch (S3Exception e) {
            log.error("Failed to delete file: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    private String generateUrl(String bucketName, String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }
}
