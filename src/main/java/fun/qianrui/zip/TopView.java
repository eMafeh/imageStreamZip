package fun.qianrui.zip;

import fun.qianrui.formulas.analyzer.Block;
import fun.qianrui.formulas.analyzer.ContainerBlock;
import fun.qianrui.formulas.analyzer.SuccessiveBlock;
import fun.qianrui.pathway.SourceDataToSuccessiveBlock;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * 压缩的本质，实际上是对全体自然数序列重排序
 * 由于任意压缩信息都为二进制的01串，而压缩后的信息也是二进制的01串——表述形式一致，都是二进制自然数空间中确定的一个大数字
 * 这里设原先为y，最大Y，压缩后为x，最大X，压缩函数为 z，解压函数为 u
 * ∵ 解压只能唯一输出原信息
 * ∴ y = u(x) 是单映射函数
 * ∵ 对所有的串都存在压缩、解压
 * ∴ y = u(x) 是y的满映射函数
 * ∵ 压缩要求，多个x可以解压到同一个y，一个x只能解压到唯一的一个y
 * ∴ count(x)大于等于count(y)
 * ∴ max(x)大于等于max(y)
 * 当每个y也只会被一个x压缩时，可取等号，x的取值空间最小，和y的取值空间一致
 * x = z(y) 也是满映射函数
 * 推论1：ΣY=ΣX
 * 这时，实际上这个映射就是对y自然数序列的一种重排序，赋予的新得下标即是x
 *
 * 推论2：{Σ(y-x),x小于y}={Σ(x-y),x大于y}
 * 为了满足压缩效果，每一个压缩串的bit数都要少于原先bit数，也就是 x小于y
 * 这实际上是不可能的!!!至少有一个数要 x大于y,才能满足推论2
 * 比如 2=u(1) 3=u(2) 4=u(3) 由于 x=1，2，3已经被占，那么y=1 时，x至少也是4
 * 压缩，如果要减小一些数的下标，必然会增大另一些数的下标，且减小的越多，另一些数增大的也越多
 *
 * 实际上还有一种考量，对于 a = u(b) b = u(c) ... a大于b大于c，不断地鸠占鹊巢
 * 最终达到一个x，无法压缩后变小，
 * 要么x只能放弃压缩，则u函数需要增加一位标识是否进行了压缩
 * 则原先的压缩结果全部增加了一位，且 x 压缩后也增大了一位
 * 要么接收x压缩后反而增加了很多位
 *
 * 但是，有一些信息，实际上比其他信息更混乱，本身就是无价值的信息，花屏，噪音，猴子打的哈姆雷特
 * 另一些，即便bit数很高，却充满规律，人可以解读。
 * 推论3：压缩是把信息按人能理解的难易程度重排序，人越能理解，越简单的应当放在越前面。
 * </pre>
 *
 * 压缩的精髓就在于寻找规律，如果补充下标信息（不行也还可以针对下标进行压缩，重新去除下标，实际无损耗）总信息量为 N(信息+下标)
 * 在补充下标后，连续合并成等差为1的数列的本至是，按数据聚合（压缩数据）、按下标排序、识别其中公差为1的序列（压缩下标）
 * 该操作可以高维化，继续按 公差和项数 聚合，分析每组的下标是否构成 新的公差和项数的序列（高维压缩下标）
 * <pre>
 * <b style="color:yellow">list[data]</b>
 * add(index) -> <b style="color:yellow">set[(data,index)]</b>
 * group(data) -> <b style="color:yellow">map[data,set[index]]</b>
 * zip(index) -> <b style="color:yellow">map[data,set[(begin,times)]]</b>
 * <b style="color:red">单块数据</b>
 * if(set=1) flat-> <b style="color:yellow">map[data,(begin,times)]</b>
 *      flipToSet -> <b style="color:yellow">set[times,data,begin]</b>
 *      group(times) -> <b style="color:yellow">map[times,set[begin,data]]</b>
 *      end -> <b style="color:white">map[times,list[begin]+list[data]]</b>
 * <b style="color:red">有多块的数据，按块长度聚合</b>
 * if(set>1) group(times) -> <b style="color:yellow">map[data,map[times,set[begin]]]</b>
 *      .....
 * <b style="color:red">构建二维等差数列</b> <b style="color:green">map[data,(begin,times)]</b>
 * if(set=1) -> <b style="color:yellow">map[data,map[times,(begin2,times2,dx)]]</b>
 * if(set>1) -> <b style="color:yellow">map[data,(times,(begin2,times2))]</b>
 * if(times>1) -> <b style="color:yellow">map[data,map[times,set[begin]]]</b>
 *
 *
 * </pre>
 * .....
 *
 * <pre>
 * 1.目前存在如下压缩思路
 *
 *     有下标
 *     连续（或固定间隔）等差数列：
 *          输入为同信息的下标数组(或者连续下标的信息数组)，
 *          标记为信息 队列头下标 公差&项数(或者连续下标头 队列头信息 公差&项数),
 *          压缩率 n(信息+下标)->信息+下标+公差&项数
 *          再次序列化为二进制 信息位 + 下标（无符号int）、状态转移（单项 int第二大值、结束 int最大值，先预留1024个）位 + 公差&项数
 *              无法消耗的数据，归还回大图，还是自己按单项的下标记着？是问后续是无规律的下标，还是多了一种信息的大图更好。
 *              归还大图，无规律下标并无甚好处理，但是大图中可能能进行三角函数处理
 *     换元 三角函数：
 *          输入为无规律但存在重复信息子串，
 *          标记为原始队列 下标数组，
 *          压缩率 n(信息+下标)-> n(信息+下标)/t +t(下标)
 *     无下标
 *     换元 指数函数：
 *          记录不同成分的出现概率，按概率指数计算分配所有元素换元树，
 *          压缩率为信息熵公式
 *
 * 2.由程序生成的窗口图片中，会经常出现连续相同元素，如果引入可填充下标图，可以大大压缩规律的部分，不规律的部分重新连续后下标移除，再执行香农压缩，
 * 3.规律发现的计算成本不同，连续数列容易计算，等差数列就难一些，同串换元就较难发现
 * 4.但同时逻辑执行前后并不干扰再次执行，且这几个的可以递归嵌套执行形成高维结构进一步压缩，选择好执行的先消耗数据，可以简化后续逻辑
 * 5.对于已经按有下标处理过的数据，如何序列化后再次参与香农压缩？
 * </pre>
 * 两份数据对比差异：数据种类计数（对数，匹配小数点后几位？）、前100数据分布频率、前100数据种类
 * <p>
 * 存储就不用存原始数据序列，只需要存压缩后的，但是如何遍历比较两个结果集呢？
 * 换言之，如何快速获取某一个点的真实数据，图层如何穿透
 * 非连续的序列，实际上就已经是折断后重新组织的高维序列了，所以初步下确实是需要去除非连续的
 * <p>
 * 什么样的数据一定是上层数据呢？无法通过插入其他数据来进一步降低压缩比的，可以放到最上层
 * 但是这里有一个矛盾，当前的上层，有可能通过和其他的上层合并，进一步压缩比率，如果已经放到了上层也就是所谓的结果集，就丧失了进一步压缩的希望
 * <p>
 * 几种特征数据
 * 1.形如aba型的，合并两端的a，挂载b
 * 2.单个颜色的也不影响aba的判断，同b一起挂载
 * 3.相邻的，被挂载的块和临块相同，且临块大，则翻转自身挂载后，和临块合并
 * 4.对于同数据块，每个计算和下一个的差，取差最多的构建等差数列，二维合并（基底和挂载都可以参与，全局只有挂载行为会导致分层，同层的可以继续）
 * 5.进阶：对于不同数据，判断数据本身
 *
 * @author 20021438
 * 2022/7/15
 */
