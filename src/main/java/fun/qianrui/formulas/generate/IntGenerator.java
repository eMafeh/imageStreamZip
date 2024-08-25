package fun.qianrui.formulas.generate;

import java.util.function.IntConsumer;

/**
 * 提供一个不保证顺序的int流
 * @author 20021438
 * 2022/7/22
 */
public interface IntGenerator {
    void write(IntConsumer consumer);

    /**
     * 项数
     */
    int times();

    /**
     * 序列化后预计大小
     */
    int zipSize();
}
