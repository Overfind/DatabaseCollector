package EventCollector;

import DatabaseHandler.DatabaseHandler;
import DatabaseHandler.Tag;
import DatabaseHandler.Event;
import com.textrazor.TextRazor;
import com.textrazor.annotations.AnalyzedText;
import com.textrazor.annotations.Topic;
import com.textrazor.AnalysisException;
import com.textrazor.NetworkException;
import java.util.ArrayList;
import java.util.List;

public class EventTagger {
		private static final String keyTextRazor = "0ebcfe4089f597292d681052e28abeab3a19c1761f7b96a4b17b0f62";
		private static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
		private static boolean control;
		
		private EventTagger() {
				//TODO
		}
		
		public static boolean tagEvents(ArrayList<Event> eventList){
			ArrayList<String> tagNameList = new ArrayList<String>();
			for(Event event : eventList) {
				String text = "";
				text = event.name + "\n" + event.description;
				tagNameList = TextRazorTopic(text);
				if(tagNameList != null) {
					addTags(event, tagNameList);
					tagNameList.clear();
				}
			}
			return true;
		}
		
		private static ArrayList<String> TextRazorTopic(String text) {
			String label;
			double score;
			List<Topic> topics;
			ArrayList<String> tags = new ArrayList<String>();
			TextRazor client = new TextRazor(keyTextRazor);
			AnalyzedText response = new AnalyzedText();
			List<String> extractors = new ArrayList<String>();
			extractors.add("topics");
			try{
				client.setAllowOverlap(false);
				client.setExtractors(extractors);
				if((response = client.analyze(text)) == null)
					return null;
				if((topics = response.getResponse().getTopics())== null)
					return null;
				for (Topic topic : topics) {
				    label = topic.getLabel();
				    score = topic.getScore();
				    if(score > 0.8) {
				    	tags.add(label);
				    }
				}
			}
			catch(AnalysisException e) {
				System.err.println( "TextRazorTopic : Analysis Exception:\n" + e);
				return null;
			}
			catch(NetworkException e) {
				System.err.println( "TextRazorTopic : Network Exception:\n" + e);
				return null;
			}
			return tags;
		}
		
		private static void addTags(Event event, ArrayList<String> tags) {
			ArrayList<Tag> tagList = new ArrayList<Tag>();
			Tag tag;
			for(String tagName : tags) {
				tag = dbHandler.createTag(tagName);
				tagList.add(tag);
				control = dbHandler.createHasRelation(event, tag);
			}
			connectTags(tagList);
			tagList.clear();
		}
		
		
		private static void connectTags(ArrayList<Tag> tags) {
			Tag tag1;
			Tag tag2;
			int tagCount = tags.size();
			for(int i = 0; i < tagCount; i++) {
				tag1 = tags.get(i);
				for(int j = i+1; j < tagCount; j++) {
					tag2 = tags.get(j);
					control = dbHandler.createRTRelation(tag1, tag2);
				}
			}
		}
}
