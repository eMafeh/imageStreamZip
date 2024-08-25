package fun.qianrui.formulas.analyzer;

import fun.qianrui.formulas.generate.Sequence;

import java.util.List;

/**
 * @author 20021438
 * 2022/7/19
 */
public record HanoiBlock(byte data, int size, Sequence sequence, List<Block> tops) implements Block {


    @Override
    public byte zipSize() {
        return 0;
    }
}
