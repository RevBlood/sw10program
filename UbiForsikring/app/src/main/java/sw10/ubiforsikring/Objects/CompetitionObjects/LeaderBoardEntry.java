package sw10.ubiforsikring.Objects.CompetitionObjects;

import org.json.JSONException;
import org.json.JSONObject;

public class LeaderBoardEntry {
    public int Rank;
    public String Username;
    public double Score;

    public LeaderBoardEntry(JSONObject obj) {
        try {
            Rank = obj.getInt("rank");
            Username = obj.getString("username");
            Score = obj.getDouble("score");
        } catch (JSONException e) {
        }
    }
}
