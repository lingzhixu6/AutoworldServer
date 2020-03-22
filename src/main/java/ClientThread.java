import Database.DataBridge;
import IBM.DiscoveryNews;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class ClientThread extends Thread {

    DataOutputStream outStream;
    DataInputStream inStream;
    Socket clientSocket;
    int clientNo;



    ClientThread(Socket inSocket, int counter) {
        clientSocket = inSocket;
        clientNo = counter;
    }

    public void run() {
        try {
            byte[] receivBuf = new byte[10000];
            inStream = new DataInputStream(clientSocket.getInputStream());
            outStream = new DataOutputStream(clientSocket.getOutputStream());

            inStream.read(receivBuf);
            String clientMsg = new String(receivBuf, StandardCharsets.UTF_8).trim();

            String receivedData = new String(receivBuf, StandardCharsets.UTF_8).trim();
            String[] splitCode =  receivedData.split(",", 2);
            System.out.println(Arrays.toString(splitCode));
            switchResponse(splitCode);

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
            case "0004":{
                HashMap details = ParseJson(data);
                int companyId = Integer.parseInt(details.get("companyID").toString());
                String startTime = details.get("startTime").toString();
                int sellPrice = Integer.parseInt(details.get("sellPrice").toString().substring(0, details.get("sellPrice").toString().length()-2));
                int completionTime = Integer.parseInt(details.get("completionTime").toString().substring(0, details.get("completionTime").toString().length()-2));
                int mechanics = Integer.parseInt(details.get("mechanics").toString().substring(0, details.get("mechanics").toString().length()-2));
                System.out.println(mechanics);
                d.startCarBuild(companyId, startTime, sellPrice, completionTime, mechanics);
                break;
            }
            case "0005":{
                HashMap dets = ParseJson(data);
                int companyId = Integer.parseInt(dets.get("CompanyId").toString());
                System.out.println(d.getBuildingCars(companyId));
                out.write(d.getBuildingCars(companyId).getBytes(StandardCharsets.UTF_8));
                break;
            }
            case "0006":{
                HashMap deets = ParseJson(data);
                int carID = Integer.parseInt(deets.get("carID").toString());
                int price = Integer.parseInt(deets.get("value").toString());
                int mechs = Integer.parseInt(deets.get("mechsUsed").toString());
                d.finishCar(carID, price, mechs);
                break;
            }
            case "0007":{
                HashMap deets = ParseJson(data);
                int compID = Integer.parseInt(deets.get("CompanyId").toString());
                int amountToPay = Integer.parseInt(deets.get("amountToPay").toString());
                d.payThePeople(compID, amountToPay);
                break;
            }
            case "0008":{
                HashMap details = ParseJson(data);
                int compID = Integer.parseInt(details.get("CompanyId").toString());
                out.write(d.getLastPaid(compID).getBytes(StandardCharsets.UTF_8));
                break;
            }
            case "0009":{
                HashMap details = ParseJson(data);
                int compID = Integer.parseInt(details.get("CompanyId").toString());
                out.write(d.getEmployeeCounts(compID).getBytes(StandardCharsets.UTF_8));
                break;
            }
            case "0010":{
                out.write(new DiscoveryNews().queryNewsAndGetDesiredResult());
                break;
            }
            case "0011":{
                HashMap details = ParseJson(data);
                int compID = Integer.parseInt(details.get("CompanyId").toString());
                int carsToSell = Integer.parseInt(details.get("numberToSell").toString());
                d.sellCars(compID, carsToSell);
            }
            case "0012":{
                HashMap details = ParseJson(data);
                System.out.println(details.keySet());
                int compID = Integer.parseInt(details.get("CompanyId").toString());
                out.write(d.getLastCarSold(compID).getBytes(StandardCharsets.UTF_8));
            }
            case "0013":{
                HashMap details = ParseJson(data);
                int compID = Integer.parseInt(details.get("CompanyId").toString());
                int alum = Integer.parseInt(details.get("aluminium").toString());
                int steel = Integer.parseInt(details.get("steel").toString());
                int glass = Integer.parseInt(details.get("glass").toString());
                int rubber = Integer.parseInt(details.get("rubber").toString());
                d.decrementMaterials(compID, alum, steel, glass, rubber);
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
            case "5002":{
                HashMap Details = ParseJson(receivedData[1]);
                int companyId = Integer.parseInt(Details.get("CompanyId").toString());
                String materialname = Details.get("Name").toString();
                int price = Integer.parseInt(Details.get("Price").toString());
                int quantity = Integer.parseInt(Details.get("Quantity").toString());
                d.BuyRawMaterials(companyId, materialname, price, quantity);
                break;
            }
            case "5005":{
                HashMap Details = ParseJson(receivedData[1]);
                int companyId = Integer.parseInt(Details.get("CompanyId").toString());
                int repaymentAmount = Integer.parseInt(Details.get("LoanAmount").toString());
                d.RepayLoan(companyId, repaymentAmount);
                break;
            }
            case "5006":{
                HashMap Details = ParseJson(receivedData[1]);
                int companyId = Integer.parseInt(Details.get("CompanyId").toString());
                String input = d.GetMaterialRecords(companyId);
                out.write(input.getBytes(StandardCharsets.UTF_8));
            }
            case "5007":{
                String Records = d.GetPlayerRankings();
                out.write(Records.getBytes(StandardCharsets.UTF_8));
            }
            case "5008":{
                HashMap Details = ParseJson(receivedData[1]);
                String email = Details.get("email").toString();
                String company = Details.get("company").toString();
                String input = d.CheckInputExists(email, company);
                out.write(input.getBytes(StandardCharsets.UTF_8));
            }
            case "5009":{
                HashMap Details = ParseJson(receivedData[1]);
                String email = Details.get("email").toString();
                String oldPassword = Details.get("oldPassword").toString();
                String password = Details.get("password").toString();
                String salt = Details.get("salt").toString();
                String input = d.UpdatePassword(email, oldPassword, password, salt);
                out.write(input.getBytes(StandardCharsets.UTF_8));
            }
            default:
                break;
        }
    }
}
