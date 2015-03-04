package tweetget;

import java.util.ArrayList;

import twitter4j.Status;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.Tables;

public class Dynamobasic {

	static String tableName = "TweetDatabase_8";
	static Table table;
	static String[] keyWords = {"apple", "football", "country", "movie", "cricket", "food", "place","healthcare" };
	static BasicAWSCredentials cred = new BasicAWSCredentials("********", "*********");
	static AmazonDynamoDBClient DBClient = new AmazonDynamoDBClient(cred);
	static DynamoDB db1 = new DynamoDB(DBClient);
	static long timer = 0L;
	static CreateTableRequest request;
	public static void createDB() throws InterruptedException {
		ArrayList<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("TwitterId").withAttributeType("N"));
		
		//Defined the KeySchema
		ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
		keySchema.add(new KeySchemaElement().withAttributeName("TwitterId").withKeyType(KeyType.HASH));

		//Create a new table request
		request = new CreateTableRequest()
		.withTableName(tableName)
		.withKeySchema(keySchema)
		.withAttributeDefinitions(attributeDefinitions)
		.withProvisionedThroughput(new ProvisionedThroughput()
		.withReadCapacityUnits(5L)
		.withWriteCapacityUnits(6L));

		//To make it realtime delete the table on every launch of the webpage
		System.out.println("TAables EXISTS ? ? ?" + Tables.doesTableExist(DBClient, tableName));
		if(Tables.doesTableExist(DBClient, tableName)) {
			System.out.println("deleting table");
			Table old_table = db1.getTable(tableName);
			old_table.delete();
			old_table.waitForDelete();
		}

		//Create new table for the tablerequest made earlier and wait
		table = db1.createTable(request);
		table.waitForActive();
		timer = System.currentTimeMillis();
		
		return;
	}
	
	public long getTime(){
		return this.timer;
	}
	
	public void clearDB() throws InterruptedException {
		Table old_table = db1.getTable(tableName);
		old_table.delete();
		old_table.waitForDelete();
		table = db1.createTable(request);
		table.waitForActive();
		timer = System.currentTimeMillis();
	}
	
	public static String getKeyword(Status tweet) {
		String thisTweet = tweet.getText();
		String keyWord = null;
		int keyLength = keyWords.length;
		for(int i=0;i<keyLength;i++) {
			if(thisTweet.contains(keyWords[i])) {
				keyWord = keyWords[i];
				break;
			}
		}

		//override some faulty tweet fetches so as to not be null.. 
		//did this instead of discard
		if(keyWord==null) keyWord="none";
		return keyWord;

	}

	public void addToDB(Status tweet) {
		/*
		 * This is one method of adding I use for reference.. found the item adding easier
		 * 
		Map<String, AttributeValue> newTweet = new HashMap<String,AttributeValue>();

		newTweet.put("Tweet ID", new AttributeValue(Long.toString(tweet.getId())));
		newTweet.put("Keyword", getKeyword(tweet));
		newTweet.put("Latitude", new AttributeValue(Double.toString(tweet.getGeoLocation().getLatitude())));
		newTweet.put("Longitude", new AttributeValue(Double.toString(tweet.getGeoLocation().getLongitude())));
		newTweet.put("Time", new AttributeValue(tweet.getCreatedAt().toString()));

		try {
		table = db1.getTable(tableName);
		PutItemRequest itemRequest = new PutItemRequest(tableName, newTweet);
		PutItemResult itemResult = table.putItem(itemRequest);
		}
		catch (Exception e) { System.out.println("Insertion error");
		return; 
		}
		 */

		//Create a new item based on the tweet & push to DB
		//System.out.println("addtob called");
		
		Item item = new Item()
		.withPrimaryKey("TwitterId",tweet.getId())
		.withString("KeyWord", getKeyword(tweet))
		.withString("Latitude",Double.toString(tweet.getGeoLocation().getLatitude()))
		.withString("Longitude", Double.toString(tweet.getGeoLocation().getLongitude()))
		.withString("Time",tweet.getCreatedAt().toString());
		try{
		table = db1.getTable(tableName);
		table.putItem(item);
		} catch (Exception e) { System.out.println("exception");};
	}
}

