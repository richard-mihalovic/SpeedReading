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
import info.energix.speedreading.models.Document;
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

    public static void saveDocuments(Context context, ArrayList<Document> documents){
        String json = new Gson().toJson(documents);

        SharedPreferences appSharedPrefs;
        SharedPreferences.Editor prefsEditor;

        appSharedPrefs = context.getSharedPreferences(APP_ID, Activity.MODE_PRIVATE);
        prefsEditor = appSharedPrefs.edit();

        prefsEditor.putString("documents", new Gson().toJson(documents));
        prefsEditor.commit();
    }

    public static ArrayList<Document> loadDocuments(Context context){
        ArrayList<Document> documents = new ArrayList<Document>();

        SharedPreferences appSharedPrefs;
        appSharedPrefs = context.getSharedPreferences(APP_ID, Activity.MODE_PRIVATE);

        String json = appSharedPrefs.getString("documents", "");

        try {
            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(json).getAsJsonArray();

            for(int i = 0, n = array.size(); i < n; i++) {
                Gson gson = new Gson();

                Document document = gson.fromJson(array.get(i), Document.class);
                documents.add(document);
            }
        } catch (Exception e) {
            Log.e("loadDocuments", e.toString());
        }

        return documents;
    }

    public static void addDocument(Context context, Document document){
        ArrayList<Document> documents = Settings.loadDocuments(context);
        documents.add(document);
        Settings.saveDocuments(context, documents);
    }

    public static void deleteDocument(Context context, String sourceId){
        ArrayList<Document> documents = Settings.loadDocuments(context);

        for(int i=0, n= documents.size(); i < n; i++) {
            if(Hash.md5(documents.get(i).getPath()).equals(sourceId)) {
                documents.remove(i); break;
            }
        }

        Settings.saveDocuments(context, documents);
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
