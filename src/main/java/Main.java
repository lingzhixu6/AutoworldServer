import IBM.DiscoveryNews;

import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws Exception{
        InetAddress ip = InetAddress.getLocalHost();
        System.out.println(ip.getHostAddress());

        new MTSock().run();
    }

}
