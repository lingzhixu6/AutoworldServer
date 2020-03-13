package ServerSocket;

import IBM.DiscoveryNews;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientThread extends Thread {

    DataOutputStream outStream;
    DataInputStream inStream;
    Socket clientSocket;
    int clientNo;
    byte[] receivBuf = new byte[8792];


    ClientThread(Socket inSocket, int counter) {
        clientSocket = inSocket;
        clientNo = counter;
    }

    public void run() {
        try {
            inStream = new DataInputStream(clientSocket.getInputStream());
            outStream = new DataOutputStream(clientSocket.getOutputStream());

            inStream.read(receivBuf);
            String clientMsg = new String(receivBuf, StandardCharsets.UTF_8).trim();

            if (!extractOpcode(clientMsg).equals("9999")) {
                System.out.println("From Client-" + clientNo + ": Number is :" + clientMsg);
                switchResponse(clientMsg);
            }
            inStream.close();
            outStream.close();
            clientSocket.close();
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            System.out.println("Client: " + clientNo + " exit!! ");
            System.out.println();       //To separate print statements made by different clients
        }
    }

    private void switchResponse(String clientMsg) throws IOException {

        switch(extractOpcode(clientMsg)) {
            case "0010":
                outStream.write(new DiscoveryNews().queryNewsAndGetDesiredResult());
                break;
            default:
                break;
        }
    }

    private String extractOpcode(String clientMsg) {
        return clientMsg.substring(0,4);
    }
}
