package fun.qianrui.zip;

import fun.qianrui.base.function.MapBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 20021438
 * 2022/7/30
 */
public class AnalyzerDataBlock implements DataBlock {

    ArrayList<DataPair> dataPairs = new ArrayList<>(256);

    public AnalyzerDataBlock(byte[] sourceData, int i) {
        for (byte j = Byte.MIN_VALUE; j < Byte.MAX_VALUE; j++) {
            final byte b = sourceData[i * 256 - Byte.MIN_VALUE + j];
            dataPairs.add(new DataPair(b, j));
        }
        final Map<Byte, List<DataPair>> map = MapBuilder.toMap(dataPairs, a -> a.data);




        for (Map.Entry<Byte, List<DataPair>> entry : map.entrySet()) {
            final List<DataPair> dataPairs = entry.getValue();
            if(dataPairs.size()==1){
                //单个元素，没啥可玩的
                continue;
            }
            if(dataPairs.size()==2){
                //两个元素，肯定是等差数列，没啥可玩的
                continue;
            }

            DataPair before = null;
            for (DataPair dataPair : dataPairs) {
                if (before == null) {
                    dataPair.dx = -1;
                } else {
                    dataPair.dx = (short) (dataPair.index - before.index);
                }
                before = dataPair;
            }
            final Map<Short, List<DataPair>> toMap = MapBuilder.toMap(dataPairs, a -> a.dx);
            if(toMap.size()<dataPairs.size()){

            }
            for (Map.Entry<Short, List<DataPair>> listEntry : toMap.entrySet()) {
                final List<DataPair> value = listEntry.getValue();
                if (value.size() > 1) {

                }
            }
            for (Short dx : toMap.keySet()) {

            }

        }
    }

    static class DataPair {
        // 数据
        final byte data;
        // 块内下标
        final byte index;
        //和前一项的差值
        short dx;

        // 是否是第一项
//        boolean isFirst = false;

        public DataPair(byte data, byte index) {
            this.data = data;
            this.index = index;
        }
    }
}
