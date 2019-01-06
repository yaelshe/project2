/**
 * 
 */
package org.bgu.ise.ddb.items;

import java.io.*;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.bgu.ise.ddb.MediaItems;
import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;



/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/items")
public class ItemsController extends ParentController 
{
	/**
	 * The function copy all the items(title and production year) from the Oracle table MediaItems to the System storage.
	 * The Oracle table and data should be used from the previous assignment
	 */
	@RequestMapping(value = "fill_media_items", method={RequestMethod.GET})
	public void fillMediaItems(HttpServletResponse response)
	{
		System.out.println("was here");
		//:TODO your implementation
		Connection conn=null;
		HttpStatus status=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver"); 
			String connUrl="jdbc:oracle:thin:@ora1.ise.bgu.ac.il:1521/ORACLE";
			String userName="sheinbey";
			String password = "abcd";
			conn = DriverManager.getConnection(connUrl, userName, password);
			PreparedStatement ps = conn.prepareStatement("SELECT * from MediaItems");
			ResultSet result = ps.executeQuery();
			MongoClient client=new MongoClient("localHost",27017);
			MongoDatabase db=client.getDatabase("projectNoSql");
			MongoCollection<Document> items=db.getCollection("Items");

			while(result.next()) 
			{//iterate the list and  insert each item to the Database
				String title=result.getString("TITLE");
				int prodYear=result.getInt("PROD_YEAR");
				Document doc=new Document();
				doc.put("title", title);
				doc.put("prodYear", prodYear);
				items.insertOne(doc);
			}
			result.close();
			ps.close();
			conn.close();
			client.close();
			status = HttpStatus.OK;
		} catch(Exception e) {
			status=HttpStatus.CONFLICT;
			e.printStackTrace();
		}
		response.setStatus(status.value());
	}


	/**
	 * The function copy all the items from the remote file,
	 * the remote file have the same structure as the films file from the previous assignment.
	 * You can assume that the address protocol is http
	 * @throws IOException 
	 */
	@RequestMapping(value = "fill_media_items_from_url", method={RequestMethod.GET})
	public void fillMediaItemsFromUrl(@RequestParam("url")    String urladdress,
			HttpServletResponse response) throws IOException
	{
		System.out.println(urladdress);

		//:TODO your implementation -ACCOMPLISHED 
		HttpStatus status = HttpStatus.OK;
		BufferedReader br = null;
		String line = "";
		MongoClient client=null;
		try {
			client = new MongoClient("localhost", 27017);
			MongoCollection<Document> colleaction = client.getDatabase("projectNoSql").getCollection("MediaItems");
			br = new BufferedReader(new InputStreamReader(new URL(urladdress).openStream()));
			line = br.readLine();
			while (line != null)
			{
				String[] currnet = line.split(",");
				currnet[0] = currnet[0].replace("'", "''");
				Document itemDoc = new Document();
				System.out.println(currnet[0] + "," + currnet[1]);
				itemDoc.append("Title", currnet[0]);
				itemDoc.append("Prod_Year", currnet[1]);
				colleaction.insertOne(itemDoc);
				
				line = br.readLine();
			} 
			status = HttpStatus.OK;
		} catch(Exception e) {
			status = HttpStatus.CONFLICT;
			response.setStatus(status.value());
			e.printStackTrace();
		} finally {
			client.close();
		}
		response.setStatus(status.value());
	}


	/**
	 * The function retrieves from the system storage N items,
	 * order is not important( any N items) 
	 * @param topN - how many items to retrieve
	 * @return
	 */
	@RequestMapping(value = "get_topn_items",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(MediaItems.class)
	public  MediaItems[] getTopNItems(@RequestParam("topn")    int topN)
	{
		//:TODO your implementation- ACCOMPLISHED
		MongoClient client = null;
		List<MediaItems> resultItems = new ArrayList<MediaItems>();
		int result=0;
		try {
			client = new MongoClient("localhost", 27017);
			MongoDatabase db = client.getDatabase("projectNoSql");
			MongoCollection<Document> collection = db.getCollection("MediaItems");
			MongoCursor<Document> itemIterator= collection.find().iterator();
			while(itemIterator.hasNext() && result < topN)
			{
				Document current = (Document) itemIterator.next();
				resultItems.add(new MediaItems(current.getString("Title"),
						Integer.parseInt(current.get("Prod_Year").toString())));
				result++;
			}
			client.close();
		}
		catch(Exception exception ) {
			System.out.println(exception);
		}
		MediaItems[] items = new MediaItems[resultItems.size()];
		resultItems.toArray(items);
		return items;
	}

	/**
	 * The function returns true if the received title exist in the system otherwise false
	 * @param title
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "is_exist_title", method={RequestMethod.GET})
	public boolean isExistTitle(@RequestParam("title") String title) throws IOException{
		System.out.println(title);
		boolean result = false;
		//:TODO your implementation- ACCOMPLISHED I ADDED 
		try {
			MongoClient client = new MongoClient("localhost", 27017);
			MongoDatabase db = client.getDatabase("projectNoSql");

			MongoCollection<Document> collection = db.getCollection("MediaItems");
			Document doc=new Document("Title", title);
			Document ans= collection.find(doc).first();
			client.close();
			if(ans==null)
			{
				result=false;
			}
			else {
				result=true;
			}
		}
		catch(Exception exception ) {
			System.out.println(exception);
		}

		return result;

	}
}
