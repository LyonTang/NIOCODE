package top.parkzepto.reactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * @Description TODO
 * @Author 唐亮
 * @Date 2020-01-08 15:05
 * @Version 1.0
 */
public class StaticMethods {
    public static void loopSelection(Selector selector, IDispatcher dispatcher){
        while (!Thread.interrupted()){
            try {
                // 阻塞
                selector.select();
                Iterator<SelectionKey> sks = selector.selectedKeys().iterator();
                while (sks.hasNext()){
                    SelectionKey selectionKey = sks.next();
                    dispatcher.dispatch(selectionKey);
                }
                // 处理完后，selector并不主动清理，需要主动清除已处理的key
                selector.selectedKeys().clear();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
