package br.com.lume.odonto.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class ImageConverter {

    public static void main(String[] args) {
        File inputFile = new File("C:\\Users\\johil.lopes\\Desktop\\photo1.jpg");
        File outputFile = new File("C:\\Users\\johil.lopes\\Desktop\\photo2.gif");

        try {
            BufferedImage img = ImageIO.read(inputFile); // load image
            BufferedImage scaledImg = Scalr.resize(img, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 800);
            ImageIO.write(scaledImg, "gif", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
