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
    
    private static int limitLow = 0;
    private static int limitUp = 10;
    private static String date = "";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	
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
			map(value);
			idList.clear();
		}
		
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
			map(value);
			idList.clear();
		}
		
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
			importance.remove(index);
		}
	}

}
