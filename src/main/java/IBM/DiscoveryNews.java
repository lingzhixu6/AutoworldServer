package IBM;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.discovery.v1.Discovery;
import com.ibm.watson.discovery.v1.model.*;
import com.google.gson.JsonArray;

import java.nio.charset.StandardCharsets;


public class DiscoveryNews
{
    public byte[] queryNewsAndGetDesiredResult() {
        Discovery discovery = new Discovery("2020-03-09");

        String environmentId = "system";
        String collectionId = "news-en";

        System.out.println("Querying the collection...");
        QueryOptions.Builder queryBuilder = new QueryOptions.Builder(environmentId, collectionId);
        queryBuilder.naturalLanguageQuery("autonomous car");
        queryBuilder.filter("text:autonomous car");
        QueryResponse queryResponse = discovery.query(queryBuilder.build()).execute().getResult();

        System.err.println(queryResponse.toString());

        return makeTitleAndUrlJsonArray(queryResponse.toString()).getBytes(StandardCharsets.UTF_8);
    }

    private String makeTitleAndUrlJsonArray(String rawJsonNews) {
        JsonArray titleAndUrlArray = new JsonArray();
        for(int newsDocCount = 0; newsDocCount < 10; newsDocCount++) {
            String title = extractNewsTitle(rawJsonNews, newsDocCount).replace("\"", "");
            String url = extractNewsUrl(rawJsonNews, newsDocCount).replace("\"", "");
            JsonObject doc = new JsonObject();
            doc.addProperty("title", title);
            doc.addProperty("url", url);
            titleAndUrlArray.add(doc);
        }
        return titleAndUrlArray.toString();
    }

    private String extractNewsTitle(String rawJsonNews, int newsDocCount) {
        JsonElement jsonTreeRoot = parseRawJsonIntoJsonTree(rawJsonNews);
        JsonObject newsJsonObj =jsonTreeRoot.getAsJsonObject();
        JsonElement newsTitle = newsJsonObj.get("results").getAsJsonArray()
                .get(newsDocCount).getAsJsonObject()
                .get("title");
        return  newsTitle.toString();
    }

    private String extractNewsUrl(String rawJsonNews, int newsDocCount) {
        JsonElement jsonTreeRoot = parseRawJsonIntoJsonTree(rawJsonNews);
        JsonObject newsJsonObj =jsonTreeRoot.getAsJsonObject();
        JsonElement newsUrl = newsJsonObj.get("results").getAsJsonArray()
                .get(newsDocCount).getAsJsonObject()
                .get("url");
        return  newsUrl.toString();
    }

    private JsonElement parseRawJsonIntoJsonTree(String rawJsonNews) {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(rawJsonNews);
        return jsonTree;
    }

}
