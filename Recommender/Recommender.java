package com.Overfind.Server;

import com.Overfind.DatabaseHandler.DatabaseHandler;
import com.Overfind.DatabaseHandler.Event;
import java.text.SimpleDateFormat;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;



public class Recommender {
	private static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
	private static ArrayList<AbstractMap.SimpleEntry<Integer, Double>> importance = new ArrayList<>();
	private static ArrayList<Integer> idList = new ArrayList<>();
	private static ArrayList<Integer> recomId = new ArrayList<>();
	
	//private static ArrayList<Tag> tagList = new ArrayList<>();
	//private static ArrayList<Integer> intList = new ArrayList<>();
	//private static String DB_PATH = "bolt://52.49.97.170:7687";
    //private static final Driver driver = (Driver) GraphDatabase.driver(DB_PATH, AuthTokens.basic("overfind", "neo4j"));
    
    private static int limitLow = 0;
    private static int limitUp = 10;
    private static String date = "";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	/*
	public static void main(String[] args) {

		ArrayList<Event> result = new ArrayList<Event>();
		date = dateFormat.format(Calendar.getInstance().getTime());
		System.out.println(date);
		
		result = recommend(new AbstractMap.SimpleEntry<String, Double>("Education", 2.2), 
				new AbstractMap.SimpleEntry<String, Double>("Business", 3.3),
				new AbstractMap.SimpleEntry<String, Double>("Cognition", 4.4),
				new AbstractMap.SimpleEntry<String, Double>("Change", 5.5),
				new AbstractMap.SimpleEntry<String, Double>("Culture", -6.6),
				new AbstractMap.SimpleEntry<String, Double>("Technology", 7.7));
		for(Event event : result) {
			System.out.println(event + "\n");
		}
		
		expand(new AbstractMap.SimpleEntry<String, Double>("Education", 2.2), 
				new AbstractMap.SimpleEntry<String, Double>("Business", 3.3),
				new AbstractMap.SimpleEntry<String, Double>("Cognition", 4.4),
				new AbstractMap.SimpleEntry<String, Double>("Change", 5.5),
				new AbstractMap.SimpleEntry<String, Double>("Culture", -6.6),
				new AbstractMap.SimpleEntry<String, Double>("Technology", 7.7));
		
		System.out.println("Done!");
		System.exit(0);
	}
*/
	
	public Recommender() {
		//TODO
	}
	
        public static ArrayList<Event> recommend(int page, ArrayList<AbstractMap.SimpleEntry<String, Double>> params) {
		ArrayList<Event> eventList = new ArrayList<>();
		String stmt = "";
		Double value = 0.0;
	
		
		for(AbstractMap.SimpleEntry<String, Double> pair: params)
                {
			value = pair.getValue();
			stmt = "match (tag:Tag)-[h:HAS]-(event:Event)"
					+ "\nwith tag as tag, event as event,"
					+ "\napoc.date.parse(event.startdate, 'ms', 'EEE MMM dd HH:mm:ss zzz yyyy') as startdate,"
					+ "\napoc.date.parse('" + dateFormat.format(new Date()) + "', 'ms', 'dd-MM-yyyy') as today"
					+ "\nwhere tag.tagName='" + pair.getKey() + "'"
					+ "\nand startdate > today"
					+ "\nreturn ID(event) as result"
                                        + "\nSKIP " + (25*page) + " LIMIT 25;";
                        
                        RestService.log += "\n"+stmt;

                        
			idList = dbHandler.getResult(stmt);
			//debug
			System.out.println("idList: " + idList.size());
			map(value);
			idList.clear();
		}
		//debug
		System.out.println("importance: " + importance.size());
		
		findMax();
		importance.clear();
		for(int id: recomId) {
			System.out.println(id);
			eventList.add(dbHandler.getEvent(id).get(0));
		}
		return eventList;
	}
        
