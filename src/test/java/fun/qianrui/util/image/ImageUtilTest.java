package fun.qianrui.util.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static fun.qianrui.MainBoot.bigFile;
import static fun.qianrui.util.image.ImageUtil.getBytes;
import static fun.qianrui.util.image.ImageUtil.getImage;

/**
 * @author 20021438
 * 2022/5/16
 */
public class ImageUtilTest {
    public static void main(String[] args) throws IOException {
        final BufferedImage image = getImage(bigFile.get("1652357414"));
        final BufferedImage image1 = getImage(getBytes(image,"jpeg"));
        final BufferedImage image2 = getImage(getBytes(image,"jpeg"));
        same(image2,image1);
    }

    private static void same(BufferedImage x, BufferedImage x2) {
        System.out.println(x.getWidth() == x2.getWidth());
        System.out.println(x.getHeight() == x2.getHeight());
        for (int i = 0; i < x.getHeight(); i++) {
            for (int j = 0; j < x.getWidth(); j++) {
                if (x.getRGB(j, i) != x2.getRGB(j, i)) {
                    System.out.println(j + "\t" + i);
                }
            }
        }
        System.out.println("end");
    }
}