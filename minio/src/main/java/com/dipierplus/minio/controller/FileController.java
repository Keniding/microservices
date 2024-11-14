package com.dipierplus.minio.controller;

import com.dipierplus.minio.dto.FileUrlResponse;
import com.dipierplus.minio.exception.FileStorageException;
import com.dipierplus.minio.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload/{objectId}")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable String objectId) {
        try {
            Map<String, Object> result = fileStorageService.uploadFile(file, objectId);
            return ResponseEntity.ok(result);
        } catch (FileStorageException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("estado", "error");
            errorResponse.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/object/{objectId}")
    public ResponseEntity<?> getFilesByObjectId(@PathVariable String objectId) {
        try {
            List<FileUrlResponse> responses = fileStorageService.getFilesByObjectId(objectId);
            if (responses.isEmpty()) {
                Map<String, Object> emptyResponse = new HashMap<>();
                emptyResponse.put("estado", "sin_resultados");
                emptyResponse.put("mensaje", "No se encontraron archivos para el ObjectId proporcionado");
                return ResponseEntity.ok(emptyResponse);
            }
            return ResponseEntity.ok(responses);
        } catch (FileStorageException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("estado", "error");
            errorResponse.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }
    }

    @DeleteMapping("/{objectId}/{fileName}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String objectId,
            @PathVariable String fileName) {
        try {
            fileStorageService.deleteFile(fileName, objectId);
            Map<String, Object> response = new HashMap<>();
            response.put("estado", "exitoso");
            response.put("mensaje", "Archivo eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (FileStorageException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("estado", "error");
            errorResponse.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse);
        }
    }
}
