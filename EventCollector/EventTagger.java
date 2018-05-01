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
import com.detectlanguage.DetectLanguage;
import com.detectlanguage.errors.APIError;

public class EventTagger {
		private static final String keyTextRazor = "apikey";
		private static final String keyDetectLanguage = "apikey";
		private static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
		private static boolean control;
		private static String language;

		public EventTagger() {
			language = "en";
		}
		
		public EventTagger(String lang) {
			language = lang;
		}
		
		public static boolean eventTagger(ArrayList<Event> eventList){
			ArrayList<String> tagNameList = new ArrayList<String>();
			String lang = "";
			String text = "";
			
			for(Event event : eventList) {
				text = event.name + "\n" + event.description;
				// check language
				lang = detectLanguage(text);
				if(lang == null || lang.compareTo(language) != 0)
					continue;
				tagNameList = TextRazorTopic(text);
				if(tagNameList != null) {
					addTags(event, tagNameList);
					tagNameList.clear();
				}
			}
			return true;
		}

		public static String detectLanguage(String text) {
			String lang = "";
			DetectLanguage.apiKey = keyDetectLanguage;
			try {
				lang = DetectLanguage.simpleDetect(text);
				if (lang == "")
					return null;
			}
			catch (APIError e) {
				System.err.println( "Detect Language : API Error:\n" + e);
				return null;
			}
			return lang;
		}
		
		public static String addEscape(String text) {
			String result = "";
			if(text == null) 
				return result;
            		result = text.replace("\"", "\\\"").replace("\\", "\\\\").replace("'", "\\'");
			return result;
		}
		
		public static String removeEscape(String text) {
			String result = "";
			if(text == null) 
				return result;
            		result = text.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\'", "'");
			return result;
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
				    	tags.add(addEscape(label));
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
