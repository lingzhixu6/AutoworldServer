import java.net.ServerSocket;
import java.net.Socket;

public class MTSock {
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(4700);
            int clientNum = 0;
            System.out.println("Server Started ....");
            while (true) {
                clientNum++;
                Socket clientSocket = serverSocket.accept();  //serverSocket accept the client connection request
                System.out.println("Client No:" + clientNum + " started!");
                ClientThread clientThread = new ClientThread(clientSocket, clientNum); //send  the request to a separate thread
                clientThread.start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
