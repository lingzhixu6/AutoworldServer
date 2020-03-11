import IBM.DiscoveryNews;

import java.net.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;


public class TestSocket {

    Socket clientSocket;

    public void run() {

        int serverPort = 4700;
        ServerSocket serverSocket = null;
        byte[] receivBuf = new byte[8792];

        try {
            serverSocket = new ServerSocket(serverPort);
            while (true) {
                clientSocket = serverSocket.accept();
//                SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
//                System.out.println("Receive client IP: " + clientAddress);
                InputStream in = clientSocket.getInputStream();

                //不能使用while。我现在只能实现一问一答的模式。这里默认read一次就可以把client的消息全部读进buffer
                if ( in.read(receivBuf) != -1 )  //read from input stream and stores into buffer. Return -1 when reaching the end of inputstream
                {
                    String receivedData = new String(receivBuf, StandardCharsets.UTF_8).trim();
                    if (receivedData.charAt(0) == '^')  //a special symbol which indicates the data received is asking for some resource. E.g. Discovery news
                        switchResponse(receivedData);
                    else {
                        break;
                    }
                }
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchResponse(String receivedData) throws IOException {
        OutputStream out = clientSocket.getOutputStream();

        switch (receivedData.substring(1)) {
            case "news":
                out.write(new DiscoveryNews().queryNewsAndGetDesiredResult());
                break;
            default:
                break;
        }
    }

}
