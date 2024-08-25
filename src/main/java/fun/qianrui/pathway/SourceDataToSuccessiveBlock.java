package fun.qianrui.pathway;

import fun.qianrui.formulas.analyzer.SuccessiveBlock;
import fun.qianrui.formulas.generate.Sequence;

import java.util.function.Consumer;

/**
 * 连续下标，构建D1规则结果
 *
 * @author 20021438
 * 2022/7/22
 */
public class SourceDataToSuccessiveBlock {
    public final Consumer<SuccessiveBlock> shit;

    public SourceDataToSuccessiveBlock(Consumer<SuccessiveBlock> shit) {
        this.shit = shit;
    }

    public void eat(byte[] sourceData) {
        if (sourceData.length == 0) return;
        //当前开始下标
        int beginIndex = 0;
        //当前数据data
        byte leftData = sourceData[0];
        for (int index = 1; index < sourceData.length; index++) {
            //当前数据
            final byte data = sourceData[index];
            //相同，直接下一个
            if (leftData == data) {
                continue;
            }
            //不相同，收集连续结果，更新下标和data
            handle(leftData, beginIndex, index - 1);
            beginIndex = index;
            leftData = data;
        }
        handle(leftData, beginIndex, sourceData.length - 1);
    }

    private void handle(byte data, int beginIndex, int endIndex) {
        if (shit != null) {
            shit.accept(new SuccessiveBlock(data, Sequence.getSequence(beginIndex, endIndex)));
        }
    }
}
