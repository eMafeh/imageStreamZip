package fun.qianrui.formulas.analyzer;

/**
 * @author 20021438
 * 2022/7/16
 */
public interface Block {

    /**
     * 本块颜色
     */
    byte data();

    /**
     * 有效数据大小
     */
    int size();

    /**
     * 压缩后预计大小
     */
    byte zipSize();

}
