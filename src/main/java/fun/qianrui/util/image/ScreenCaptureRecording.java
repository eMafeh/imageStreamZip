package fun.qianrui.util.image;

import fun.qianrui.base.computer.ZipUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 配合连续截屏，记录多个切换屏幕的连续图像帧，配合put方法落地
 * 提供迭代器，按顺序展示记录在get方法中的图像
 *
 * @author 20021438
 * 2022/5/26
 */
public class ScreenCaptureRecording implements Iterable<BufferedImage> {
    private static final long BEGIN = System.currentTimeMillis();

    public volatile BiConsumer<String, byte[]> put;
    public volatile Function<String, byte[]> get;
    public volatile Supplier<List<String>> keyList;
    public volatile IntSupplier size;
    public volatile int tempCount = 10;
    private volatile int rootNum = 0;
    private final int width;
    private final int height;
    private final int fuseSize;
    private final Window[] windows;
    private final Window tempWindow;
    private volatile BufferedImage tempImage;

    public ScreenCaptureRecording(int width, int height, int windows) {
        this.width = width;
        this.height = height;
        this.fuseSize = width * height;
        this.windows = new Window[windows + 1];
        //最后一位用来放新进来图片的rgb
        tempWindow = this.windows[windows] = new Window(windows, -1);

    }

    public synchronized void addImage(BufferedImage image) {
        if (image.getWidth() != width || image.getHeight() != height) {
            throw new RuntimeException("size diff" + width + ":" + image.getHeight() + " " + height + ":" + image.getWidth());
        }
        tempWindow.rgbs = image.getRGB(0, 0, width, height, new int[width * height], 0, width);


//        System.out.println();
        tempImage = image;
        final Optional<Window> min = Stream.of(windows)
                .parallel()
                .filter(Objects::nonNull)
                .map(Window::diff)
                .filter(a -> a.diffBytes != null)
                .min(Comparator.comparingInt(a -> a.diffBytes.length));
        tempImage = null;
        if (min.isEmpty()) {
            throw new RuntimeException("check code:must hive one target");
        }
        final Window minDiff = min.get();
        final int index;
        //全新图像
        if (minDiff == tempWindow) {
            index = getNewIndex();
            rootNum += 1;
            windows[index] = new Window(index, rootNum);
        }
        //可以认为是某张图的后续
        else {
            index = minDiff.index;
        }
        final Window window = windows[index];
        window.iteration++;
        window.lastTime = (int) (System.currentTimeMillis() - BEGIN);
        window.rgbs = tempWindow.rgbs;
//        System.out.print(window.rootNum + "\t");
//        System.out.print(minDiff.diffBytes.length + "\t");
//        count(tempWindow.rgbs);
        tempWindow.rgbs = null;
        put(window.rootNum, window.iteration, minDiff.diffBytes);
        for (Window w : windows) if (w != null) w.diffBytes = null;
    }

    private static final double L2 = Math.log(2);

    private void count(int[] rgbs) {
        final HashMap<Integer, Integer> map = new HashMap<>();
        for (int rgb : rgbs) {
            map.compute(rgb, (k, v) -> {
                if (v == null) return 1;
                return v + 1;
            });
        }
        double all = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            final double v = (double) entry.getValue() / rgbs.length;
            all += v * Math.log(v) / L2;
        }
        System.out.println(-all * rgbs.length);

        final List<Map.Entry<Integer, Integer>> collect = map.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toList());
        final int size = collect.size();
        System.out.println(size + " " + limit(collect, 1) + " " + limit(collect, 10) + " " + limit(collect, 100) + " " + limit(collect, size / 1000) + " " + limit(collect, size / 100) + " " + limit(collect, size / 30) + " " + limit(collect, size / 10));
    }

    private double limit(List<Map.Entry<Integer, Integer>> collect, int limit) {
        return collect.stream()
                       .limit(limit)
                       .mapToInt(i -> i.getValue())
                       .sum() / 1920d / 1080;
    }

    private void put(int rootNum, int iteration, byte[] data) {
        if (put != null) put.accept(rootNum + "," + iteration, data);
    }

    private int getNewIndex() {
        int minIterationIndex = -1;
        int minIteration = Integer.MAX_VALUE;
        int minLastTimeIndex = -1;
        int minLastTime = Integer.MAX_VALUE;

        for (int i = 0; i < windows.length - 1; i++) {
            final Window window = windows[i];
            //还有空位，直接返回
            if (window == null) return i;
            final int iteration = window.iteration;
            if (iteration < tempCount) {
                //记录迭代数最小的
                if (iteration < minIteration) {
                    minIterationIndex = i;
                    minIteration = iteration;
                }
            } else {
                //记录最老的
                final int lastTime = window.lastTime;
                if (lastTime < minLastTime) {
                    minLastTimeIndex = i;
                    minLastTime = lastTime;
                }
            }
        }
        final int newIndex = getNewIndex(minIterationIndex, minLastTimeIndex);
        put(windows[newIndex].rootNum, -1, new byte[0]);
        return newIndex;
    }

    private int getNewIndex(int minIterationIndex, int minLastTimeIndex) {
        if (minIterationIndex != -1) return minIterationIndex;
        else if (minLastTimeIndex != -1) return minLastTimeIndex;
        else throw new RuntimeException("check code:must make a choose");
    }

    @Override
    public Iterator<BufferedImage> iterator() {
        return iterator(get, keyList);
    }

    public static Iterator<BufferedImage> iterator(Function<String, byte[]> get, Supplier<List<String>> keyList) {
        final List<String> keys = keyList.get();
        return new Iterator<>() {
            int index = 0;
            final HashMap<String, BufferedImage> images = new HashMap<>();

            @Override
            public boolean hasNext() {
                return index < keys.size();
            }

            @Override
            public BufferedImage next() {
                final String window = keys.get(index);
                index++;
                final byte[] data = get.apply(window);
                final String[] split = window.split(",");
                final String rootNum = split[0];
                final String iteration = split[1];
                if ("-1".equals(iteration)) {
                    images.remove(rootNum);
                    return next();
                }

                final BufferedImage image = images.get(rootNum);
                if (image == null) {
                    final BufferedImage first = ImageUtil.getImage(data);
                    images.put(rootNum, first);
                    return first;
                } else if (data.length > 0) {
                    final BufferedImage add = ImageUtil.add(image, ImageUtil.fromByteArray(ZipUtil.unZip(data)));
                    images.put(rootNum, add);
                    return add;
                } else {
                    return image;
                }
            }
        };
    }

    @Override
    public Spliterator<BufferedImage> spliterator() {
        return Spliterators.spliterator(iterator(), size.getAsInt(), Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }

    private class Window {
        final int index;
        final int rootNum;
        volatile int iteration;
        volatile int lastTime;
        volatile int[] rgbs;

        volatile byte[] diffBytes;

        public Window(int index, int rootNum) {
            this.index = index;
            this.rootNum = rootNum;
        }

        public Window diff() {
            if (this == tempWindow) {
                this.diffBytes = ImageUtil.getBytes(tempImage, "png");
            } else {
                final List<Point> xor = ImageUtil.xor(tempWindow.rgbs, this.rgbs, fuseSize);
                if (xor.isEmpty()) {
                    this.diffBytes = new byte[0];
                } else if (xor.size() < fuseSize) {
                    this.diffBytes = ZipUtil.zip(ImageUtil.toByteArray(xor));
                } else {
                    this.diffBytes = null;
                }
            }
            return this;
        }
    }

    private static double bit(Integer num, int all) {
        final double v = (double) num / all;
        return v * Math.log(v) / L2;
    }
}
