package IBM;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.discovery.v1.Discovery;
import com.ibm.watson.discovery.v1.model.*;
import com.google.gson.JsonArray;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.*;

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
        return makeTitleAndUrlJsonArray(queryResponse.toString()).getBytes(StandardCharsets.UTF_8);
    }

    public String makeTitleAndUrlJsonArray(String rawJsonNews) {
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

    public static String extractNewsTitle(String rawJsonNews, int newsDocCount) {
        JsonElement jsonTreeRoot = parseRawJsonIntoJsonTree(rawJsonNews);
        JsonObject newsJsonObj =jsonTreeRoot.getAsJsonObject();
        JsonElement newsTitle = newsJsonObj.get("results").getAsJsonArray()
                .get(newsDocCount).getAsJsonObject()
                .get("title");
        return  newsTitle.toString();
    }

    public static String extractNewsUrl(String rawJsonNews, int newsDocCount) {
        JsonElement jsonTreeRoot = parseRawJsonIntoJsonTree(rawJsonNews);
        JsonObject newsJsonObj =jsonTreeRoot.getAsJsonObject();
        JsonElement newsUrl = newsJsonObj.get("results").getAsJsonArray()
                .get(newsDocCount).getAsJsonObject()
                .get("url");
        return  newsUrl.toString();
    }

    public static JsonElement parseRawJsonIntoJsonTree(String rawJsonNews) {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(rawJsonNews);
        return jsonTree;
    }

    public static String newsTitles(){
        Discovery discovery = new Discovery("2020-03-09");

        String environmentId = "system";
        String collectionId = "news-en";

       // System.out.println("Querying the collection...");
        QueryOptions.Builder queryBuilder = new QueryOptions.Builder(environmentId, collectionId);
        queryBuilder.naturalLanguageQuery("autonomous car");
        queryBuilder.filter("text:autonomous car");
        QueryResponse queryResponse = discovery.query(queryBuilder.build()).execute().getResult();

        StringBuilder titles = new StringBuilder();
        for(int i = 0; i < 10; i++){
            titles.append(extractNewsTitle(queryResponse.toString(), i).replace("\"", ""));
            titles.append("\n");
        }
        return titles.toString();
    }


}
