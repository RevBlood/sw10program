package sw10.ubiforsikring.Objects.CompetitionObjects;

import android.util.Log;

import org.json.JSONObject;

public class CompetitionListItem {
    public int CompetitionId;
    public String CompetitionName;
    public int ParticipantCount;
    public boolean IsParticipating;
    public int Rank;
    public int AttemptCount;

    public CompetitionListItem(int competitionId, String competitionName, int participantCount, boolean isParticipating, int rank, int attemptCount) {
        CompetitionId = competitionId;
        CompetitionName = competitionName;
        ParticipantCount = participantCount;
        IsParticipating = isParticipating;
        Rank = rank;
        AttemptCount = attemptCount;
    }

    public CompetitionListItem(JSONObject obj) {
        try {
            CompetitionId = obj.getInt("competitionid");
            CompetitionName = obj.getString("competitionname");
            ParticipantCount = obj.getInt("participantcount");
            IsParticipating = obj.getBoolean("isparticipating");
            Rank = obj.getInt("rank");
            AttemptCount = obj.getInt("attemptcount");
        }
        catch (Exception e){
            Log.e("Debug", "Competition JsonObject Constructor: ", e);
        }
    }

    @Override public String toString() {
        String result = "";
        String NEW_LINE = System.getProperty("line.separator");

        result += this.getClass().getName() + " Object {" + NEW_LINE;
        result += " CompetitionId: " + CompetitionId + NEW_LINE;
        result += " CompetitionName: " + CompetitionName + NEW_LINE;
        result += " ParticipantCount: " + ParticipantCount + NEW_LINE;
        result += " IsParticipating: " + IsParticipating + NEW_LINE;
        result += " Rank: " + Rank + NEW_LINE;
        result += " AttemptCount: " + AttemptCount + NEW_LINE;

        return result;
    }
}

