import Database.DataBridge;
import IBM.DiscoveryNews;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.net.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestSocket {

    Socket clientSocket;

    public void run() {

        int serverPort = 4700;
        ServerSocket serverSocket = null;


        try {
            serverSocket = new ServerSocket(serverPort);
            while (true) {
                byte[] receivBuf = new byte[10000];
                clientSocket = serverSocket.accept();
//                SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
//                System.out.println("Receive client IP: " + clientAddress);
                InputStream in = clientSocket.getInputStream();

                //不能使用while。我现在只能实现一问一答的模式。这里默认read一次就可以把client的消息全部读进buffer
                if ( in.read(receivBuf) != -1 )  //read from input stream and stores into buffer. Return -1 when reaching the end of inputstream
                {
                    String receivedData = new String(receivBuf, StandardCharsets.UTF_8).trim();
                    String[] splitCode =  receivedData.split(",", 2);
                    System.out.println(Arrays.toString(splitCode));
                    switchResponse(splitCode);
                }
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap ParseJson(String json){
        Gson g = new Gson();
        return g.fromJson(json, HashMap.class);
    }

    private void switchResponse(String[] receivedData) throws IOException, SQLException {
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
                break;
            case "0001":{
                HashMap details = ParseJson(data);
                String email = details.get("email").toString();
                String company = details.get("company").toString();
                String password = details.get("hashedPassword").toString();
                String salt = details.get("salt").toString();
                boolean RegisterStatus = d.WritePlayer(email, company, password, salt);
                if (RegisterStatus){
                    out.write("1".getBytes(StandardCharsets.UTF_8));
                }
                else{
                    out.write("0".getBytes(StandardCharsets.UTF_8));
                }
                break;}
            case "0002":{
                HashMap authDetails = ParseJson(data);
                String authEmail = authDetails.get("email").toString();
                String authPassword = authDetails.get("password").toString();
                boolean AuthStatus = d.AuthPlayer(authEmail, authPassword);
                if (AuthStatus){
                    out.write("1".getBytes(StandardCharsets.UTF_8));
                }
                else{
                    out.write("0".getBytes(StandardCharsets.UTF_8));
                }
                break;
            }
            case "0003":{
                HashMap details = ParseJson(data);
                String loggedInEmail = details.get("email").toString();
                out.write(d.companyDetails(loggedInEmail).getBytes(StandardCharsets.UTF_8));
                break;
            }
            case "5000": {
                HashMap EmployeeDetails = ParseJson(receivedData[1]);
                int companyId = Integer.parseInt(EmployeeDetails.get("CompanyId").toString());
                String Records = d.GetEmployeeRecords(companyId);
                out.write(Records.getBytes(StandardCharsets.UTF_8));
                break;
            }
            case "5001":{
                HashMap EmployeeDetails = ParseJson(receivedData[1]);
                int companyId = Integer.parseInt(EmployeeDetails.get("CompanyId").toString());
                String type = EmployeeDetails.get("EmployeeType").toString();
                d.IncrementEmployeeQuantity(companyId, type);
                break;
            }
            default:
                break;
        }
    }

}
