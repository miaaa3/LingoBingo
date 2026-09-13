package com.estsb.QuizIT.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    // Method to generate QR code from a string and return the BufferedImage
    public BufferedImage generateQRCodeImage(String content) throws Exception {
        // Define the encoding hints
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // Create a QR code writer instance
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        // Create a bit matrix for the QR code
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200, hints);

        // Convert the bit matrix into a BufferedImage
        return toBufferedImage(bitMatrix);
    }

    // Method to convert BitMatrix into BufferedImage
    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE); // Background color
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK); // QR Code color

        // Loop through the bit matrix and set black or white pixels
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (matrix.get(i, j)) {
                    image.setRGB(i, j, Color.BLACK.getRGB());
                } else {
                    image.setRGB(i, j, Color.WHITE.getRGB());
                }
            }
        }

        return image;
    }

    // Method to return a byte array for the QR image (to send as a response or save)
    public byte[] generateQRCodeByteArray(String content) throws Exception {
        BufferedImage image = generateQRCodeImage(content);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "PNG", byteArrayOutputStream);  // Writing as PNG
        return byteArrayOutputStream.toByteArray();
    }
}