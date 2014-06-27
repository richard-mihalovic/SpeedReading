package info.energix.speedreading;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import info.energix.speedreading.models.AppSettings;
import info.energix.speedreading.models.Source;
import info.energix.speedreading.utils.Hash;

public class Settings {
    private static final String APP_ID = "info.energix.fast.reader";

    public static void saveAppSettings(Context context, AppSettings settings){
        SharedPreferences appSharedPrefs;
        SharedPreferences.Editor prefsEditor;

        appSharedPrefs = context.getSharedPreferences(APP_ID, Activity.MODE_PRIVATE);
        prefsEditor = appSharedPrefs.edit();

        prefsEditor.putString("appSettings", new Gson().toJson(settings));
        prefsEditor.commit();
    }

    public static AppSettings loadAppSettings(Context context){
        SharedPreferences appSharedPrefs;
        appSharedPrefs = context.getSharedPreferences(APP_ID, Activity.MODE_PRIVATE);

        String json = appSharedPrefs.getString("appSettings", "");

        return new Gson().fromJson(json, AppSettings.class);
    }

    public static void saveSources(Context context, ArrayList<Source> sources){
        String json = new Gson().toJson(sources);

        SharedPreferences appSharedPrefs;
        SharedPreferences.Editor prefsEditor;

        appSharedPrefs = context.getSharedPreferences(APP_ID, Activity.MODE_PRIVATE);
        prefsEditor = appSharedPrefs.edit();

        prefsEditor.putString("sources", new Gson().toJson(sources));
        prefsEditor.commit();
    }

    public static ArrayList<Source> loadSources(Context context){
        ArrayList<Source> sources = new ArrayList<Source>();

        SharedPreferences appSharedPrefs;
        appSharedPrefs = context.getSharedPreferences(APP_ID, Activity.MODE_PRIVATE);

        String json = appSharedPrefs.getString("sources", "");

        try {
            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(json).getAsJsonArray();

            for(int i = 0, n = array.size(); i < n; i++) {
                Gson gson = new Gson();

                Source source = gson.fromJson(array.get(i), Source.class);
                sources.add(source);
            }
        } catch (Exception e) {
            Log.e("loadSources", e.toString());
        }

        return sources;
    }

    public static void addSource(Context context, Source source){
        ArrayList<Source> sources = Settings.loadSources(context);
        sources.add(source);
        Settings.saveSources(context, sources);
    }

    public static void deleteSource(Context context, String sourceId){
        ArrayList<Source> sources = Settings.loadSources(context);

        for(int i=0, n=sources.size(); i < n; i++) {
            if(Hash.md5(sources.get(i).getPath()).equals(sourceId)) {
                sources.remove(i); break;
            }
        }

        Settings.saveSources(context, sources);
    }

    public static void save(Context context, String variable, String value) {
        SharedPreferences appSharedPrefs;
        SharedPreferences.Editor prefsEditor;

        appSharedPrefs = context.getSharedPreferences(APP_ID, Activity.MODE_PRIVATE);
        prefsEditor = appSharedPrefs.edit();

        prefsEditor.putString(variable, value);
        prefsEditor.commit();
    }

    public static String read(Context context, String variable) {
        SharedPreferences appSharedPrefs;
        appSharedPrefs = context.getSharedPreferences(APP_ID, Activity.MODE_PRIVATE);

        return appSharedPrefs.getString(variable, null);
    }
}
