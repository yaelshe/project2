/**
 * 
 */
package org.bgu.ise.ddb.history;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.MediaItems;
import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.registration.*;
import org.bgu.ise.ddb.User;
import org.bgu.ise.ddb.items.ItemsController;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/history")
public class HistoryController extends ParentController{
	
	
	
	/**
	 * The function inserts to the system storage triple(s)(username, title, timestamp). 
	 * The timestamp - in ms since 1970
	 * Advice: better to insert the history into two structures( tables) in order to extract it fast one with the key - username, another with the key - title
	 * @param username
	 * @param title
	 * @param response
	 */
	@RequestMapping(value = "insert_to_history", method={RequestMethod.GET})
	public void insertToHistory (@RequestParam("username")    String username,
			@RequestParam("title")   String title,
			HttpServletResponse response){
		System.out.println(username+" "+title);
		//:TODO your implementation -ACCOMPLISHED
		HttpStatus status=HttpStatus.OK;
		ItemsController itemCon= new ItemsController();
		RegistarationController userCon= new RegistarationController();
		try {
			if(!userCon.isExistUser(username)|| itemCon.isExistTitle(title)) 
				status = HttpStatus.CONFLICT;
			else{
				MongoClient client = new MongoClient("localhost", 27017);
				MongoDatabase db = client.getDatabase("projectNoSql");
				MongoCollection<Document> collection = db.getCollection("History");
				Document doc = new Document("Username", username);
				doc.append("Title", title);
				long date=new Date().getTime();
				doc.append("Viewtime", date);
				collection.insertOne(doc);
				client.close();
			}
			
		}
		catch (Exception exception) {
			System.out.println(exception);
			status = HttpStatus.CONFLICT;
		}
		response.setStatus(status.value());
	}
	
	
	
	/**
	 * The function retrieves  users' history
	 * The function return array of pairs <title,viewtime> sorted by VIEWTIME in descending order
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "get_history_by_users",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public  HistoryPair[] getHistoryByUser(@RequestParam("entity")    String username){
		//:TODO your implementation
		
		HistoryPair hp = new HistoryPair("aa", new Date());
		RegistarationController userCon = new RegistarationController();
		ArrayList<HistoryPair> pairsResult= new ArrayList();
		MongoClient client = null;
		try {
			if (userCon.isExistUser(username)) {
				client = new MongoClient("localhost", 27017);
				MongoDatabase db = client.getDatabase("projectNoSql");
				MongoCollection<Document> collection = db.getCollection("History");
				MongoCursor<Document> iterator= collection.find(Filters.eq("Username", username)).iterator();
				while(iterator.hasNext()) {
					Document pair = (Document) iterator.next();
					pairsResult.add(new HistoryPair(pair.get("Title").toString(), new Date(Long.parseLong(pair.get("Timestamp").toString(), 10))));
				}
			}
			client.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
//		System.out.println("ByUser "+hp);
		HistoryPair[] historyPairs = new HistoryPair[pairsResult.size()];
		pairsResult.toArray(historyPairs);
		return historyPairs;
	}
	
	
	/**
	 * The function retrieves  items' history
	 * The function return array of pairs <username,viewtime> sorted by VIEWTIME in descending order
	 * @param title
	 * @return
	 */
	@RequestMapping(value = "get_history_by_items",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public  HistoryPair[] getHistoryByItems(@RequestParam("entity")    String title){
		//:TODO your implementation
		//HistoryPair hp = new HistoryPair("aa", new Date());
		ArrayList<HistoryPair> pairsResult= new ArrayList();
		MongoClient client = null;
		ItemsController itemCon= new ItemsController();
		try {
			if (itemCon.isExistTitle(title)) {
				client = new MongoClient("localhost", 27017);
				MongoDatabase db = client.getDatabase("projectNoSql");
				MongoCollection<Document> collection = db.getCollection("History");
				MongoCursor<Document> iterator= collection.find(Filters.eq("Title", title)).iterator();
				while(iterator.hasNext()) {
					Document pair = (Document) iterator.next();
					pairsResult.add(new HistoryPair(pair.get("Username").toString(), new Date(Long.parseLong(pair.get("Timestamp").toString(), 10))));
				}
			}
			client.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		HistoryPair[] historyPairs = new HistoryPair[pairsResult.size()];
		pairsResult.toArray(historyPairs);
		return historyPairs;
	}
	
	/**
	 * The function retrieves all the  users that have viewed the given item
	 * @param title
	 * @return
	 */
	@RequestMapping(value = "get_users_by_item",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public  User[] getUsersByItem(@RequestParam("title") String title){
		//:TODO your implementation
		ArrayList<User> usersPairItem = new ArrayList<User>();
		MongoClient client = null;
		ItemsController itemCon= new ItemsController();
		try {
			if(itemCon.isExistTitle(title)) {
				client = new MongoClient("localhost", 27017);
				MongoDatabase db = client.getDatabase("projectNoSql");
				MongoCollection<Document> collection = db.getCollection("Users");
				MongoCursor<Document> iterator= collection.find(Filters.eq("Title", title)).iterator();
				while(iterator.hasNext())
				{
					Document docPair = (Document)iterator.next();
					MongoCursor<Document> useriterator= collection.find(Filters.eq("Username", docPair.get("Username"))).iterator();
					Document docUser = (Document) useriterator.next();
					usersPairItem.add(new User(docUser.get("Username").toString(),  docUser.get("Password").toString(), docUser.get("Firstname").toString(), docUser.get("Lastname").toString()));
				}
			}
			client.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		User[] historyPairs = new User[usersPairItem.size()];
		usersPairItem.toArray(historyPairs);
		return historyPairs;
	}
	
	/**
	 * The function calculates the similarity score using Jaccard similarity function:
	 *  sim(i,j) = |U(i) intersection U(j)|/|U(i) union U(j)|,
	 *  where U(i) is the set of usernames which exist in the history of the item i.
	 * @param title1
	 * @param title2
	 * @return
	 */
	@RequestMapping(value = "get_items_similarity",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	public double  getItemsSimilarity(@RequestParam("title1") String title1,
			@RequestParam("title2") String title2){
		//:TODO your implementation
		ItemsController itemCon = new ItemsController();
		double intersection=0;
		double union=0;
		try {
			if (itemCon.isExistTitle(title1) && itemCon.isExistTitle(title2)) 
			{
				List<User> title1Users = Arrays.asList(getUsersByItem(title1));
				List<User> title2Users = Arrays.asList(getUsersByItem(title2));
				List<String> title2UsersNames =new ArrayList<String>();
				for (User user: title2Users) {
					title2UsersNames.add(user.getUsername());
				}
				Set<String> unionSet = new HashSet<String>(title2UsersNames);
				for (User user: title1Users) {
					String name=user.getUsername();
		            if (title2UsersNames.contains(name)) {
		            	intersection++;
		            }
		            unionSet.add(name);
		            
		        }			
				union = unionSet.size();
				return Math.max(0, intersection / union);
			} else {
				return 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	
	}
	

}
