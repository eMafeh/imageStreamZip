package fun.qianrui;

import fun.qianrui.base.data.BigFile;
import fun.qianrui.base.function.ConsumerBiInt;
import fun.qianrui.formulas.analyzer.SuccessiveBlock;
import fun.qianrui.util.image.ScreenCaptureRecording;
import fun.qianrui.zip.TopView;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Collectors;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * @author 20021438
 * 2022/5/13
 */
public class ShowBoot {


    public static void main(String[] args) throws IOException, InterruptedException {
        JFrame jFrame = new JFrame();
        jFrame.setAlwaysOnTop(false);
        jFrame.setResizable(true);
        final BigFile bigFile = new BigFile("F:\\ScreenCapture20220527", 2_000_000_000);
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = defaultToolkit.getScreenSize();
        ScreenCaptureRecording recording = new ScreenCaptureRecording((int) screenSize.getWidth(), (int) screenSize.getHeight(), 100);

//        final Map<String, byte[]> all = bigFile.getAll();
        jFrame.setPreferredSize(new Dimension(500, 300));
        jFrame.add(new JComponent() {
            Iterator<BufferedImage> iterator;
            BufferedImage next;

            private void newIterator() {
                iterator = ScreenCaptureRecording.iterator(bigFile::get, () -> bigFile.logs()
                        .stream()
                        .map(i -> i.name)
                        .collect(Collectors.toList()));
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (iterator == null || !iterator.hasNext()) {
                    newIterator();
                }
//                if (next == null) {
//                    for (int i = 0; i < 1; i++) {
                        next = iterator.next();
//                    }
//                }
                try {
//                    final BufferedImage next = iterator.next();
                    final int width = next.getWidth();
//
//                    final TopView topView = new TopView(next.getRGB(0, 0, width, next.getHeight(), null, 0, width));
//                    Arrays.equals(dataArrayAnalyzer.toSourceData(), next.getRGB(0, 0, width, next.getHeight(), null, 0, width));



//                    g.setColor(Color.black);
//                    g.fillRect(0, 0, width, next.getHeight());
//                    if (indexArrayAnalyzer != null) {
//                    g.setColor(Color.RED);

                    final int[] ints = next.getRGB(0, 0, width, next.getHeight(), null, 0, width);
                    final ConsumerBiInt consumer = (index, d) -> {
                        final Color c = new Color(d);
                        g.setColor(c);
//                        g.setColor(Color.BLACK);
                        final int y = index / width;
                        final int x = index % width;
                        g.drawLine(x, y, x, y);
                    };
                    for (int i = 0; i < ints.length; i++) {
                        consumer.accept(i,ints[i]);
                    }
//                    write(g, topView.root, consumer);
//                    final ImageIcon icon = new ImageIcon(next);
//                    jFrame.setBounds(Math.max((RobotHandler.X_MAX - size.width) / 2, 0), Math.max((RobotHandler.Y_MAX - size.height) / 2, 0), size.width, size.height);
//                    icon.paintIcon(this, g, 0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        jFrame.setVisible(true);
//        Scanner scanner = new Scanner(System.in);
        while (true) {
//            i = scanner.nextInt();
            Thread.sleep(10L);
            jFrame.repaint();
        }
    }

    private static void write(Graphics g, java.util.List<SuccessiveBlock> list, ConsumerBiInt consumer) {
        for (SuccessiveBlock block : list) {
//                        if(block.size()!=6)continue;
//                        if(block.sequence.times()<i)continue;
//            g.setColor(Color.GREEN);
//            consumer.accept(block.sequence.begin(),block.data());
//            g.setColor(Color.RED);
//            consumer.accept(block.sequence.end(),block.data());
            System.out.println(block);
                        block.sequence.write(index->consumer.accept(index,block.data));
        }
    }

    public static int i=2;
}