public class TopView {
    /**
     * todo 原始颜色数据长度 可能不需要冗余记录
     */
    public final int length;

//    public final SuccessiveBlock[] blockView;

    public final ArrayList<SuccessiveBlock> root = new ArrayList<>();
    public final List<SuccessiveBlock> top = new ArrayList<>();
    public final List<SuccessiveBlock> down = new ArrayList<>();
    public final Map<Integer, Map<Byte, Object>> time_data_begin = new HashMap<>();
    public final Map<Byte, List<SuccessiveBlock>> groupMap;
    public final List<ContainerBlock> containerBlocks;

    // 最终可直接用于序列化的结果集
//    public final List<SuccessiveBlock> top = new ArrayList<>();
    public TopView(byte[] sourceData) {
        this.length = sourceData.length;
        final int blockSize = (sourceData.length + 255) / 256;
        for (int i = 0; i < blockSize; i++) {
            blockInit(sourceData, i);
        }

        // 转为块图
//        this.blockView = new SuccessiveBlock[sourceData.length];

        //当前根节点挂载的所有块，如果多块发生合并，则根节点只挂载合成的新块,点位在原先的第一块
        new SourceDataToSuccessiveBlock(root::add).eat(sourceData);

        //按数据分组统计
        groupMap = root.stream().collect(Collectors.groupingBy(Block::data));


        for (List<SuccessiveBlock> value : groupMap.values()) {
            if (value.size() == 1) {
                //单数据块
                top.add(value.get(0));
            } else {
                //多数据块
                value.sort(Comparator.comparingInt(SuccessiveBlock::size));

                down.addAll(value);
                for (SuccessiveBlock block : value) {
                    final Map<Byte, Object> map = time_data_begin.computeIfAbsent(block.sequence.times(), i -> new HashMap<>());
                    final Object o = map.get(block.data);
                    if (o == null) {
                        map.put(block.data, block);
                    } else if (o instanceof List) {
                        ((List<SuccessiveBlock>) o).add(block);
                    } else {
                        final ArrayList<Object> list = new ArrayList<>();
                        list.add(o);
                        list.add(block);
                        map.put(block.data, list);
                    }
                }
            }
        }


        //多数据块
        containerBlocks = groupMap
                .entrySet()
                .stream()
                .filter(i -> i.getValue().size() > 1)
                .map(i -> new ContainerBlock(i.getKey(), i.getValue()
                        .stream()
                        .mapToInt(Block::size)
                        .sum(), i.getValue()))
                .sorted(Comparator.comparingInt(ContainerBlock::size).reversed())
                .collect(Collectors.toList());


        // 遍历，对aba型的，将b放置到上层，a连起来(由于)

    }

    private void blockInit(byte[] sourceData, int i) {
        // block记录了连续256个下标的数据，可以构建的方式有[256]数组，[256]对子，数学公式
        // 优先转化为对子，对子可以参与后续操作，再决定实际存储格式

    }


    public int[] toSourceData() {
        //待还原数据
        final int[] result = new int[length];
        //考虑改位图

        //写入规则数据

//        Block.writeIndexData(root, (index, data) -> result[index] = data);
        return result;
    }


    private void removeLast(List<Block> root) {
        root.remove(root.size() - 1);
    }
}
