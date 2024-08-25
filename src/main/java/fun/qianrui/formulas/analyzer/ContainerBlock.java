package fun.qianrui.formulas.analyzer;

import java.util.List;

/**
 * @author 20021438
 * 2022/7/22
 */
public record ContainerBlock(byte data, int size, List<SuccessiveBlock> blocks) implements Block {

    @Override
    public byte zipSize() {
        return 0;
    }
}
