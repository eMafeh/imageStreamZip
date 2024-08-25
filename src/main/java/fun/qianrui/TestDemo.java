package fun.qianrui;
import fun.qianrui.base.data.BigFile;
import fun.qianrui.util.image.ScreenCaptureRecording;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static fun.qianrui.util.image.ImageUtil.*;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

/**
 * 1.robot.createScreenCapture:40ms
 * 2.jpeg save:80ms read:40ms
 * 3.png save:180ms read:60ms
 * 4.1.8M diff:180ms toArray:120ms  drawTo:60ms
 *
 * @author 20021438
 * 2022/5/16
 */
public class TestDemo {


    //    {
//        BufferedImage src1 = ImageIO.read(new File("Img221785570.jpg"));
// BufferedImage src2 = ImageIO.read(new File("W.gif"));
// //BufferedImage src3 = ImageIO.read(new File("c:/ship3.jpg"));
// AnimatedGifEncoder e = new AnimatedGifEncoder();
// e.setRepeat(0);
// e.start("laoma.gif");
// e.setDelay(300); // 1 frame per sec
// e.addFrame(src1);
// e.setDelay(100);
// e.addFrame(src2);
// e.setDelay(100);
// //  e.addFrame(src2);
// e.finish();
//    }


    public static void main(String[] args) throws IOException, AWTException {
        final BigFile bigFile = new BigFile("F:\\ScreenCapture20220527", 2_000_000_000);
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = defaultToolkit.getScreenSize();
        ScreenCaptureRecording recording = new ScreenCaptureRecording((int) screenSize.getWidth(), (int) screenSize.getHeight(), 10);

        Iterator<BufferedImage> iterator = ScreenCaptureRecording.iterator(bigFile::get, () -> bigFile.logs()
                .stream()
                .map(i -> i.name)
                .collect(Collectors.toList()));
        while (iterator.hasNext()){
            final BufferedImage next = iterator.next();
            recording.addImage(next);
        }

    }


    private static void extracted() throws AWTException {

        Robot robot = new Robot();
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = defaultToolkit.getScreenSize();
        long l;
        for (int i = 0; i < 100; i++) {

            l = System.currentTimeMillis();
            BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight()));
            System.out.print((System.currentTimeMillis() - l) + "\t");


            l = System.currentTimeMillis();
            final byte[] jpeg = getBytes(screenCapture, "jpeg");
            System.out.print((System.currentTimeMillis() - l) + "\t");

            l = System.currentTimeMillis();
            final BufferedImage jpegImage = getImage(jpeg);
            System.out.print((System.currentTimeMillis() - l) + "\t");

            l = System.currentTimeMillis();
            final byte[] pngs = getBytes(screenCapture, "png");
            System.out.print((System.currentTimeMillis() - l) + "\t");

            l = System.currentTimeMillis();
            final BufferedImage image = getImage(pngs);
            System.out.print((System.currentTimeMillis() - l) + "\t");

            l = System.currentTimeMillis();
            final List<Point> xor = xor(image, jpegImage);
            System.out.print((System.currentTimeMillis() - l) + "\t");

            l = System.currentTimeMillis();
            final BufferedImage t = add(jpegImage, xor);
            System.out.print((System.currentTimeMillis() - l) + "\t");


            l = System.currentTimeMillis();
            final byte[] bytes = toByteArray(xor);
            System.out.print((System.currentTimeMillis() - l) + "\t");


            l = System.currentTimeMillis();
            fromByteArray(bytes);
            System.out.print((System.currentTimeMillis() - l) + "\t");

            System.out.println();
        }
//        System.out.println(screenCapture.getRaster().getClass());
//        System.out.println(screenCapture.getColorModel().getClass());
//        System.out.println(jpegImage.getRaster().getClass());
//        System.out.println(jpegImage.getColorModel().getClass());
//        System.out.println(image.getRaster().getClass());
//        System.out.println(image.getColorModel().getClass());
//        System.out.println(xor.getRaster().getClass());
//        System.out.println(xor.getColorModel().getClass());
//        System.out.println(xor2.getRaster().getClass());
//        System.out.println(xor2.getColorModel().getClass());
    }


    private static void change(byte[] bytes, byte[] last) throws IOException {
        BufferedImage i1 = getImage(last);
        BufferedImage i2 = getImage(bytes);
        BufferedImage i3 = new BufferedImage(i1.getWidth(), i1.getHeight(), TYPE_4BYTE_ABGR);
        int c = 0;
        for (int i = 0; i < i1.getWidth(); i++) {
            for (int j = 0; j < i1.getHeight(); j++) {
                final int r1 = i1.getRGB(i, j);
                final int r2 = i2.getRGB(i, j);
                if (r1 != r2) {
                    i3.setRGB(i, j, r2);
                    c++;
                }
            }
        }
        int c2 = 0;

        final BufferedImage png = getImage(getBytes(i3, "png"));

        for (int i = 0; i < i1.getWidth(); i++) {
            for (int j = 0; j < i1.getHeight(); j++) {
                final int r3 = png.getRGB(i, j);
                if (r3 != 0) {
                    c2++;
                }
            }
        }
        System.out.print("dif:" + c + "\t" + c2 + "\t");
        show(i3);
    }

    static final String[] strings = {"png", "jpeg"
//            , "gif"
//            , "bmp", "tif", "wbmp"
    };

    private static void show(BufferedImage image) throws IOException {
        for (String string : strings) {
            System.out.print(string + "\t" + getBytes(image, string).length + "\t");
        }
        System.out.println();

    }

    private static void compare(byte[] bytes, byte[] last) throws IOException {
        final byte[] s1 = trans(last, "gif");
        final byte[] s2 = trans(bytes, "gif");
        int c = 0;
        final int length = Math.min(s1.length, s2.length);
        for (int i = 0; i < length; i++) {
            if (s1[i] != s2[i]) {
                c++;
            }
        }
        System.out.println(bytes.length + "\t" + last.length + "\t" + c);
    }

    private static byte[] trans(byte[] bytes, String formatName) throws IOException {
        BufferedImage image = getImage(bytes);
        return getBytes(image, formatName);
    }


}
