import Database.DataBridge;
import IBM.DiscoveryNews;

import java.net.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


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
                    System.out.println(receivedData);
                    String[] splitCode =  receivedData.split(",", 2);
                    System.out.println(Arrays.toString(splitCode));
                    System.out.println(splitCode.length);
                    switchResponse(splitCode);
                }
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchResponse(String[] receivedData) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        DataBridge d = new DataBridge();
        String opCode = receivedData[0];
        String data = receivedData[1];
        switch (opCode) {
            case "news":
                out.write(new DiscoveryNews().queryNewsAndGetDesiredResult());
                break;
            case "0000":
                d.createTables();

            default:
                break;
        }
    }

}