	public static ArrayList<Event> recommend(int page, AbstractMap.SimpleEntry<String, Double>... params) {
		ArrayList<Event> eventList = new ArrayList<>();
		String stmt = "";
		Double value = 0.0;
		
		
		for(AbstractMap.SimpleEntry<String, Double> pair: params) {
			value = pair.getValue();
			stmt = "match (tag:Tag)-[h:HAS]-(event:Event)"
					+ "\nwith tag as tag, event as event,"
					+ "\napoc.date.parse(event.startdate, 'ms', 'EEE MMM dd HH:mm:ss zzz yyyy') as startdate,"
					+ "\napoc.date.parse('" + date + "', 'ms', 'dd-MM-yyyy') as today"
					+ "\nwhere tag.tagName='" + pair.getKey() + "'"
					+ "\nand startdate > today"
					+ "\nreturn ID(event) as result"
                                        + "\nSKIP " + (25*page) + " LIMIT 25;";
                        RestService.log += "\n"+stmt;
                        
			idList = dbHandler.getResult(stmt);
			//debug
			System.out.println("idList: " + idList.size());
			map(value);
			idList.clear();
		}
		//debug
		System.out.println("importance: " + importance.size());
		
		findMax();
		importance.clear();
		for(int id: recomId) {
			System.out.println(id);
			eventList.add(dbHandler.getEvent(id).get(0));
		}
		return eventList;
	}
	
	public static void SetLimits(int up, int low) {
		limitUp = up;
		limitLow = low;
	}
	
	private static void map(Double value) {
		AbstractMap.SimpleEntry<Integer, Double> keyValue;
		for(int id : idList) {
			keyValue = new AbstractMap.SimpleEntry<Integer, Double>(id, value);
			reduce(keyValue);
		}
	}
	
	private static boolean reduce(AbstractMap.SimpleEntry<Integer, Double> keyvalue)
	{
		if(keyvalue == null)
			return false;
		if(importance != null) {
			for(AbstractMap.SimpleEntry<Integer, Double> pair: importance)
			{
				if(keyvalue.getKey().equals(pair.getKey()))
				{	
					double d = pair.getValue() + keyvalue.getValue();
					pair.setValue(d);
					return true;
				}
			}
		}
		importance.add(keyvalue);
		return true;
	}
	
	private static void findMax() {
		Double maxValue = 0.0;
		Double value = 0.0;
		int index = 0;
		int count = 0;
		
		for(int i = 0; i < limitUp; i++) {
			maxValue = 0.0;
			index = 0;
			count = 0;
			if(importance.size() > 0) {
				for(AbstractMap.SimpleEntry<Integer, Double> pair: importance) {
					value = pair.getValue();
					if(value > maxValue) {
						maxValue = value;
						index = count;
					}
					count++;
				}
			}
			if(count > limitLow && maxValue > 0)
				recomId.add(importance.get(index).getKey());
			//System.out.println(importance.get(index).getValue() + "\n" + importance.get(index).getKey() + "\n");
			importance.remove(index);
		}
	}
	/*
	private static void expand(AbstractMap.SimpleEntry<String, Double>... params) {

		int weightSum = 0;
		
		for(AbstractMap.SimpleEntry<String, Double> pair: params) {
			final String stmt1 = "match (tag1:Tag)-[relation:RELATES_TO]-(:Tag)"
					+ "\nwhere tag1.tagName = '" + pair.getKey() + "'"
					+ "return sum(relation.weight) as result";
			weightSum = dbHandler.getResult(stmt1).get(0);
			System.out.println("weightSum = " + weightSum);
			
			final String stmt2 = "match (tag1:Tag)-[relation:RELATES_TO]-(tag2:Tag)"
					+ "\nwhere tag1.tagName = '" + pair.getKey() + "'"
					+ "return ID(tag2) as id, tag2, relation.weight as weight";
			try ( Session session = driver.session() )
	        {
	            session.writeTransaction((Transaction tx) ->
	            {
	                StatementResult result = tx.run( stmt2 );
	                while (result.hasNext())
	                {
	                    Record next = result.next();
	                    tagList.add(new Tag( next.get("id").asInt(),
                                next.get("tag2").asNode()));
	                    intList.add( next.get("weight").asInt());
	                    System.out.println( next.get("weight") + "\n" + next.get("tag"));
	                }
	                return null;
	            });
	        }
			// TODO
			//tag2weight = (tag2weight/weightSum)*tag1value
			 
		}
	}
	*/
}