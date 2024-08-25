package fun.qianrui.pathway;

import fun.qianrui.formulas.analyzer.SuccessiveBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 20021438
 * 2022/7/22
 */
public class SuccessiveBlockToHanoiBlock {
    SuccessiveBlock last;
    final List<SuccessiveBlock> cache = new ArrayList<>();

    public void eat(SuccessiveBlock block) {
        if (last == null) {
            last = block;
            return;
        }
        if(block.data()==last.data()){
        }
//
//
//            //至少也要三个元素才能这么操作
//            if (root.size() < 3) return;
//        final Block last = root.get(root.size() - 1);
//        final Block begin = root.get(root.size() - 3);
//
//        if (begin.data() == last.data()) {
//            final Sequence sequence = Sequence.getSequence(begin.sequence().begin(), last.sequence().end());
//            final ArrayList<Block> screens = new ArrayList<>();
//            if (begin instanceof HanoiBlock) {
//                screens.addAll(((HanoiBlock) begin).tops());
//            }
//            final Block mid = root.get(root.size() - 2);
//            screens.add(mid);
//            if (last instanceof HanoiBlock) {
//                screens.addAll(((HanoiBlock) last).tops());
//            }
//            final HanoiBlock hanoiBlock = new HanoiBlock(begin.data(), begin.size() + last.size(), sequence, screens);
//
//            removeLast(root);
//            removeLast(root);
//            removeLast(root);
//            root.add(hanoiBlock);
    }

    public void end() {

    }
}
