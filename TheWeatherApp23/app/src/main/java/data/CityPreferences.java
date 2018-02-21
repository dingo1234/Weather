package data;


import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreferences {

    SharedPreferences prefs;
    public CityPreferences(Activity activity){
        prefs=activity.getPreferences(Activity.MODE_PRIVATE);
    }

    public String getCity(){
        return prefs.getString("city","Brcko,BA");
    }
    public void setCity(String city){
        prefs.edit().putString("city",city).commit();
    }
}
