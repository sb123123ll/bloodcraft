import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

public class GoreSteveGenerator {

    public static void fillRect(BufferedImage img, int x, int y, int w, int h, Color color) {
        int argb = color.getRGB();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (x + i < img.getWidth() && y + j < img.getHeight()) {
                    img.setRGB(x + i, y + j, argb);
                }
            }
        }
    }
    
    // Add blood splatter with some noise
    public static void splatter(BufferedImage img, int x, int y, int w, int h, Color c1, Color c2) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (x + i < img.getWidth() && y + j < img.getHeight()) {
                    if (Math.random() > 0.3) {
                        img.setRGB(x + i, y + j, (Math.random() > 0.5 ? c1 : c2).getRGB());
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        File inputFile = new File("G:\\downnowd\\正常史蒂夫.png");
        if (!inputFile.exists()) {
            System.out.println("No G:\\downnowd\\正常史蒂夫.png found!");
            return;
        }

        BufferedImage img = ImageIO.read(inputFile);
        
        Color EYE_BLACK = new Color(10, 10, 10, 255);
        Color BLOOD_RED = new Color(139, 0, 0, 255);
        Color FRESH_BLOOD = new Color(200, 20, 20, 255);
        Color MEAT = new Color(150, 60, 60, 255);
        Color BONE = new Color(220, 220, 210, 255);
        Color SKIN = new Color(170, 118, 86, 255);
        Color PANTS_BLUE = new Color(43, 43, 137, 255);

        // 1. Black eyes
        fillRect(img, 8+1, 8+4, 2, 1, EYE_BLACK);
        fillRect(img, 8+5, 8+4, 2, 1, EYE_BLACK);

        // 2. Blood tears
        fillRect(img, 8+1, 8+5, 1, 3, BLOOD_RED);
        fillRect(img, 8+2, 8+5, 1, 2, FRESH_BLOOD);
        fillRect(img, 8+5, 8+5, 1, 3, BLOOD_RED);
        fillRect(img, 8+6, 8+5, 1, 2, FRESH_BLOOD);

        // 3. Slash wound on Chest
        fillRect(img, 20+1, 20+3, 2, 2, SKIN);
        fillRect(img, 20+2, 20+4, 4, 2, MEAT);
        fillRect(img, 20+3, 20+5, 3, 2, FRESH_BLOOD);
        fillRect(img, 20+4, 20+6, 2, 2, SKIN);
        fillRect(img, 20+5, 20+7, 2, 2, MEAT);
        fillRect(img, 20+6, 20+8, 1, 2, BLOOD_RED);

        // 4. Bloody right leg (0-16, 16-32 is right leg)
        // Top
        fillRect(img, 4, 16, 4, 4, MEAT);
        fillRect(img, 8, 16, 4, 4, BLOOD_RED);
        // Sides
        splatter(img, 0, 20, 16, 12, MEAT, BLOOD_RED);
        // Expose some bone
        fillRect(img, 4, 20+3, 1, 3, BONE);
        // Leave some pants
        splatter(img, 0, 20+1, 3, 2, PANTS_BLUE, PANTS_BLUE);
        splatter(img, 8, 20+9, 4, 3, PANTS_BLUE, PANTS_BLUE);

        // 5. Bloody left leg (16-32, 48-64 is left leg)
        // Top
        fillRect(img, 20, 48, 4, 4, BLOOD_RED);
        fillRect(img, 24, 48, 4, 4, MEAT);
        // Sides
        splatter(img, 16, 52, 16, 12, MEAT, BLOOD_RED);
        // Expose bone
        fillRect(img, 20+2, 52+4, 1, 4, BONE);
        // Leave some pants
        splatter(img, 20, 52, 3, 3, PANTS_BLUE, PANTS_BLUE);
        splatter(img, 28, 52+6, 2, 2, PANTS_BLUE, PANTS_BLUE);

        // Save
        File outputFile = new File("src/main/resources/assets/blood/textures/entity/steve_gore.png");
        ImageIO.write(img, "png", outputFile);
        System.out.println("Successfully generated " + outputFile.getAbsolutePath());
    }
}
