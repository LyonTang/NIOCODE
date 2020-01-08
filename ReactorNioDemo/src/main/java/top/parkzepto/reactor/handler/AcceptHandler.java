package top.parkzepto.reactor.handler;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 处理连接事件
 * @Author 唐亮
 * @Date 2020-01-07 14:42
 * @Version 1.0
 */
public class AcceptHandler implements Runnable {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    /**
     *
     * @param serverSocketChannel
     * @param selector
     */
    public AcceptHandler(ServerSocketChannel serverSocketChannel, Selector selector){
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
    }

    /**
     * 仅为新连接开辟一个IO处理的Handler
     */
    public void run() {
        try {
            SocketChannel client = serverSocketChannel.accept();
            if(client != null) {
                new IOHandler(selector, client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
