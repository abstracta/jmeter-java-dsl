import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WebSocketEchoServer extends WebSocketServer {

    private final CountDownLatch startLatch = new CountDownLatch(1);

    public WebSocketEchoServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        conn.send(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        startLatch.countDown();
    }

    public void awaitStart(long timeout, TimeUnit unit) throws InterruptedException {
        if (!startLatch.await(timeout, unit)) {
            throw new RuntimeException("WebSocket server failed to start within timeout");
        }
    }

    public String getUri() {
        return "ws://localhost:" + getPort();
    }
}