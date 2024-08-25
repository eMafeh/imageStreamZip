package fun.qianrui.util.image;

import fun.qianrui.base.computer.SerializableUtil;
import fun.qianrui.base.sys.ExceptionUtil;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static fun.qianrui.base.sys.ReflectCache.getField;

/**
 * @author 20021438
 * 2022/5/16
 */
public class ImageUtil {
    private static final ByteArrayOutputStream output = new ByteArrayOutputStream(2 << 15);
    public static final byte INDEX_4 = -128;
    public static final byte COLOR_4 = 127;

    public static synchronized byte[] getBytes(BufferedImage image, String formatName) {
        try {
            output.reset();
            ImageIO.write(image, formatName, new MemoryCacheImageOutputStream(output));
            return output.toByteArray();
        } catch (Exception e) {
            return ExceptionUtil.throwT(e);
        }
    }

    public static BufferedImage getImage(byte[] bytes) {
        try {
            return ImageIO.read(new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes)));
        } catch (Exception e) {
            return ExceptionUtil.throwT(e);
        }
    }

    /**
     * 用底图对目标图取异或生成结果图
     * 或者再次异或结果图生成目标图
     * b1图像范围内：按以下异或b2两轮，可以还原回b1的值（期望相同的点多，可以多取0减小存储）
     * 0 0 -> 0
     * 1 1 -> 0
     * 0 1 -> 1
     * 1 0 -> 1
     * 1 2 -> 1
     *
     * @param b1 目标图或结果图
     * @param b2 底图
     * @return 结果图或目标图
     */
    public static List<Point> xor(BufferedImage b1, BufferedImage b2) {
        Object outData1 = null, outData2 = null;
        if (b1.getHeight() != b2.getHeight() || b1.getWidth() != b2.getWidth()) {
            throw new RuntimeException("size diff" + b1.getHeight() + ":" + b2.getHeight() + " " + b1.getWidth() + ":" + b2.getWidth());
        }
        final ArrayList<Point> result = new ArrayList<>();
        for (int y = 0; y < b1.getHeight(); y++) {
            for (int x = 0; x < b1.getWidth(); x++) {
                xor(result, b1.getColorModel()
                                .getRGB((outData1 = b1.getRaster()
                                        .getDataElements(x, y, outData1))), b2.getColorModel()
                                .getRGB((outData2 = b2.getRaster()
                                        .getDataElements(x, y, outData2))),
                        x + y * b1.getWidth());
            }
        }
        return result;
    }

    private static void xor(ArrayList<Point> result, int rgb1, int rgb2, int index) {
        final int rgb;
        if (rgb1 != 0) {
            //都有颜色，比较，不相同设置b1
            if (rgb1 != rgb2) {
                rgb = rgb1;
            } else {
                rgb = 0;
            }
        } else {
            //b1没有颜色，设置b2
            rgb = rgb2;
        }
        if (rgb != 0) {
            result.add(new Point(index, rgb));
        }
    }

    public static List<Point> xor(int[] b1, int[] b2, int fuseSize) {
        if (b1.length != b2.length) {
            throw new RuntimeException("size diff" + b1.length + ":" + b2.length);
        }
        final ArrayList<Point> result = new ArrayList<>();
        for (int i = 0; i < b1.length && result.size() < fuseSize; i++) {
            xor(result, b1[i], b2[i], i);
        }
        return result;
    }

    public static BufferedImage add(BufferedImage b, List<Point> data) {
        Object outData = null;
        for (Point datum : data) {
            outData = b.getColorModel()
                    .getDataElements(datum.y, outData);
            b.getRaster()
                    .setDataElements(datum.x % b.getWidth(), datum.x / b.getWidth(), outData);
        }
        return b;
    }

    public static byte[] toByteArray(List<Point> data) {
        final int size = data.size();
        final byte[] bytes = new byte[size * 6];
        List<Point> toSort = new ArrayList<>(data);
        toSort.sort(Comparator.comparingInt((Point a) -> a.y)
                .thenComparingInt(a -> a.x));
        for (int i = 0; i < size; i++) {
            Point datum = toSort.get(i);
            //最大 16777215
            if (datum.x >= 16777215) throw new RuntimeException("out of index " + datum.x);
            //记录rgb
            SerializableUtil.toByte(datum.y, bytes, i + size * 3, i + size * 4, i + size * 5, i);
            //实际第三位总是255
            if (bytes[i] != COLOR_4) throw new RuntimeException("out of color " + bytes[i]);
            //记录index
            SerializableUtil.toByte(datum.x, bytes, i, i + size, i + size * 2, -1);
        }
        return bytes;
    }

    public static List<Point> fromByteArray(byte[] bytes) {
        final int size = bytes.length / 6;
        final ArrayList<Point> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final int index = SerializableUtil.toInt(bytes[i], bytes[i + size], bytes[i + size * 2], INDEX_4);
            final int rgb = SerializableUtil.toInt(bytes[i + size * 3], bytes[i + size * 4], bytes[i + size * 5], COLOR_4);
            result.add(new Point(index, rgb));
        }
        return result;
    }

    public static void compare(BufferedImage bi) {
        final ColorModel colorModel = bi.getColorModel();
        final WritableRaster raster = bi.getRaster();
        if (!(colorModel instanceof DirectColorModel)) {
            throw new RuntimeException("not support:" + colorModel.getClass()
                    .getName());
        }
        final String name = raster.getClass()
                .getName();
        if (!"sun.awt.image.IntegerInterleavedRaster".equals(name) && !"sun.awt.image.IntegerComponentRaster".equals(name)) {
            throw new RuntimeException("not support:" + name);
        }
        final DirectColorModel cm = (DirectColorModel) colorModel;
        final int minX = raster.getMinX();
        final int minY = raster.getMinY();
        final int scanlineStride = getField(raster, "scanlineStride");
        final int[] dataOffsets = getField(raster, "dataOffsets");
        final int numDataElements = raster.getNumDataElements();

        final DataBuffer dataBuffer = raster.getDataBuffer();
        if (dataBuffer instanceof DataBufferInt) {
            final int[] data = ((DataBufferInt) dataBuffer).getData();
        }

//        if ((x < this.minX) || (y < this.minY) ||
//            (x >= this.maxX) || (y >= this.maxY)) {
//            throw new ArrayIndexOutOfBoundsException
//                    ("Coordinate out of bounds!");
//        }
//        int[] outData = (int[]) obj;
//
//        int off = (y - minY) * scanlineStride +
//                  (x - minX) * pixelStride;
//        for (int band = 0; band < numDataElements; band++) {
//            outData[band] = data[dataOffsets[band] + off];
//        }
//
//        raster.getScanlineStride()

    }

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        WritableRaster raster = bi.copyData(bi.getRaster()
                .createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
    }
}
