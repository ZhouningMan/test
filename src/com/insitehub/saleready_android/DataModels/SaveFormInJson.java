package com.insitehub.saleready_android.DataModels;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import android.util.JsonWriter;

import com.insitehub.saleready_android.DataModels.FormStructures.ActionItem;
import com.insitehub.saleready_android.DataModels.FormStructures.CompetencyListItem;
import com.insitehub.saleready_android.DataModels.FormStructures.Form;
import com.insitehub.saleready_android.DataModels.FormStructures.Option;

public class SaveFormInJson {
//	public static String testJSONWritter(OutputStream out, Form form){
//         JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
//		 writer.setIndent("  ");
//		 User user = new User("Linfeng", 100);
//		 List<Message> messages = new LinkedList<TestJSONWritter.Message>();
//		 Message message = new Message(1, "hello", user);
//		 messages.add(message);
//		 writer.beginObject();
//		 writer.name("competencylist");
//		 writeCompetencyListArray(writer, messages);
//		 writer.endObject();
//		 writer.close();
//		 byte[] bytes = out.toByteArray();
//		 String jsonStr = new String(bytes);
//		 System.out.println(jsonStr);
//	     return jsonStr;
//	}
	
	public static void saveFormInJson(OutputStream out, Form form){
		JsonWriter writer;
		try {
			writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
			writer.setIndent("  ");
			writeForm(writer, form);
			writer.flush();
			writer.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	

	
	private static void writeForm(JsonWriter writer, Form form) throws IOException{
		writer.beginObject();
		writer.name("userid").value(form.getUserID());
		
		writer.name("form");
		writer.beginObject();
		writer.name("objectId").value(form.getObjectID());
		writer.name("name").value(form.getName());
		writer.name("fromClass").value(form.getFormClass());
		writer.endObject();
		
		writer.name("competencylist");
		writeCompetencyList(writer, form.getCompetencyList());
		
		writer.endObject();
	}
	
	
	
	private static void writeCompetencyList(JsonWriter writer, List<CompetencyListItem> list) throws IOException{
		writer.beginArray();
		for (CompetencyListItem competencyListItem : list) {
			writeCompetencyListItem(writer, competencyListItem);
		}
		
		writer.endArray();
	}
	
	
	
	private static void writeCompetencyListItem(JsonWriter writer, CompetencyListItem item) throws IOException{
		writer.beginObject();
		writer.name("actionlist");
		writeAcionListArray(writer,item.getActionlist());
		writer.name("name").value(item.getName());
		writer.endObject();
	}
	
	private static void writeAcionListArray(JsonWriter writer,List<ActionItem> actionlist) throws IOException{
		writer.beginArray();
		for (ActionItem actionItem : actionlist) {
			writeACtionItem(writer, actionItem);
		}
		writer.endArray();
	}

	private static void writeACtionItem(JsonWriter writer, ActionItem item) throws IOException{
		writer.beginObject();
		String type = item.getType();
		writer.name("type").value(type);
		writer.name("name").value(item.getName());
		if(type.equals("text")){
			writer.name("value").value(item.getView().getText().toString());
		}else{
			writer.name("value").value(item.getValue());
		}
		
		if(type.equals("select")){
			writer.name("optionlist");
			writeOptionListArray(writer,item.getOptionlist());
		}
		writer.endObject();
	}

	private static void writeOptionListArray(JsonWriter writer, List<Option> optionlist) throws IOException{
		writer.beginArray();
		for (Option option : optionlist) {
			writeOption(writer, option);
		}
		writer.endArray();
	}
	
	private static void writeOption(JsonWriter writer, Option option) throws IOException{
			writer.beginObject();
			writer.name("name").value(option.getName());
			writer.name("selected").value(option.getView().isChecked());	
			writer.endObject();
	}
}
