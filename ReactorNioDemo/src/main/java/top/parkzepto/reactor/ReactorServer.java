package top.parkzepto.reactor;

import top.parkzepto.reactor.handler.AcceptHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 基于Reactor模式的NIO Server
 * @Author 唐亮
 * @Date 2020-01-07 10:58
 * @Version 1.0
 */
public class ReactorServer {
    private Selector selector;
    // 服务连接通道
    private ServerSocketChannel serverSocketChannel;
    // 反应器
    private Reactor reactor;

    public ReactorServer() throws IOException {
        // 初始化Selector,Channel
        open();
        // localhost:8000
        InetSocketAddress address = new InetSocketAddress(8000);
        serverSocketChannel.socket().bind(address);
        serverSocketChannel.configureBlocking(false);

        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new AcceptHandler(serverSocketChannel, selector));

        reactor = new Reactor(selector);
    }

    private void open() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
    }

    public void startService(){
        if(reactor == null) { return; }
        new Thread(reactor).start();
    }

    public static void main(String[] args) {
        try {
            ReactorServer server = new ReactorServer();
            server.startService();
            System.out.println("服务启动Listen:8000");
        } catch (IOException e) {
            System.out.println("服务启动失败！");
            e.printStackTrace();
        }
    }

    /**
     * 反应器
     */
    class Reactor implements Runnable, IDispatcher {
        private Selector selector;
        public Reactor(Selector selector) {
            this.selector = selector;
        }

        public void run() {
            StaticMethods.loopSelection(selector, this);
        }

        /**
         * 分发事件
         * @param key 事件
         */
        public void dispatch(SelectionKey key){
            // 拿到key的处理器，执行处理事件
            Runnable handler = (Runnable) key.attachment();
            if(handler != null){
                handler.run();
            }
        }
    }
}
