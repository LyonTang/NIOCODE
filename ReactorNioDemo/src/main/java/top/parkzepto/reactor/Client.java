package top.parkzepto.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description 客户端连接程序
 * @Author 唐亮
 * @Date 2020-01-08 00:13
 * @Version 1.0
 */
public class Client {
    public static int PORT = 8000;
    public static int BUFFER_SIZE = 1024;

    private SocketChannel channel;

    public Client() throws IOException {
        channel = SocketChannel.open(new InetSocketAddress(PORT));
        channel.configureBlocking(false);
    }

    public void startConnect() throws IOException {
        while (!channel.finishConnect()) { }
        new Thread(new Processor(channel)).start();
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.startConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Processor implements Runnable, IDispatcher {
        private Selector selector;

        public Processor(SocketChannel channel) throws IOException {
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }

        public void run() {
            StaticMethods.loopSelection(selector, this);
        }

        // 阻塞
        public void dispatch(SelectionKey key) {
            if(!key.isValid()) { return; }
            try {
                if(key.isWritable()){
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    Scanner scanner = new Scanner(System.in);
                    if (scanner.hasNext()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        String next = scanner.next();
                        buffer.put(("Leon:" + next).getBytes());
                        buffer.flip();
                        socketChannel.write(buffer);
                        buffer.clear();
                    }
                }
                if(key.isReadable()){
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    int length = 0;
                    while ((length = socketChannel.read(buffer)) > 0) {
                        buffer.flip();
                        System.out.println("server:" + new String(buffer.array(), 0, length));
                        buffer.clear();
                    }
                }

            }catch (IOException e){
                try {
                    key.cancel();
                    key.channel().close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }

        }
    }
}
