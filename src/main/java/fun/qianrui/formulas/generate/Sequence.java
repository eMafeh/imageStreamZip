package fun.qianrui.formulas.generate;


import java.util.function.IntConsumer;

/**
 * 连续记录 data、begin、dx&times、dx&times
 *
 * 等差数列记录信息
 *
 * @author 钱睿
 * @date 20220709
 */
public interface Sequence extends IntGenerator{
    /**
     * 开始下标 负最小值含义为
     */
    int begin();

    /**
     * 开始下标 负最小值含义为
     */
    int end();
    /**
     * 公差
     */
    int dx();

    @Override
    default void write(IntConsumer consumer) {
        final int begin = begin();
        final int times = times();
        final int dx = dx();
        for (int i = 0; i < times; i++) {
            final int index = begin + i * dx;
            consumer.accept(index);
        }
    }

     static Sequence getSequence(int beginIndex, int endIndex) {
        if (beginIndex == endIndex) {
            return new Singleton(beginIndex);
        } else {
            return new Successive(beginIndex, endIndex);
        }
    }

    /**
     * 连续序列（公差为1的等差数列）
     *
     * @author 20021438
     * 2022/7/15
     */
    record Successive(int begin, int end) implements Sequence {

        @Override
        public int dx() {
            return 1;
        }

        @Override
        public int times() {
            return end - begin + 1;
        }

        @Override
        public int zipSize() {
            return 2;
        }
    }

    /**
     * 单个元素的序列
     *
     * @author 20021438
     * 2022/7/15
     */
    record Singleton(int num) implements Sequence {

        @Override
        public int begin() {
            return num;
        }

        @Override
        public int end() {
            return num;
        }

        @Override
        public int dx() {
            return 1;
        }

        @Override
        public int times() {
            return 1;
        }

        @Override
        public int zipSize() {
            return 1;
        }
    }

    /**
     * 序列
     *
     * @author 20021438
     * 2022/7/15
     */
    record Arithmetic(int begin, int dx, int times) implements Sequence {

        @Override
        public int end() {
            return begin + (times - 1) * dx;
        }

        @Override
        public int zipSize() {
            return 2;
        }
    }
}