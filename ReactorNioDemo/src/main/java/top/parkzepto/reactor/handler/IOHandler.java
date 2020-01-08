package top.parkzepto.reactor.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description 处理IO事件
 * @Author 唐亮
 * @Date 2020-01-07 14:42
 * @Version 1.0
 */
public class IOHandler implements Runnable{
    enum IOType {
        WRITE, READ
    }

    public static int BUFF_SIZE = 1024;
    public static int THREAD_SIZE = 5;
    // IO handler为的实际业务处理是在线程池中进行
    public static ExecutorService pool = Executors.newFixedThreadPool(THREAD_SIZE);

    private Selector selector;
    private SocketChannel client;
    private SelectionKey selectionKey;
    private IOType curType = IOType.READ;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFF_SIZE);

    public IOHandler(Selector selector, SocketChannel client) throws IOException {
        this.selector = selector;
        this.client = client;
        this.client.configureBlocking(false);
        // 暂时不进行事件监听 0
        selectionKey = this.client.register(this.selector, 0);
        selectionKey.attach(this);

        // 初次设定：等待客户端的可读状态
        selectionKey.interestOps(SelectionKey.OP_READ);
        // 这里会让调用select的进程立即返回，回到Reactor即可知道，接下来会通知sk进入handler run
        this.selector.wakeup();
    }

    public void run() {
        // 可以多线程处理IO事件，此时当IO量过大或当线程池满时，并不会阻塞其他Handler的运行，
        // 重点是不影响处理连接的AcceptHandler，避免了服务因阻塞而不可用
        pool.execute(new Task());
    }

    /**
     * 确保当前Handler实例同步执行IO操作
     */
    public synchronized void eventHandle(){
        // 远程连接关闭，但是如何释放资源呢
        if(!selectionKey.isValid()){ return; }
        try {
            if (curType == IOType.READ){
                // 通道可读
                // buffer 处于可写状态
                buffer.clear();
                // 接收完毕
                int length = 0;
                while ((length = client.read(buffer)) > 0){
                    System.out.print(new String(buffer.array(), 0, length));
                }
                // 清理buffer
                buffer.clear();
                // 调整状态，准备返回客户端数据
                // Write应该是能当即触发的事件，缓存满了该事件才不会触发
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                curType = IOType.WRITE;
            }else if(curType == IOType.WRITE){
                // 写入量远超客户端数据发送量？为什么
                // 涉及大量的读写模式切换
                buffer.clear();
                buffer.put("Hello! I've received your message!".getBytes());
                // 对写入通道而言，buffer需要读模式
                buffer.flip();
                client.write(buffer);
                buffer.clear();
                selectionKey.interestOps(SelectionKey.OP_READ);
                curType = IOType.READ;
            }
        }catch (Exception e){
            e.printStackTrace();
            selectionKey.cancel();
            try {
                client.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 同步任务执行
     */
    class Task implements Runnable {

        public void run() {
            IOHandler.this.eventHandle();
        }
    }

}
