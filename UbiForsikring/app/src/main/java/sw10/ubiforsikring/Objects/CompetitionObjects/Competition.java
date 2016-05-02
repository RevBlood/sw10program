package sw10.ubiforsikring.Objects.CompetitionObjects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sw10.ubiforsikring.Helpers.DateObjectHelper;

public class Competition {
    public String Name;
    public Date EndDate;
    public int Rank;
    public int ParticipantCount;
    public double PersonalScore;
    public String Description;
    public List<LeaderBoardEntry> LeaderBoardEntries;

    public Competition(JSONObject obj) {
        try {
            Name = obj.getString("name");
            EndDate = DateObjectHelper.CreateDateObject("enddate");
            Rank = obj.getInt("rank");
            ParticipantCount = obj.getInt("participantcount");
            PersonalScore = obj.getDouble("personalscore");
            Description = obj.getString("description");

            LeaderBoardEntries = new ArrayList<>();
            JSONArray jsonArray = obj.getJSONArray("leaderboard");
            for (int i = 0; i < jsonArray.length(); i++) {
                LeaderBoardEntries.add(new LeaderBoardEntry(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
        }
    }
}
