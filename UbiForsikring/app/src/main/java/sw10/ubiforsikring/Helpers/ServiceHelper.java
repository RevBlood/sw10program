package sw10.ubiforsikring.Helpers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import sw10.ubiforsikring.Objects.CarObjects.Car;
import sw10.ubiforsikring.Objects.CompetitionObjects.CompetitionListItem;
import sw10.ubiforsikring.Objects.FactObjects.Fact;
import sw10.ubiforsikring.Objects.TripObjects.Trip;
import sw10.ubiforsikring.Objects.TripObjects.TripListItem;

public class ServiceHelper {
	private static String ip = "stream.cs.aau.dk:9220";
	//private static String ip = "62.107.99.175";

	//region GET

	public static Trip GetTrip(int carId, long tripId){
		String response = "Empty response";
        Trip trip = null;

		try {
			response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Trip/GetTrip?carid=" + carId + "&tripid=" + tripId);
			System.out.println("Response: " + response);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.i("Debug", response);

		String prunedResponse = pruneXMLtags(response);

		try {
			trip = new Trip(new JSONObject(prunedResponse));
		} catch(Exception e) {
			Log.e("Debug", "GetTrip:", e);
		}

		return trip;
	}

	public static Car GetOrCreateCar(String imei){
		String response = "Empty response";
		Car car = null;

		try {
			response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Car/GetOrCreateCar?imei=" + imei);
			System.out.println("Response: " + response);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.i("Debug", response);

		String prunedResponse = pruneXMLtags(response);

		try {
			car = new Car(new JSONObject(prunedResponse));
		} catch(Exception e) {
			Log.e("Debug", "GetOrCreateCar:", e);
		}

		return car;
	}

	public static void UpdateCarWithUsername(int carId, String username){
		String response = "Empty response";
		try {
			response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Car/UpdateCarWithUsername?carid=" + carId + "&username=" + username);
			System.out.println("Response: " + response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static ArrayList<TripListItem> GetTripsForListView(int carId, int offset){
		String response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Trip/GetTripsForList?carid=" + carId + "&offset=" + offset);
        Log.i("Debug", response);

		String prunedResponse = pruneXMLtags(response);
		Log.i("Debug", prunedResponse);

		ArrayList<TripListItem> tripListItems = new ArrayList<>();

		try {
			JSONArray jsonArray = new JSONArray(prunedResponse);

			for(int i = 0; i < jsonArray.length(); i++){
                tripListItems.add(new TripListItem(jsonArray.getJSONObject(i)));
			}
		} catch(Exception e) {
			Log.e("Debug", "GetTripsForListView:", e);
		}

		return tripListItems;
	}

	public static void CompetitionSignUp(int carId, int competitionId){
		String response = "Empty response";
		try {
			response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Competition/CompetitionSignUp?carid=" + carId + "&competitionid=" + competitionId);
			System.out.println("Response: " + response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void CompetitionSignDown(int carId, int competitionId){
		String response = "Empty response";
		try {
			response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Competition/CompetitionSignDown?carid=" + carId + "&competitionid=" + competitionId);
			System.out.println("Response: " + response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

    //TODO: Få Lau til at sætte denne op på serveren, og parse til CompetitionListItem i stedet for tripListItem
	public static ArrayList<CompetitionListItem> GetCompetitionsForListView(int carId, int offset){
		String response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Competition/GetCompetitionsForListView?carid=" + carId + "&offset=" + offset);
		Log.i("Debug", response);

		String prunedResponse = pruneXMLtags(response);
		Log.i("Debug", prunedResponse);

		ArrayList<CompetitionListItem> competitionListItems = new ArrayList<>();

		try {
			JSONArray jsonArray = new JSONArray(prunedResponse);

			for(int i = 0; i < jsonArray.length(); i++){
                competitionListItems.add(new CompetitionListItem(jsonArray.getJSONObject(i)));
			}
		} catch(Exception e) {
			Log.e("Debug", "GetCompetitionsForListView:", e);
		}

		return competitionListItems;
	}

	public static ArrayList<Fact> GetFacts(int carid, int tripid){
		String response = "Empty response";
		try {
			response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Fact/GetFacts?carid=" + carid + "&tripid=" + tripid);
			Log.i("Debug", response);
		} catch (Exception e) {
			Log.i("Debug", "Response failure:", e);
		}

		ArrayList<Fact> facts = new ArrayList<>();

		try {
			JSONArray jsonArray = new JSONArray(response);

			for(int i = 0; i < jsonArray.length(); i++){
				facts.add(new Fact(jsonArray.getJSONObject(i)));
			}
		} catch(Exception e) {
			Log.e("Debug", "GetFacts:", e);
		}

		return facts;
	}

	public static ArrayList<Fact> GetFactsForMap(int carid, long tripid){
		String response = "Empty response";

		try {
			response = HTTPHelper.HTTPGet("http://" + ip + "/RestService/Fact/GetFacts?carid=" + carid + "&tripid=" + tripid);
			Log.i("Debug", response);
		} catch (Exception e) {
			Log.i("Debug", "Response failure:", e);
		}

		ArrayList<Fact> facts = new ArrayList<>();

		String prunedResponse = pruneXMLtags(response);
		Log.i("Debug", prunedResponse);

		try {
			JSONArray jsonArray = new JSONArray(prunedResponse);

			for(int i = 0; i < jsonArray.length(); i++){
				facts.add(new Fact(jsonArray.getJSONObject(i)));
			}
		} catch(Exception e) {
			Log.e("Debug", "GetFacts:", e);
		}

		return facts;
	}

    //endregion

	//region POST

	public static boolean PostFacts(ArrayList<Fact> facts) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < facts.size(); i++) {
            jsonArray.put(facts.get(i).serializeToJSON());
        }

        String request = "http://" + ip + "/RestService/Fact/AddFacts";
        try {
            String response = HTTPHelper.HTTPPost(request, jsonArray.toString());
            System.out.println("Response: " + response);
            return true;
        } catch (Exception e) {
            return false;
        }

	}

	public static String pruneXMLtags(String response) {
		return response.replaceAll("<.*?>", "");
	}

    //endregion
}
