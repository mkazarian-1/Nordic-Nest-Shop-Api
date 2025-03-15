package org.example.nordicnestshop.service.amazon.s3;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file);

    void deleteFile(String url);

    List<String> uploadFiles(List<MultipartFile> files);

    void deleteFiles(List<String> files);
}
