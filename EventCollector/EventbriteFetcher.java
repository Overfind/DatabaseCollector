package EventCollector;

import DatabaseHandler.DatabaseHandler;
import DatabaseHandler.Event;
import DatabaseHandler.EventList;
import Debug.Debugger;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EventbriteFetcher implements EventFetcher // TODO public into nothing
{
    private final String EB_API = "https://www.eventbriteapi.com";
    private final String OAUTH_TOKEN = "WLXBINT76YVZTK6XLCE2";
    
    private final DateFormat dateFormat;
    
    // Parameters
    private String sdate_range_start;
    private String sdate_range_end;
    private String edate_range_start;
    private String edate_range_end;

    public EventbriteFetcher()
    {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public void setStartDateRange(Date start, Date end)
    {
        // Parameters
        if(start != null)
            this.sdate_range_start = dateFormat.format( start );
        if(end != null)
            this.sdate_range_end = dateFormat.format( end );  
    }
    
    public void setEndDateRange(Date start, Date end)
    {
        // Parameters
        if(start != null)
            this.edate_range_start = dateFormat.format( start );
        if(end != null)
            this.edate_range_end = dateFormat.format( end );  
    }
    
    @Override
    public EventList fecthEventList() 
    {
        iterateEvents( );
        
        return null;
    }
    
    private boolean iterateEvents()
    {
        String getURL = EB_API + "/v3/events/search/?token=" + OAUTH_TOKEN;
        getURL += "&expand=organizer,venue";
        
        // Start Date Range
        if( sdate_range_start != null )
            getURL += "&start_date.range_start=" + sdate_range_start;
        if( sdate_range_end != null )
            getURL += "&start_date.range_end=" + sdate_range_end;
        
        // End Date Range
        if( edate_range_start != null )
            getURL += "&end_date.range_start=" + edate_range_start;
        if( edate_range_end != null )
            getURL += "&end_date.range_end=" + edate_range_end;
        
        
        System.out.println(getURL);
        
        
        try
        {
            // Get JSON
            HttpClient client = HttpClientBuilder.create().disableCookieManagement().build();
            HttpGet getMethod = new HttpGet( getURL );
            HttpResponse response = client.execute(getMethod);
            String responseStr = EntityUtils.toString(response.getEntity());
            JSONObject rootJSON = (JSONObject) new JSONParser().parse(responseStr);

            // Pagination
            JSONObject pagination = (JSONObject) rootJSON.get( "pagination" );
            int page_size = ((Long) pagination.get( "page_size" )).intValue();
            int page_count = ((Long) pagination.get( "page_count" )).intValue();

            String str = page_count + "x" + page_size;
            Debug.Debugger.getInstance().printEventCollector(
                    Debugger.EVENT_COLLECTOR.PAGINATOR, str);
            
            for(int i = 1; i <= page_count; i++) // Try until done?
            {
                getEvents( getURL, i, page_size );
            }
            
            return true;
        }
        catch (IOException ex)
        {
            System.err.println( "Eventbrite Fetcher fecthEventList (IO):\n" + ex );
        }
        catch (ParseException ex)
        {
            System.err.println( "Eventbrite Fetcher fecthEventList (Parse):\n" + ex );
        }
        return false;
    }
    
    private boolean getEvents( String getURL, int pace_number, int page_size )
                               throws IOException, ParseException
    {
        getURL += "&page=" + pace_number;
        
        HttpClient client = HttpClientBuilder.create().disableCookieManagement().build();
        HttpGet getMethod = new HttpGet( getURL );
        HttpResponse response = client.execute(getMethod);
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject rootJSON = (JSONObject) new JSONParser().parse(responseStr);

        EventList eventList = new EventList( page_size );

        // Events
        JSONArray events = (JSONArray) rootJSON.get( "events" );
        JSONObject tempObj;
        for(Object obj: events.toArray())
        {
            tempObj = (JSONObject) obj;
            
            // API ID
            String api_id = (String) tempObj.get("id");

            // Name
            String name = (String) ((JSONObject) tempObj.get("name")).get("text");
            if(name == null) name = "";
            name = name.replace("\"", "\'").replace("\\", "/");

            // Description
            String description = (String) ((JSONObject) tempObj.get("description")).get("text");
            if(description == null) description = "";
            description = description.replace("\"", "\'").replace("\\", "/");

            // URL
            String url = (String) tempObj.get("url");
            if(url == null) url = "";
            url = url.replace("\"", "\'").replace("\\", "/");

            // Capacity
            int capacity = ((Long) tempObj.get("capacity")).intValue();

            // Logo URL
            String logoURL = null;
            try
            {
                logoURL = (String) ((JSONObject) tempObj.get("logo")).get("url");
                if(logoURL == null) logoURL = "";
                logoURL = logoURL.replace("\"", "\'");
            }
            catch(Exception e) {}

            // Is Free
            Boolean is_free = (Boolean) tempObj.get("is_free");

            // Status
            String status = (String) tempObj.get("status");
            if(status == null) status = "";
            status = status.replace("\"", "\'");
            
            
            // Start Date
            Date startdate = null;
            try
            {
                startdate = dateFormat.parse((String) ((JSONObject) tempObj.get("start")).get("utc"));
            }
            catch (java.text.ParseException ex)
            {
                System.err.println( "Eventbrite Fetcher startdate (Parse):\n" + ex );
            }

            // End Date
            Date enddate = null;
            try
            {
                enddate = dateFormat.parse((String) ((JSONObject) tempObj.get("end")).get("utc"));
            }
            catch (java.text.ParseException ex)
            {
                System.err.println( "Eventbrite Fetcher enddate (Parse):\n" + ex );
            }
            
            // Venue
            String city = null, address = null, x = null, y = null;
            try
            {
                JSONObject addressObj = (JSONObject) ((JSONObject) tempObj.get("venue")).get("address");
                city = (String) addressObj.get("city");
                if(city == null) city = "";
                city = city.replace("\"", "\'");

                address = (String) addressObj.get("localized_address_display");
                if(address == null) address = "";
                address = address.replace("\"", "\'");

                x = (String) addressObj.get("longitude");

                y = (String) addressObj.get("latitude");
            }catch(NullPointerException e){}
            

            
            
            // Oranizor ID
            String organizer_id = (String) tempObj.get("organizer_id");
            if(organizer_id == null) organizer_id = "";
            organizer_id = organizer_id.replace("\"", "\'");
            
            // Oranizor Name
            String organizer_name = (String) ((JSONObject) tempObj.get("organizer")).get("name");
            if(organizer_id == null) organizer_id = "";
            organizer_id = organizer_id.replace("\"", "\'");

            eventList.add( new Event(
                     api_id,
                     name,
                     description,
                     url,
                     capacity,
                     logoURL,
                     is_free,
                     status,
 
                     startdate,
                     enddate,

                     city,
                     address,
                     x,
                     y,

                     organizer_id,
                     organizer_name
             ) );
        }

        DatabaseHandler.getInstance().createEventList( eventList );
        
        Debug.Debugger.getInstance().printEventCollector(Debugger.EVENT_COLLECTOR.URL, getURL);
        
        return true;
    }
}
