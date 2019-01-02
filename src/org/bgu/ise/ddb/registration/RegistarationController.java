/**
 * 
 */
package org.bgu.ise.ddb.registration;



import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;


/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/registration")
public class RegistarationController extends ParentController{


	/**
	 * The function checks if the username exist,
	 * in case of positive answer HttpStatus in HttpServletResponse should be set to HttpStatus.CONFLICT,
	 * else insert the user to the system  and set to HttpStatus in HttpServletResponse HttpStatus.OK
	 * @param username
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param response
	 */
	@RequestMapping(value = "register_new_customer", method={RequestMethod.POST})
	public void registerNewUser(@RequestParam("username") String username,
			@RequestParam("password")    String password,
			@RequestParam("firstName")   String firstName,
			@RequestParam("lastName")  String lastName,
			HttpServletResponse response){
		System.out.println(username+" "+password+" "+lastName+" "+firstName);
		//:TODO your implementation - accomplished CHECKED
		HttpStatus status;
		try {
			if(!isExistUser(username)) {
				MongoClient client = new MongoClient("localhost", 27017);
				MongoDatabase db = client.getDatabase("projectNoSql");
				MongoCollection<Document> collection = db.getCollection("Users");
				Document user = new Document("Username", username);
				user.append("Password", password);
				user.append("FirstName", firstName);
				user.append("LastName", lastName);
				LocalDate now=LocalDate.now();
				user.append("RegistrationDate", now);
				collection.insertOne(user);
				client.close();

				status = HttpStatus.OK;
			}
			else
				status = HttpStatus.CONFLICT;
			response.setStatus(status.value());}
		catch(Exception exception) {
			System.out.println(exception);
		}

	}

	/**
	 * The function returns true if the received username exist in the system otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "is_exist_user", method={RequestMethod.GET})
	public boolean isExistUser(@RequestParam("username") String username) throws IOException{
		System.out.println(username);
		boolean result = false;
		//:TODO your implementation accomplished CHECKED
		try {
			MongoClient client = new MongoClient("localhost", 27017);
			MongoDatabase db = client.getDatabase("projectNoSql");

			MongoCollection<Document> collection = db.getCollection("Users");
			Document doc=new Document("Username", username);
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

	/**
	 * The function returns true if the received username and password match a system storage entry, otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "validate_user", method={RequestMethod.POST})
	public boolean validateUser(@RequestParam("username") String username,
			@RequestParam("password")    String password) throws IOException{
		System.out.println(username+" "+password);
		boolean result = false;
		//:TODO your implementation -accomplished CHECK
		try {
			MongoClient client = new MongoClient("localhost", 27017);
			MongoDatabase db = client.getDatabase("projectNoSql");
			MongoCollection<Document> collection = db.getCollection("Users");
			Document doc=new Document("Username", username);
			doc.append("Password", password);

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

	/**
	 * The function retrieves number of the registered users in the past n days
	 * @param days
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "get_number_of_registred_users", method={RequestMethod.GET})
	public int getNumberOfRegistredUsers(@RequestParam("days") int days) throws IOException{
		System.out.println(days+"");
		int result = 0;
		//:TODO your implementation- accomplished CHECK
		try {
			MongoClient client = new MongoClient("localhost", 27017);
			MongoDatabase db = client.getDatabase("projectNoSql");
			MongoCollection<Document> collection = db.getCollection("Users");
			LocalDate targetDate = LocalDate.now().minusDays(days);
			MongoCursor<Document> userIterator= collection.find(Filters.gt("RegistrationDate", targetDate)).iterator();
			System.out.println(userIterator);
			while(userIterator.hasNext())
			{
					result++;
					userIterator.next();
			}
			client.close();
		}
		catch(Exception exception ) {
			System.out.println(exception);
		}
		return result;

	}

	/**
	 * The function retrieves all the users
	 * @return
	 */
	@RequestMapping(value = "get_all_users",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(User.class)
	public  User[] getAllUsers(){
		//:TODO your implementation- accomplished CHECK
		List<User> resultUsers = new ArrayList<User>();
		try {
			MongoClient client = new MongoClient("localhost", 27017);
			MongoDatabase db = client.getDatabase("projectNoSql");
			MongoCollection<Document> collection = db.getCollection("Users");
			MongoCursor<Document> userIterator= collection.find().iterator();
			while(userIterator.hasNext())
			{
				Document current = (Document) userIterator.next();
				resultUsers.add(new User(current.getString("Username"),
						current.getString("FirstName"), current.getString("LastName")));
			}
			client.close();
		}
		catch(Exception exception ) {
			System.out.println(exception);
		}
	
		User[] users = new User[resultUsers.size()];
		resultUsers.toArray(users);
		return users;
	}

}
