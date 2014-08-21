import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Read from a Google Calendar.
 * 
 * The clientId and clientSecret can be found in Google Developers Console.
 * See url(https://console.developers.google.com)
 * 
 * Usage:
 * 1. Create a project and note down the generated app name (or the one you set).
 * 2. Enable Calendar API for your project (Dev console > APIs).
 * 3. Get the client id/secret for your project in and set it up as a desktop app (Dev console > Credentials).
 * 4. Get the id of the calendar that you wish to retrieve entries from (Google Calendar > Calendar settings > Calendar Id) 
 * 5. Enter these parameters below.
 * 6. Run the app as a Java application. Copy the url that is displayed in the console into your browser. Accept the permissions.
 * 7. Copy the result from your browser into the console and hit return.
 * 8. Calendar entries should now be displayed.
 * 
 * Prerequisites: 
 * 1. Cal jar
 * 2. Google client jars
 * See url(http://stackoverflow.com/questions/17732062/google-calendar-api-not-found-calendar-classes-missing)
 * 
 * @author norrielm
 */
public class CalendarTest {

	private static String CLIENT_ID = "YOUR_CLIENT_ID";
	private static String CLIENT_SECRET = "YOUR_CLIENT_SECRET";
	private static String APP_ID = "YOUR-APP-NAME";
	private static String CAL_ID = "YOUR-CALENDAR-ID";

	/**
	 * Read calendar events from an authorised calendar.
	 */
	public static void main(String[] args) {
		try {
			Calendar client = getAuthCalendarService();

			String pageToken = null;

			do {
				Events events = client.events().list(CAL_ID).setPageToken(pageToken).execute();

				List<Event> items = events.getItems();
				for (Event event : items) {
					System.out.println(event.getSummary());
				}
				pageToken = events.getNextPageToken();
			} while (pageToken != null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get an authorised calendar.
	 */
	private static Calendar getAuthCalendarService() throws IOException {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";    

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET,
				Arrays.asList(CalendarScopes.CALENDAR_READONLY)).setAccessType("online")
				.setApprovalPrompt("auto").build();

		String url = flow.newAuthorizationUrl().setRedirectUri(redirectUrl).build();
		System.out.println("Please open the following URL in your browser then type the authorization code:");

		System.out.println("  " + url);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = br.readLine();

		GoogleTokenResponse response = flow.newTokenRequest(code)
				.setRedirectUri(redirectUrl).execute();
		GoogleCredential credential = new GoogleCredential()
		.setFromTokenResponse(response);

		// Create a new authorized API client
		Calendar service = new Calendar.Builder(httpTransport, jsonFactory,
				credential).setApplicationName(APP_ID).build();

		return service;
	}
}
