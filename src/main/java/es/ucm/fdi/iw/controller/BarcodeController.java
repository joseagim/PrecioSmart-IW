package es.ucm.fdi.iw.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.ucm.fdi.iw.model.BarcodeReaderService;

@RestController
@RequestMapping("/api/lector")
public class BarcodeController {

    private final BarcodeReaderService barcodeService;

    public BarcodeController(BarcodeReaderService barcodeService) {
        this.barcodeService = barcodeService;
    }

    @PostMapping("/leer")
    public ResponseEntity<Map<String, String>> leerCodigo(@RequestParam("imagen") MultipartFile file) {
        try {
            String codigoEan = barcodeService.decodeBarcode(file.getInputStream());
            
            // Si ZXing no encuentra nada, devolvemos un error claro
            if (codigoEan.contains("Error")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(Map.of("error", "No se reconoce ningún código"));
            }

            // Devolvemos la URL que tu SearchController ya gestiona: /search/{product}
            String redirectUrl = "/search/" + codigoEan;
            return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}