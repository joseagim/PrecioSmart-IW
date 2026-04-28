package es.ucm.fdi.iw.model;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class BarcodeReaderService {

    public String decodeBarcode(InputStream imageStream) throws Exception {
        
        BufferedImage bufferedImage = ImageIO.read(imageStream);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        // Definimos específicamente los formatos de producto
        List<BarcodeFormat> formatosPermitidos = List.of(
            BarcodeFormat.EAN_13, 
            BarcodeFormat.EAN_8, 
            BarcodeFormat.UPC_A
        );
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formatosPermitidos);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE); 

        try {
            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            return "No se detectó ningún código de barras.";
        }
    }
}
