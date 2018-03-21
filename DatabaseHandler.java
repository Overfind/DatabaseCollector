package DatabaseHandler;

import Debug.Debugger;
import java.util.AbstractMap;
import java.util.ArrayList;
import org.neo4j.driver.v1.*;

public class DatabaseHandler
{
    private static DatabaseHandler singletonDBHandler;
    private final Driver driver;
    
    private DatabaseHandler()
    {
        String DB_PATH = "bolt://localhost:7687";
        driver = (Driver) GraphDatabase.driver(DB_PATH, AuthTokens.basic("overfind", "neo4j"));
    }
    
    /**
     * Singleton Getter
     * @return DatabaseHandler
     */
    public static DatabaseHandler getInstance()
    {
        if( singletonDBHandler == null )
            singletonDBHandler = new DatabaseHandler();
        return singletonDBHandler;
    }
    
    /**
     * Creates Event
     * @param event
     * @return the Event that created
     */
    public Event createEvent(Event event)
    {
        String stmt = "MERGE (event:Event{api_id: \"" + event.api_id + "\"})"
                    + "\nSET "
                    + "\nevent.title = \"" +       event.name + "\","
                    + "\nevent.description = \"" + event.description + "\","
                    + "\nevent.url = \"" +         event.url + "\","
                    + "\nevent.capacity = \"" +    event.capacity + "\","
                    + "\nevent.logoURL = \"" +     event.logoURL + "\","
                    + "\nevent.is_free = \"" +     event.is_free + "\","
                    + "\nevent.status = \"" +      event.status + "\","

                    + "\nevent.startdate = \"" + event.startdate + "\","
                    + "\nevent.enddate = \"" +   event.enddate + "\","

                    + "\nevent.venue_city = \"" +    event.venue.city + "\","
                    + "\nevent.venue_address = \"" + event.venue.address + "\","
                    + "\nevent.venue_x = \"" +       event.venue.x + "\","
                    + "\nevent.venue_y = \"" +       event.venue.y + "\","

                    + "\nevent.organizer_id = \"" +   event.organizer_id + "\","
                    + "\nevent.organizer_name = \"" + event.organizer_id + "\""
                    + "\nRETURN id(event) as id, event";
        Debug.Debugger.getInstance().printDatabaseHandler(Debugger.DATABASE_HANDLER.QUERY, stmt);
        
        try ( Session session = driver.session() )
        {
            return session.writeTransaction((Transaction tx) ->
            {                
                StatementResult result = tx.run( stmt );
                
                while (result.hasNext())
                {
                    Record next = result.next();
                    return new Event( next.get("id").asInt(), next.get("event").asNode() );
                }
                
                return null;
            });
        }
    }
    
    public void createEventList( EventList list )
    {
        for(Event event: list)
        {
            String stmt = "MERGE (event:Event{api_id: \"" + event.api_id + "\"})"
                        + "\nSET "
                        + "\nevent.title = \"" +       event.name + "\","
                        + "\nevent.description = \"" + event.description + "\","
                        + "\nevent.url = \"" +         event.url + "\","
                        + "\nevent.capacity = \"" +    event.capacity + "\","
                        + "\nevent.logoURL = \"" +     event.logoURL + "\","
                        + "\nevent.is_free = \"" +     event.is_free + "\","
                        + "\nevent.status = \"" +      event.status + "\","
                    
                        + "\nevent.startdate = \"" + event.startdate + "\","
                        + "\nevent.enddate = \"" +   event.enddate + "\","
                    
                        + "\nevent.venue_city = \"" +    event.venue.city + "\","
                        + "\nevent.venue_address = \"" + event.venue.address + "\","
                        + "\nevent.venue_x = \"" +       event.venue.x + "\","
                        + "\nevent.venue_y = \"" +       event.venue.y + "\","
                    
                        + "\nevent.organizer_id = \"" +   event.organizer_id + "\","
                        + "\nevent.organizer_name = \"" + event.organizer_id + "\""
                        + "\nRETURN id(event) as id, event";
            Debug.Debugger.getInstance().printDatabaseHandler(Debugger.DATABASE_HANDLER.QUERY, stmt);
            
            
            try ( Session session = driver.session() )
            {
                int result = session.writeTransaction( new  TransactionWork<Integer>()
                {
                    @Override
                    public Integer execute(Transaction tx)
                    {                
                        StatementResult result = tx.run( stmt );

                        while (result.hasNext())
                        {
                            Record next = result.next();
                            event.setID( next.get("id").asInt() );
                            
                            return event.id;
                        }
                        return -1;
                    }
                });
            }
        }
    }
    
