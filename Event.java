package DatabaseHandler;

import java.util.Date;
import org.neo4j.driver.v1.types.Node;

public class Event
{
    int id;
    String api_id;
    String name;
    String description;
    String url;
    int capacity;
    String logoURL;
    Boolean is_free;
    String status;
    
    Date startdate;
    Date enddate;
    
    Venue venue;
    
    String organizer_id;
    String organizer_name;
    
    
    public class Venue
    {
        String city;
        String address;
        String x;
        String y;
    }
    
    public Event(String api_id,
                 String name,
                 String description,
                 String url,
                 int capacity,
                 String logoURL,
                 Boolean is_free,
                 String status,
                 
                 Date startdate,
                 Date enddate,
                
                 String city,
                 String address,
                 String x,
                 String y,
                 
                 String organizer_id,
                 String organizer_name)
    {
        this.id = -1;
        this.api_id = api_id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.capacity = capacity;
        this.logoURL = logoURL;
        this.is_free = is_free;
        this.status = status;
        
        this.startdate = startdate;
        this.enddate = enddate;
        
        this.venue =  new Venue();
        this.venue.city = city;
        this.venue.address = address;
        this.venue.x = x;
        this.venue.y = y;
        
        this.organizer_id = organizer_id;
        this.organizer_name = organizer_name;
    }
    
    public void setID( int id )
    {
        if( this.id < 0 )
            this.id = id;
    }
    
    public Event(int id, Node node)
    {
        this.id = id;
        
        this.venue = new Venue();
        
        node.asMap().entrySet().forEach((pair) ->
        {
            switch( pair.getKey() )
            {
                case "api_id":
                    this.api_id = (String) pair.getValue();
                    break;
                case "name":
                    this.name = (String) pair.getValue();
                    break;
                case "description":
                    this.description = (String) pair.getValue();
                    break;
                case "url":
                    this.url = (String) pair.getValue();
                    break;
                case "capacity":
                    this.capacity = (int) pair.getValue();
                    break;
                case "logoURL":
                    this.logoURL = (String) pair.getValue();
                    break;
                case "is_free":
                    this.is_free = (boolean) pair.getValue();
                    break;
                case "status":
                    this.status = (String) pair.getValue();
                    break;
                    
                case "startdate":
                    this.startdate = new Date((Long) pair.getValue());
                    break;
                case "enddate":
                    this.enddate = new Date((Long) pair.getValue());
                    break;
                    
                case "organizer_id":
                    this.organizer_id = (String) pair.getValue();
                    break;
                case "organizer_name":
                    this.organizer_name = (String) pair.getValue();
                    break;
                    
                default:
                    break;
            }
        });
    }
    
    @Override
    public String toString()
    {
        return "id:\t" + id
           + "\nAPI id:\t" + name
           + "\nTitle:\t" + name;
    }
}