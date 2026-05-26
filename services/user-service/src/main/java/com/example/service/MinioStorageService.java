package com.example.service;

import com.example.enums.ErrorCode;
import com.example.exception.AppException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "minio.enabled",
        havingValue = "true"
)
public class MinioStorageService {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.presign-expiration}")
    private int presignExpire;

    public String uploadObject(Long userId, MultipartFile file) {
        try {
            validateImage(file);

            bucketExists();

            String extension = getExtension(file.getOriginalFilename());
            String objectName = "users/" + userId + "/avatar-" + UUID.randomUUID() + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new AppException(
                    ErrorCode.UPLOAD_FAILED
            );
        }
    }

    public void deleteObject(String objectName) {
        if (objectName == null) {
            throw new AppException(
                    ErrorCode.FILE_NOT_FOUND
            );
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new AppException(
                    ErrorCode.DELETE_FAILED
            );
        }
    }

    public String getPresignedUrl(String objectName) {
        if (objectName == null) {
            throw new AppException(
                    ErrorCode.FILE_NOT_FOUND
            );
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(presignExpire, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            throw new AppException(
                    ErrorCode.GENERATE_PRESIGNED_URL_FAILED
            );
        }
    }

    private void bucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs
                            .builder()
                            .bucket(bucket)
                            .build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(bucket)
                                .build()
                );
            }
        } catch (Exception e) {
            throw  new AppException(
                    ErrorCode.STORAGE_ERROR
            );
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }
        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp");

        if (!allowedTypes.contains(file.getContentType())) {
            throw new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf("."));
    }

}
