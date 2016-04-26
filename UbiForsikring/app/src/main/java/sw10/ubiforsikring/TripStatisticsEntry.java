package sw10.ubiforsikring;

public class TripStatisticsEntry {
    public String Title;
    public double Optimality;
    public String DescriptionText;
    public String PerHundredText;

    public boolean IsSection = false;

    public TripStatisticsEntry(String sectionTitle) {
        Title = sectionTitle;
        IsSection = true;
    }

    public TripStatisticsEntry(String title, double optimality, String descriptionText, String perHundredText) {
        Title = title;
        Optimality = optimality;
        //Price = price;
        DescriptionText = descriptionText;
        PerHundredText = perHundredText;
    }
}
