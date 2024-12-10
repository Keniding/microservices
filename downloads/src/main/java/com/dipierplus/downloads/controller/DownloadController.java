package com.dipierplus.downloads.controller;

import com.dipierplus.downloads.service.PdfGeneratorService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
public class DownloadController {
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable @NotBlank String invoiceId) {
        if (!ObjectId.isValid(invoiceId)) {
            return ResponseEntity.badRequest().build();
        }
        return pdfGeneratorService.generateInvoicePdf(invoiceId);
    }


    @PostMapping("/quote")
    public ResponseEntity<Resource> downloadQuote(@RequestBody Map<String, Object> quoteData) {
        return pdfGeneratorService.generateQuotePdf(quoteData);
    }
}
