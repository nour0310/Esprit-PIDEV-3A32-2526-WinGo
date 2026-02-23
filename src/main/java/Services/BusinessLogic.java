package Services;
import Entites.Transport;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
public class BusinessLogic {
    public static float calculerPrixDynamique(Transport transport) {
        float prixFinal = transport.getTarif();
        int heure = transport.getDateDepart().getHour();

        if ((heure >= 7 && heure <= 9) || (heure >= 17 && heure <= 19)) {
            prixFinal *= 1.25;
        }
        return prixFinal;
    }

    public static Image generateQRCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return new Image(new ByteArrayInputStream(pngOutputStream.toByteArray()));
        } catch (Exception e) {
            return null;
        }
    }
}
