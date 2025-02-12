package org.example.nordicnestshop.service.amazon.s3;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;

public interface S3Service {
    String uploadFile(MultipartFile file);

    void deleteFile(String url);
}
