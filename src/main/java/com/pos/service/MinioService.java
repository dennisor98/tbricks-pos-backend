package com.pos.service;


import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
//        ensureBucketExists();
    }

    // ─── Bucket Initialization ────────────────────────────────────────────────

    /**
     * Creates the configured bucket if it does not already exist.
//     */
//    private void ensureBucketExists() {
//        try {
//            boolean exists = minioClient.bucketExists(
//                    BucketExistsArgs.builder()
//                            .bucket(minioProperties.getBucketName())
//                            .build()
//            );
//            if (!exists) {
//                minioClient.makeBucket(
//                        MakeBucketArgs.builder()
//                                .bucket(minioProperties.getBucketName())
//                                .build()
//                );
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to initialize MinIO bucket: " + e.getMessage(), e);
//        }
  //  }

    // ─── Upload ───────────────────────────────────────────────────────────────

    /**
     * Uploads a MultipartFile to MinIO.
     *
     * @param file       the file to upload
     * @param objectName the target object name/path in the bucket (e.g. "images/photo.png")
     * @return the object name on success
     */
    public Object uploadFile(MultipartFile file, String objectName,String bucketName) {
        try (InputStream inputStream = file.getInputStream()) {
            return minioClient.putObject(
                     PutObjectArgs.builder()
                             .bucket(bucketName)
                             .object(objectName)
                             .stream(inputStream, file.getSize(), -1)
                             .contentType(file.getContentType())
                             .build()
             );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file '" + objectName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Uploads raw bytes to MinIO with an explicit content type.
     *
     * @param data        byte array content
     * @param objectName  target object name in the bucket
     * @param contentType MIME type (e.g. "application/json", "text/plain")
     * @return the object name on success
     */
//    public String uploadBytes(byte[] data, String objectName, String contentType) {
//        try (InputStream inputStream = new ByteArrayInputStream(data)) {
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(minioProperties.getBucketName())
//                            .object(objectName)
//                            .stream(inputStream, data.length, -1)
//                            .contentType(contentType)
//                            .build()
//            );
//            return objectName;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to upload bytes to '" + objectName + "': " + e.getMessage(), e);
//        }
//    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    /**
     * Downloads an object and returns it as an InputStream.
     * Caller is responsible for closing the stream.
     *
     * @param objectName the object name/path in the bucket
     * @return InputStream of the object content
     */
    public InputStream readFile(String objectName,String bucketName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file '" + objectName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Downloads an object and returns its content as a byte array.
     *
     * @param objectName the object name/path in the bucket
     * @return byte array of the object content
     */
//    public byte[] readFileAsBytes(String objectName) {
//        try (InputStream stream = readFile(objectName)) {
//            return stream.readAllBytes();
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to read file as bytes '" + objectName + "': " + e.getMessage(), e);
//        }
//    }

    /**
     * Generates a pre-signed URL granting temporary GET access to an object.
     *
     * @param objectName   the object name/path in the bucket
     * @param expiryMinutes how long the URL remains valid
     * @return pre-signed URL string
     */
//    public String generatePresignedUrl(String objectName, int expiryMinutes) {
//        try {
//            return minioClient.getPresignedObjectUrl(
//                    GetPresignedObjectUrlArgs.builder()
//                            .method(Method.GET)
//                            .bucket(minioProperties.getBucketName())
//                            .object(objectName)
//                            .expiry(expiryMinutes, TimeUnit.MINUTES)
//                            .build()
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to generate pre-signed URL for '" + objectName + "': " + e.getMessage(), e);
//        }
//    }

    /**
     * Returns metadata for an object (size, content-type, last modified, etc.).
     *
     * @param objectName the object name/path in the bucket
     * @return StatObjectResponse containing the object's metadata
     */
    public StatObjectResponse getFileMetadata(String objectName,String bucketName) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch metadata for '" + objectName + "': " + e.getMessage(), e);
        }
    }

    // ─── Edit / Replace ───────────────────────────────────────────────────────


    public String renameFile(String sourceObjectName, String targetObjectName,String bucketName) {
        try {
            // Copy to new key
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetObjectName)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(sourceObjectName)
                                            .build()
                            )
                            .build()
            );
            // Remove old key
            deleteFile(sourceObjectName,bucketName);
            return targetObjectName;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to rename '" + sourceObjectName + "' -> '" + targetObjectName + "': " + e.getMessage(), e);
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    /**
     * Deletes a single object from the bucket.
     *
     * @param objectName the object name/path to delete
     */
    public void deleteFile(String objectName,String bucketName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file '" + objectName + "': " + e.getMessage(), e);
        }
    }

    // ─── List ─────────────────────────────────────────────────────────────────

    /**
     * Lists all objects in the bucket under an optional prefix.
     *
     * @param prefix folder-like prefix to filter results (use "" or null for all)
     * @return list of object names
     */
//    public List<String> listFiles(String prefix) {
//        List<String> objectNames = new ArrayList<>();
//        try {
//            Iterable<Result<Item>> results = minioClient.listObjects(
//                    ListObjectsArgs.builder()
//                            .bucket(minioProperties.getBucketName())
//                            .prefix(prefix != null ? prefix : "")
//                            .recursive(true)
//                            .build()
//            );
//            for (Result<Item> result : results) {
//                objectNames.add(result.get().objectName());
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
//        }
//        return objectNames;
//    }

    /**
     * Checks whether an object exists in the bucket.
     *
     * @param objectName the object name/path to check
     * @return true if the object exists, false otherwise
     */
//    public boolean fileExists(String objectName) {
//        try {
//            minioClient.statObject(
//                    StatObjectArgs.builder()
//                            .bucket(minioProperties.getBucketName())
//                            .object(objectName)
//                            .build()
//            );
//            return true;
//        } catch (MinioException e) {
//            return false;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to check existence of '" + objectName + "': " + e.getMessage(), e);
//        }
//    }
}