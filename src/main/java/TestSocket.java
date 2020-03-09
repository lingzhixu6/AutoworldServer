import java.net.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;


public class TestSocket {

    public static void run() {

        int serverPort = 4700;

        ServerSocket serverSocket = null;
        int recvMsgSize = 0;

        byte[] receivBuf = new byte[8792];

        try {
            serverSocket = new ServerSocket(serverPort);
            while (true) {
                Socket clientSocket = serverSocket.accept();
//                SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
//                System.out.println("Receive client IP: " + clientAddress);
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                while ( (recvMsgSize = in.read(receivBuf)) != -1 ) {
                    String receivedData = new String(receivBuf, StandardCharsets.UTF_8);
                    System.out.println("Data sent from client: " + receivedData);
                    out.write(receivBuf, 0, recvMsgSize);
                }
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void pushDataToClient(byte[] dataBuf) {
        int serverPort = 4700;

        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("Server binds to port: " + serverPort);
                Socket clientSocket = serverSocket.accept();
//                SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
//                System.out.println("Receive client IP: " + clientAddress);
                OutputStream out = clientSocket.getOutputStream();

                out.write(dataBuf, 0, dataBuf.length);

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
