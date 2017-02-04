package nvgtt.data.db.datafeeder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;


public class WikipediaApi {
	
	static String wikipediaApiUrl = ".wikipedia.org/w/api.php";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			getAbstractLinks("Tibia_(video_game)", "en");
		} catch(Exception e) {
			print(e.getMessage());
		} finally {
			
			
		}
	}
	
	public static WikipediaPageData getAbstractLinks(final String page, String lang) throws HttpRequestException, Exception {

	    //Obs.: Page must already been encoded

	    String reqUrl = "https://" + lang + wikipediaApiUrl + "?action=parse&redirects&section=0&prop=text&format=json&page=" + page;

	    HttpRequest request = HttpRequest.get(reqUrl);
	    
	    if(request.code() != 200)
	    	throw new Exception("Failed in downloading " + page + " Code: " + request.code());
	  

        //Parse json object that contains only the abstract portion of the page
		JSONObject reqObj = new JSONObject(request.body());
		
        //Check some error
        if(reqObj.has("error"))
        	throw new Exception("ERROR:" + reqObj.getJSONObject("error").get("code") + " | " + reqObj.getJSONObject("error").get("info"));

        //Get page info
        String pageTitle = reqObj.getJSONObject("parse").getString("title");
        long pageId = reqObj.getJSONObject("parse").getLong("pageid");

        
        //Get the abstract html data
        String htmlData = reqObj.getJSONObject("parse").getJSONObject("text").getString("*");
        
        //Load it on html parser
        Document doc = Jsoup.parse(htmlData);
        
        //String list to store links
        ArrayList<String> links = new ArrayList<String>();
        
        
        //Get all the <a> tags inside the <p> tags,(where the abstract is placed) and put them into the links array
        for (Element pTag : doc.getElementsByTag("p")) {
        	for (Element aTag : pTag.getElementsByTag("a")) {
        		String link = aTag.attr("href");

	            //var notAllowedChars

	            //Check the link exists and is a wiki link
	            //Get only wikipedia links
	            //Remove pages that contains a colon (":"). Their offen are special pages. Not sure if there is articles with colon
	            if(link != ""
	                && link.indexOf("/wiki/") == 0 
	                && link.indexOf(":") == -1
	            ) { 
	                //We MUST NOT use last index of / to get the path cause some titles like TCP/IP, have bar in the title
	                //var lastPathIndex = link.lastIndexOf("/") + 1;
	                //We should use the '/wiki/' string length
	                String linkName = link.substring(6);

	                //Remove hashtag from url if any
	                int hashIndex = linkName.indexOf("#");
	                if(hashIndex != -1)
	                    linkName = linkName.substring(0, hashIndex);

	                //If the link is not in the links array, push it 
	                if(links.indexOf(linkName) == -1)
	                    links.add(linkName);
	            }  
        		
        	}
        }        
        
        //print(pageTitle);
        //print(pageId);
        //print("");
        
        //for(String l : links) {
        	//print(l);
        //}
        
		return new WikipediaPageData(pageTitle, page, pageId, links);
	}
	
	static void print(Object x) {
		System.out.println(x);
	}
	
	
}
	    
	    
	    
	    
	    
	    
	    
	    /*
	    simpleHttpsGet(reqUrl, function(error, reqData) {

	        if(error) {
	            callback("ERROR:" + error);
	            return;
	        }

	        //Parse json object that contains only the abstract portion of the page
	        var reqObj = JSON.parse(reqData);            


	        //Check some error
	        if(reqObj['error']) {
	            //Throw reject error
	            callback("ERROR:" + reqObj['error']['code'] + " | " + reqObj['error']['info']);
	            return;
	        }

	        //Get page info
	        var pageTitle = reqObj['parse']['title'];
	        var pageId = reqObj['parse']['pageid'];

	        //Check if page has subtitle
	        //Regex get the data inside some () found
	        //var subtitleMatch = pageTitle.match(/\((.+)\)/)
	        //print(testString.match(/\((.+)\)/));

	        //Get the abstract html data
	        var htmlData = reqObj['parse']['text']['*']
	    
	        //Load it on cheerio (jQuery like module)
	        $ = cheerio.load(htmlData);

	        var links = []

	        //Get all the <a> tags inside the <p> tags,(where the abstract is placed) and put them into the links array
	        $('a', 'p').each(function(i, elem) {
	                
	            var link = $(this).attr('href');

	            //var notAllowedChars

	            //Check the link exists and is a wiki link
	            //Get only wikipedia links
	            //Remove pages that contains a colon (":"). Their offen are special pages. Not sure if there is articles with colon
	            if(link 
	                && link.indexOf("/wiki/") == 0 
	                && link.indexOf(":") == -1
	            ) { 
	                //We MUST NOT use last index of / to get the path cause some titles like TCP/IP, have bar in the title
	                //var lastPathIndex = link.lastIndexOf("/") + 1;
	                //We should use the '/wiki/' string length
	                var linkName = link.substring(6);

	                //Remove hashtag from url if any
	                var hashIndex = linkName.indexOf("#");
	                if(hashIndex != -1)
	                    linkName = linkName.substring(0, hashIndex);

	                //If the link is not in the links array, push it 
	                if(links.indexOf(linkName) == -1)
	                    links.push(linkName);
	            }                
	        });

	        //Return success with the page abstract links
	        callback(null, {
	            title: pageTitle,
	            pageId: pageId,
	            links: links    
	        });     

	    });*/
