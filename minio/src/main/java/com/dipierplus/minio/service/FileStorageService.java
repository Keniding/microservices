package com.dipierplus.minio.service;

import com.dipierplus.minio.dto.FileUrlResponse;
import com.dipierplus.minio.exception.FileStorageException;
import com.dipierplus.minio.utils.ContentTypePrefix;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public Map<String, Object> uploadFile(MultipartFile file, String objectId) throws FileStorageException {
        if (isValidObjectId(objectId)) {
            throw new FileStorageException("ID inválido. Debe ser un ObjectId válido de 24 caracteres hexadecimales.");
        }

        try {
            String fileName = generateFileName(file, objectId);
            String md5Hash = calculateMD5(file);

            String duplicateFile = checkDuplicate(md5Hash);
            if (duplicateFile != null) {
                return createDuplicateResponse(duplicateFile);
            }

            Map<String, String> userMetadata = new HashMap<>();
            userMetadata.put("hash_md5", md5Hash);
            userMetadata.put("nombre_original", file.getOriginalFilename());
            userMetadata.put("fecha_subida", LocalDateTime.now().toString());
            userMetadata.put("tamanio", String.valueOf(file.getSize()));
            userMetadata.put("mime_type", file.getContentType());
            userMetadata.put("object_id", objectId);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .userMetadata(userMetadata)
                            .build());

            log.info("Archivo subido exitosamente: {} con ObjectId: {}", fileName, objectId);
            return createSuccessResponse(fileName, userMetadata);
        } catch (Exception e) {
            log.error("Error al almacenar el archivo", e);
            throw new FileStorageException("No se pudo almacenar el archivo", e);
        }
    }

    private String generateFileName(MultipartFile file, String objectId) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        String originalFileName = file.getOriginalFilename();
        assert originalFileName != null;
        String fileNameWithoutExtension = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));

        String cleanFileName = fileNameWithoutExtension.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        cleanFileName = cleanFileName.length() > 20 ? cleanFileName.substring(0, 20) : cleanFileName;

        return ContentTypePrefix.getPrefix(file.getContentType())
                + timestamp
                + "_"
                + cleanFileName
                + "_"
                + objectId
                + extension;
    }

    private boolean isValidObjectId(String objectId) {
        return objectId == null ||
                objectId.length() != 24 ||
                !objectId.matches("[a-fA-F0-9]{24}");
    }

    private String calculateMD5(MultipartFile file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(file.getBytes());
            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Error al calcular MD5", e);
        }
    }

    private String checkDuplicate(String md5Hash) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                StatObjectResponse stat = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(item.objectName())
                                .build());

                String storedHash = stat.userMetadata().get("hash_md5");
                if (md5Hash.equals(storedHash)) {
                    return item.objectName();
                }
            }
        } catch (Exception e) {
            log.error("Error al buscar duplicados", e);
        }
        return null;
    }

    private Map<String, Object> createSuccessResponse(String fileName, Map<String, String> metadata) {
        Map<String, Object> response = new HashMap<>();
        response.put("estado", "exitoso");
        response.put("mensaje", "Archivo subido correctamente");
        response.put("nombreArchivo", fileName);
        response.put("metadata", metadata);
        return response;
    }

    private Map<String, Object> createDuplicateResponse(String existingFileName) {
        Map<String, Object> response = new HashMap<>();
        response.put("estado", "duplicado");
        response.put("mensaje", "El archivo ya existe");
        response.put("nombreArchivo", existingFileName);
        return response;
    }

    public List<FileUrlResponse> getFilesByObjectId(String objectId) throws FileStorageException {
        if (isValidObjectId(objectId)) {
            throw new FileStorageException("ID inválido. Debe ser un ObjectId válido de 24 caracteres hexadecimales.");
        }

        try {
            List<FileUrlResponse> responses = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                try {
                    Item item = result.get();
                    StatObjectResponse stat = minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(item.objectName())
                                    .build());

                    String fileObjectId = stat.userMetadata().get("object_id");
                    if (fileObjectId != null && fileObjectId.equals(objectId)) {
                        String url = minioClient.getPresignedObjectUrl(
                                GetPresignedObjectUrlArgs.builder()
                                        .bucket(bucketName)
                                        .object(item.objectName())
                                        .method(Method.GET)
                                        .expiry(1, TimeUnit.HOURS)
                                        .build());

                        responses.add(FileUrlResponse.builder()
                                .estado("exitoso")
                                .url(url)
                                .nombreArchivo(item.objectName())
                                .metadata(stat.userMetadata())
                                .tamanio(stat.size())
                                .tipoContenido(stat.contentType())
                                .ultimaModificacion(stat.lastModified().toString())
                                .tiempoExpiracion("1 hora")
                                .build());
                    }
                } catch (Exception e) {
                    log.warn("Error al procesar archivo: {}", e.getMessage());
                    continue;
                }
            }

            return responses;
        } catch (Exception e) {
            log.error("Error al obtener archivos para el ObjectId: {}", objectId, e);
            throw new FileStorageException("No se pudieron obtener los archivos", e);
        }
    }

    public void deleteFile(String fileName, String objectId) throws FileStorageException {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            String fileObjectId = stat.userMetadata().get("object_id");
            if (!objectId.equals(fileObjectId)) {
                throw new FileStorageException(
                        "El archivo " + fileName + " no pertenece al objectId: " + objectId
                );
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            log.info("Archivo eliminado exitosamente: {} del objectId: {}", fileName, objectId);
        } catch (FileStorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al eliminar el archivo: {} del objectId: {}", fileName, objectId, e);
            throw new FileStorageException("No se pudo eliminar el archivo: " + e.getMessage(), e);
        }
    }
}