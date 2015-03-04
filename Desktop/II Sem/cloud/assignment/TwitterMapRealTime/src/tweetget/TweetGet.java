package tweetget;

import java.io.IOException;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;


/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class TweetGet {
	
	private long startTime=0L;
	private long pollTime = 120000L; ///This sets the duration for tweets before we flush DB
	public void startTweet() throws TwitterException, IOException, InterruptedException {

		//Provide Twitter credentials
		String conKey = "**********";
		String conSec = "***********";
		String AccTok = "***********";
		String AccSec = "***********";

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(conKey)
		.setOAuthConsumerSecret(conSec)
		.setOAuthAccessToken(AccTok)
		.setOAuthAccessTokenSecret(AccSec);


		final Dynamobasic MyDB = new Dynamobasic();
		MyDB.createDB();
		startTime = MyDB.getTime();
		final TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {

				if(status.getLang().toString().equals("en") && status.getGeoLocation()!=null) {
					if(MyDB!=null)
					MyDB.addToDB(status);
					
				//	System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText() 
					//		 + status.getGeoLocation());
					
					if((System.currentTimeMillis()-startTime)>pollTime) {
						//System.out.println("flush");
						try {
							MyDB.clearDB();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						startTime=System.currentTimeMillis();
						
					}
				}
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			//    @Override
			//    public void onStallWarning(StallWarning warning) {
			//       System.out.println("Got stall warning:" + warning);
			//   }

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				// TODO Auto-generated method stub

			}
		};

		FilterQuery filterQuery = new FilterQuery();
		String[] keyWords = {"apple", "football", "country", "movie", "cricket", "food", "place", "healthcare" };

		filterQuery.track(keyWords);

		twitterStream.addListener(listener);
		twitterStream.filter(filterQuery);
		}
	}
