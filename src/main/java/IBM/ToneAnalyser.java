package IBM;

import Database.DataBridge;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.discovery.v1.Discovery;
import com.ibm.watson.discovery.v1.model.QueryOptions;
import com.ibm.watson.discovery.v1.model.QueryResponse;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;

import java.util.Arrays;
import java.util.HashMap;

public class ToneAnalyser {
    public String confidenceScore(){
        IamAuthenticator authenticator = new IamAuthenticator("qxTJ9SSb03_7H4L5T8PWclKQ27zcaszKpf1WJOwP4uRL");
        ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2017-09-21", authenticator);
        toneAnalyzer.setServiceUrl("https://api.eu-gb.tone-analyzer.watson.cloud.ibm.com/instances/5bf913d0-4c7c-4eb9-a179-e70c69dc318f");

//        DiscoveryNews d = new DiscoveryNews();
//        Discovery discovery = new Discovery("2020-03-09");
//
//        String environmentId = "system";
//        String collectionId = "news-en";
//
//        QueryOptions.Builder queryBuilder = new QueryOptions.Builder(environmentId, collectionId);
//        queryBuilder.naturalLanguageQuery("autonomous car");
//        queryBuilder.filter("text:autonomous car");
//        QueryResponse queryResponse = discovery.query(queryBuilder.build()).execute().getResult();

        //System.out.println(DiscoveryNews.newsTitles());
        String[] titlesArray = DiscoveryNews.newsTitles().split("\n");
        //System.out.println(titlesArray[0]);
        String text = DiscoveryNews.newsTitles();
        ToneOptions toneOptions = new ToneOptions.Builder()
                .text(text)
                .build();

        ToneAnalysis toneAnalysis = toneAnalyzer.tone(toneOptions).execute().getResult();
        //System.out.println(toneAnalysis.toString());

        JsonElement jsonTreeRoot = DiscoveryNews.parseRawJsonIntoJsonTree(toneAnalysis.toString());
        JsonObject documentTones = jsonTreeRoot.getAsJsonObject();
        JsonArray tones = documentTones.getAsJsonObject("document_tone").getAsJsonArray("tones");

        int scoreChange = 0;
        //System.out.println(tones.size());
        for(int i = 0; i < tones.size(); i++){
            String toneName = tones.get(i).getAsJsonObject().get("tone_id").toString();
            //System.out.println(toneName);
            if(toneName.equals("anger") || toneName.equals("fear") || toneName.equals("sadness")){
                scoreChange = -1;
            } else if(toneName.equals("joy") || toneName.equals("confident")){
                scoreChange = 1;
            }
        }
        //System.out.println(scoreChange);
        String returnString = "{\"change\":\""+scoreChange+"\",\"title\":\""+titlesArray[0]+"\"}";
        //System.out.println(returnString);
        return returnString;
    }

}
