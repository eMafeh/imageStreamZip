package fun.qianrui.pathway;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 每一个步骤，有消费原料，生产中间产物，结束清空工作区域的能力
 * @author 20021438
 * 2022/7/22
 */
public interface Step<T, R> extends Consumer<T>, Supplier<R>, Closeable {

}
