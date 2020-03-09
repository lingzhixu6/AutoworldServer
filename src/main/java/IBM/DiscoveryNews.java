package IBM;

import com.ibm.watson.discovery.v1.Discovery;
import com.ibm.watson.discovery.v1.model.*;


public class DiscoveryNews
{
    public byte[] testDiscoveryNews() {
        Discovery discovery = new Discovery("2020-03-09");

        String environmentId = "system";
        String collectionId = "news-en";

        System.out.println("Querying the collection...");
        QueryOptions.Builder queryBuilder = new QueryOptions.Builder(environmentId, collectionId);
        queryBuilder.query("text");
        QueryResponse queryResponse = discovery.query(queryBuilder.build()).execute().getResult();

//        System.out.println("Query Results:");
//        System.out.println(queryResponse);

        return queryResponse.toString().getBytes();

    }
}
