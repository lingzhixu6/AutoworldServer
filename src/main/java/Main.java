import Database.DataBridge;

public class Main {

    public static void main(String[] args) throws Exception{
//        InetAddress ip = InetAddress.getLocalHost();
//        System.out.println(ip.getHostAddress());

        DataBridge dataBridge = new DataBridge();
        dataBridge.createSqlTables();
    }
}
