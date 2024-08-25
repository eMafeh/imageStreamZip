package fun.qianrui.formulas.analyzer;

import fun.qianrui.formulas.generate.Sequence;

/**
 * @author 20021438
 * 2022/7/16
 */
public class SuccessiveBlock implements Block {
    public final byte data;
    public final Sequence sequence;

    public SuccessiveBlock(byte data, Sequence sequence) {
        this.data = data;
        this.sequence = sequence;
    }

    @Override
    public byte data() {
        return data;
    }

    @Override
    public int size() {
        return sequence.times();
    }


    @Override
    public byte zipSize() {
        return (byte) (1 + sequence.zipSize());
    }
}
