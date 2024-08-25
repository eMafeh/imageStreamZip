package fun.qianrui;

import fun.qianrui.base.data.BigFile;
import fun.qianrui.base.computer.DateUtil;
import fun.qianrui.util.image.ScreenCaptureRecording;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author 20021438
 * 2022/4/28
 */
public class MainBoot {
    public static final BigFile bigFile;
    public static final long time;

    static {
        final String yyyyMMdd = "yyyyMMdd";
        final String today = DateUtil.format(yyyyMMdd, System.currentTimeMillis());
        time = DateUtil.parse(yyyyMMdd, today)
                .getTime();
        bigFile = new BigFile("F:\\ScreenCapture" + today, 2_000_000_000);
    }

    public static void main(String[] args) throws InterruptedException, AWTException {
        Robot robot = new Robot();
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = defaultToolkit.getScreenSize();
        ScreenCaptureRecording recording = new ScreenCaptureRecording((int) screenSize.getWidth(), (int) screenSize.getHeight(), 10);
        recording.put = bigFile::put;
        while (true) {
            BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight()));
            final long l = System.currentTimeMillis();
            recording.addImage(screenCapture);
            System.out.println(System.currentTimeMillis() - l);
            Thread.sleep(1000L);
        }
    }

}
