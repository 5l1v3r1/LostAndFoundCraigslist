package android.lostandfound;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 *      Important words: colors (white, black, tan, etc.) should have higher weight
 *                       area found (distance from area lost to area found + time originally lost)
 *
 *      http://try.jsoup.org/~LGB7rk_atM2roavV0d-czMt3J_g
 *
 *
 *
 */



public class LostAndFound extends Activity {

    // --------------------- GLOBAL VARIABLES -------------------------
    public final String TAG = "LostAndFound";
    static final String URL = "https://sfbay.craigslist.org/eby/pet/";
    static final String NextPage = "index100.html"; //Note: nextNextPage is "index200.html"
    static ArrayList<String> foundArray = new ArrayList<String>();
    static ArrayList<String> lostArray = new ArrayList<String>();
    boolean bothHaveImages = false, foundImage = false, lostImage = false;

    // ---------------------         HASHMAP STRUCTURE (Found and Lost)  -------------------------------
    // --------------------- {DataId: [Associated, Words, That, Are, In, Post]} ------------------------
    HashMap<String, String> foundAnimals = new HashMap<String, String>();
    HashMap<String, String> lostAnimals = new HashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_and_found);




    }

    @Override
    protected void onStart() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lost_and_found, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ScrapeMainURL() {
        /**
         *  Example of Craigslist HTML we are parsing for
         *
         * <a href="/eby/pet/4628433246.html"
         *      data-id="4628433246"
         *          class="hdrlnk">Most Mellow Kid-Centered Kittens Ever! 14 Weeks Old!</a>
         */
        String date = ""; //<span class="date">Aug 20</span>
        Document doc = null;
        Elements search = null;

        try {
            doc = Jsoup.connect(URL).get();
            //date = doc.select("span[class=date]").toString(); Just grabs ALL Dates.
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (doc != null) {
            search = doc.select("a[href]");
        }

        //Determine if "Found" or "Lost" in title
        for (Element searches : search != null ? search : null) {
            boolean found = searches.toString().toLowerCase().contains("found");
            boolean lost = searches.toString().toLowerCase().contains("lost");
            if(found) {
                //searches will point to correct row we need to grab data-id from
                String key = searches.getElementsByAttribute("a[data-id]").toString(); //might just be "[data-id]"
                scrapePageLinks(key, true);
                for(String k : foundArray) {
                    foundAnimals.put(key, k.toLowerCase());
                }
            }
            else if(lost) {
                String key = searches.getElementsByAttribute("a[data-id]").toString();
                scrapePageLinks(key, false);
                for(String k : lostArray) {
                    lostAnimals.put(key, k.toLowerCase());
                }
            }
        }
        analyzeMatches();
    }

    void analyzeMatches() {

        final String[] WEIGHTED_WORDS = new String[]{"black", "tan", "red", "white", "brown", "cream", "yellow",
                                                          "orange" };

        String foundTitle = "";
        String lostTitle = "";
        int foundLen = foundArray.size();
        int lostLen = lostArray.size();

        foundTitle = foundArray.get(foundLen - 1);
        lostTitle = lostArray.get(lostLen - 1);

        //---------------------- Image recognition algorithms here. -------------------------------
        if (bothHaveImages) { //if both lost && found posts have images.




        }

        // --------------------- MATCHING ALGORITHMS -----------------------------
        /**
         *   - If good matches --> (determine distance found - distance lost + time lost + time found)
         *   - NOTE: foundArray && lostArray -> [size-2] == URL, [size-1] == Title
         *
         *
         *
         *
         */










        //TODO: -------- FINISH (clear all data structures after done). -----------
        foundArray.clear();
        lostArray.clear();
        foundAnimals.clear();
        lostAnimals.clear();
    }

    void scrapePageLinks(String key, boolean isFound) {
        Document foundPage = null;
        Elements elem = null;
        String title = null;
        String imageLink = null;
        String foundURL = URL + key + ".html";
        String newImageLink = "";
        try {
            foundPage = Jsoup.connect(foundURL).get();
        }catch(IOException e) {
            Log.e(TAG, "Could not open Found Link.");
            e.printStackTrace();
        }

        if (foundPage != null) {
            elem = foundPage.select("section[id=postingbody]");
            //content="Please help me get Charlie Murphy back home. He is a 6lb chihuahua all black with white chest and paws. Contact me if you know where he is or have him safe and sound at 510-761-2714 or 510-693-3736!...">
            imageLink = foundPage.select("meta[property=og:image]").toString(); //<meta property="og:image" content="http://images.craigslist.org/00k0k_5AaB0dMT6og_600x450.jpg">
            title = foundPage.title();
        }

        //Now we need to Strip the Extra content we don't need that was scraped
        if (imageLink != null) {
           int startIndex = imageLink.indexOf("<"); //Really just index[0].
           int endIndex = imageLink.indexOf("h");
           String toBeReplaced = imageLink.substring(startIndex, endIndex - 1); //http://images.craigslist.org/00k0k_5AaB0dMT6og_600x450.jpg">
           toBeReplaced = toBeReplaced.replace("\">", "");
           newImageLink = toBeReplaced;
        }

        String bodyOfPage = "";
        if(isFound && elem != null) {
            bodyOfPage = elem.toString();
            foundArray.add(Arrays.asList(bodyOfPage).toString()); //TODO: Dunno about this!
            foundArray.add(newImageLink);
            foundArray.add(title);
            foundImage = true;
//        for(int i = 0; i < populateFound.length(); i++)
//            foundArray.add(populateFound[i].toString());
        }
        else if(!isFound && elem != null) {
            bodyOfPage = elem.toString();
            lostArray.add(Arrays.asList(bodyOfPage).toString());
            lostArray.add(newImageLink);
            lostArray.add(title);
            lostImage = true;
        }
        if(lostImage && foundImage) {
            bothHaveImages = true;
        }
    }

}