    /**
     * Creates Tag
     * @param tagName
     * @return the Tag that created
     */
    public Tag createTag(String tagName)
    {
        String stmt = "MERGE (tag:Tag{tagName: '" + tagName + "'})"
                    + "\nRETURN id(tag) as id, tag";
        Debug.Debugger.getInstance().printDatabaseHandler(Debugger.DATABASE_HANDLER.QUERY, stmt);
        
        try ( Session session = driver.session() )
        {
            return session.writeTransaction((Transaction tx) ->
            {                
                StatementResult result = tx.run( stmt );
                
                while (result.hasNext())
                {
                    Record next = result.next();
                    return new Tag( next.get("id").asInt(), next.get("tag").asNode() );
                }
                
                return null;
            });
        }
    }
    
    /**
     * Creates a HAS relation
     * @param event
     * @param tag
     * @return if relation created successfully
     */
    public boolean createHasRelation(Event event, Tag tag)
    {
        String stmt = "MATCH (event:Event), (tag:Tag)"
                    + "\nWHERE ID(event) = " + event.id + " and ID(tag) = " + tag.id
                    + "\nMERGE (event)-[has:HAS]->(tag)"
                    + "\nRETURN has";
        Debug.Debugger.getInstance().printDatabaseHandler(Debugger.DATABASE_HANDLER.QUERY, stmt);
        
        try ( Session session = driver.session() )
        {
            return session.writeTransaction((Transaction tx) ->
            {
                StatementResult result = tx.run( stmt );
                return result != null;
            });
        }
    }
    
    /**
     * Creates a RELATES_TO relation
     * @param tag1
     * @param tag2
     * @return if relation created successfully
     */
    public boolean createRTRelation(Tag tag1, Tag tag2) // TODO Relation object?
    {
        String stmt = "MATCH (tag1:Tag), (tag2:Tag)"
                    + "\nWHERE ID(tag1) = " + tag1.id + " and ID(tag2) = " + tag2.id
                    + "\nMERGE (tag1)<-[rt:RELATES_TO]->(tag2)"
                    + "\nSET rt.weight = coalesce(rt.weight, 0) + 1"
                    + "\nRETURN rt";
        Debug.Debugger.getInstance().printDatabaseHandler(Debugger.DATABASE_HANDLER.QUERY, stmt);
        
        try ( Session session = driver.session() )
        {
            return session.writeTransaction((Transaction tx) ->
            {
                StatementResult result = tx.run( stmt );
                return result != null;
            });
        }
        
    }
    
    /**
     * Gets a list of Events that matches the given parameters
     * @param params
     * @return list of Tags
     */
    public ArrayList<Event> getEvent(AbstractMap.SimpleEntry<String, String>... params)
    {
        String paramStr = "";
        for(AbstractMap.SimpleEntry<String, String> pair: params)
            paramStr += pair.getKey() + ": '" + pair.getValue()+ "', ";
        if( 2 < paramStr.length() )
            paramStr = paramStr.substring(0, paramStr.length() - 2);

        String stmt = "MATCH (event:Event{" + paramStr + "})"
                    + "\nRETURN id(event) as id, event";
        Debug.Debugger.getInstance().printDatabaseHandler(Debugger.DATABASE_HANDLER.QUERY, stmt);
        
        ArrayList<Event> resultArr = new ArrayList<>();
        
        try ( Session session = driver.session() )
        {
            return session.writeTransaction((Transaction tx) ->
            {                
                StatementResult result = tx.run( stmt );
                
                while (result.hasNext())
                {
                    Record next = result.next();
                    resultArr.add(new Event(next.get("id").asInt(),
                                            next.get("event").asNode()));
                }
                
                return resultArr;
            });
        }
    }
    
    /**
     * Gets a list of Tags that matches the given parameters
     * @param params 
     * @return list of Tags
     */
    public ArrayList<Tag> getTag(AbstractMap.SimpleEntry<String, String>... params)
    {
        String paramStr = "";
        for(AbstractMap.SimpleEntry<String, String> pair: params)
            paramStr += pair.getKey() + ": '" + pair.getValue()+ "', ";
        if( 2 < paramStr.length() )
            paramStr = paramStr.substring(0, paramStr.length() - 2);

        String stmt = "MATCH (tag:Tag{" + paramStr + "})"
                    + "\nRETURN id(tag) as id, tag";
        Debug.Debugger.getInstance().printDatabaseHandler(Debugger.DATABASE_HANDLER.QUERY, stmt);
        
        ArrayList<Tag> resultArr = new ArrayList<>();
        
        try ( Session session = driver.session() )
        {
            return session.writeTransaction((Transaction tx) ->
            {                
                StatementResult result = tx.run( stmt );
                
                while (result.hasNext())
                {
                    Record next = result.next();
                    resultArr.add(new Tag(next.get("id").asInt(),
                                          next.get("tag").asNode()));
                }
                
                return null;
            });
        }
    }
     
}
