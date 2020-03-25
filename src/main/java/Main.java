import IBM.DiscoveryNews;
import IBM.ToneAnalyser;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;

import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws Exception{
        InetAddress ip = InetAddress.getLocalHost();
        System.out.println(ip.getHostAddress());
        new MTSock().run();

    }
}

