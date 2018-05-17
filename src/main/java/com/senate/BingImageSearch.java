package com.senate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.senate.apiobjects.Image;
import com.senate.apiobjects.Images;

import java.net.*;
import java.util.*;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;

/*
 * Gson: https://github.com/google/gson
 * Maven info:
 *     groupId: com.google.code.gson
 *     artifactId: gson
 *     version: 2.8.1
 *
 * Once you have compiled or downloaded gson-2.8.1.jar, assuming you have placed it in the
 * same folder as this file (BingImageSearch.java), you can compile and run this program at
 * the command line as follows.
 *
 * javac BingImageSearch.java -classpath .;gson-2.8.1.jar -encoding UTF-8
 * java -cp .;gson-2.8.1.jar BingImageSearch
 */

class BingImageSearch {

    // Replace the subscriptionKey string value with your valid subscription key.


    // Verify the endpoint URI.  At this writing, only one endpoint is used for Bing
    // search APIs.  In the future, regional endpoints may be available.  If you
    // encounter unexpected authorization errors, double-check this value against
    // the endpoint for your Bing Web search instance in your Azure dashboard.
    private static String host = "https://api.cognitive.microsoft.com";
    private static String path = "/bing/v7.0/images/search";


    //search method
    private static SearchResults SearchImages(String searchQuery) throws Exception {
        // construct URL of search request (endpoint + query string)
        URL url = new URL(host + path + "?q=" +  URLEncoder.encode(searchQuery, "UTF-8")+"&safeSearch=Off");
        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", Keys.bingKey);

        // receive JSON body
        InputStream stream = connection.getInputStream();
        String response = new Scanner(stream).useDelimiter("\\A").next();

        // construct result object for return
        SearchResults results = new SearchResults(new HashMap<>(), response);

        // extract Bing-related HTTP headers
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String header : headers.keySet()) {
            if (header == null) continue;      // may have null key
            if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
                results.relevantHeaders.put(header, headers.get(header).get(0));
            }
        }

        stream.close();
        return results;
    }

    // pretty-printer for JSON; uses GSON parser to parse and re-serialize
    private static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(json_text).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    static String search(String query) {
        if (Keys.bingKey.length() != 32) {
            System.out.println("Invalid Bing Search API subscription key!");
            System.out.println("Please paste yours into the source code.");
            System.exit(1);
        }

        try {
            System.out.println("Searching the Web for: " + query);

            SearchResults result = SearchImages(query);

//            System.out.println("\nRelevant HTTP Headers:\n");
//            for (String header : result.relevantHeaders.keySet())
//                System.out.println(header + ": " + result.relevantHeaders.get(header));

            //System.out.println("\nJSON Response:\n");
            //System.out.println(prettify(result.jsonResponse));

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Images bingResponse = gson.fromJson(prettify(result.jsonResponse), Images.class);

            int randomImage = (int)(bingResponse.value.length*Math.random());


            for(Image img : bingResponse.value){
                System.out.println(img.contentUrl);
            }
            System.out.println(randomImage + " " +bingResponse.value.length);
            if(bingResponse.value.length == 0) return "you fucked it up";
            else return bingResponse.value[randomImage].contentUrl;

        }
        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }

        return "heck";
    }
}

// Container class for search results encapsulates relevant headers and JSON data
class SearchResults{
    HashMap<String, String> relevantHeaders;
    String jsonResponse;
    SearchResults(HashMap<String, String> headers, String json) {
        relevantHeaders = headers;
        jsonResponse = json;
    }
}