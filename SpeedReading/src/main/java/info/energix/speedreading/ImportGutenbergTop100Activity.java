package info.energix.speedreading;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.energix.speedreading.models.Source;
import info.energix.speedreading.utils.Hash;

public class ImportGutenbergTop100Activity extends SherlockListActivity {
    private DefaultHttpClient client;
    private RequestQueue queue;
    private static ArrayList<String> items = new ArrayList<String>();

    private String downloadTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_import_gutenberg_top_100);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        client = new DefaultHttpClient();
        queue = Volley.newRequestQueue(this, new HttpClientStack(client));

        loadTop100List();
    }

    private void loadTop100List(){
        if(items.size() > 0) {
            setListAdapter(
                new SourcesAdapter(
                    this,
                    R.layout.activity_import_gutenberg_top_100_row,
                    items
                )
            );
            return;
        }

        Toast.makeText(
            getApplicationContext(),
            "Loading: Top 100 books list, please wait.",
            Toast.LENGTH_LONG
        ).show();

        String url = "http://www.gutenberg.org/browse/scores/top";
        StringRequest request;
        request = new StringRequest(
            url,
            new ResponseListener(),
            new ErrorListener()
        );

        setSupportProgressBarIndeterminateVisibility(true);
        queue.add(request);
    }

    private class ResponseListener implements Response.Listener<String>{
        @Override
        public void onResponse(String response) {
            Document doc = Jsoup.parse(response, "UTF-8");
            items.clear();

            Element content = doc.getElementById("books-last30");
            Elements links = content.nextElementSibling().getElementsByTag("a");
            for (Element link : links) {
                String linkHref = link.attr("href");
                String linkText = link.text();

                int pos = linkHref.lastIndexOf('/');
                if(pos > 0) {
                    String num = linkHref.substring(pos + 1);
                    linkHref = "/" + num + "/pg" + num + ".txt.utf8";

                    String parts[] = extractTitleDetails(linkText + ':' + linkHref);
                    String title = parts[0];
                    String author = parts[1];
                    String linkUrl = parts[2];

                    items.add(title + ':' + author + ':' + linkUrl);
                }
            }

            setListAdapter(
                new SourcesAdapter(ImportGutenbergTop100Activity.this,
                R.layout.activity_import_gutenberg_top_100_row,
                items
            ));

            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    private class ErrorListener implements Response.ErrorListener{
        @Override
        public void onErrorResponse(VolleyError error) {
            setSupportProgressBarIndeterminateVisibility(false);

            Toast.makeText(getApplicationContext(),
                "Error, Please Try Again:" + error.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        String line = (String) l.getItemAtPosition(position);

        String[] items = line.split(":");
        final String title = items[0];
        final String link = "http://gutenberg.readingroo.ms/cache/generated" + items[2];

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder
            .setTitle("DOWNLOAD")
            .setMessage("Download `" + title + "` to reading list ?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(
                        getApplicationContext(),
                        "Downloading: " + title,
                        Toast.LENGTH_LONG
                    ).show();
                    downloadFile(title, link);
                }
            });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void downloadFile(String title, String url) {
        downloadTitle = title;

        StringRequest request;
        request = new StringRequest(
            url,
            new DownloadResponseListener(),
            new ErrorListener()
        );

        setSupportProgressBarIndeterminateVisibility(true);
        queue.add(request);
    }

    private class DownloadResponseListener implements Response.Listener<String>{
        @Override
        public void onResponse(String response) {
            File f = new File("/data/data/info.energix.speedreading/" + downloadTitle + ".txt");

            try {
                FileOutputStream fos = new FileOutputStream(f);

                OutputStreamWriter osw = new OutputStreamWriter(fos);
                osw.append(response.toString());
                osw.close();

                fos.close();

                String fileName = f.getAbsolutePath();

                Source source = new Source();
                source.setTitle(downloadTitle);
                source.setPath(fileName);
                source.setWordCount(0);
                source.setWordCurrent(0);
                Settings.addSource(ImportGutenbergTop100Activity.this, source);

            } catch (Exception e) {
                Log.e("DOWNLOAD", e.toString());
            }

            setSupportProgressBarIndeterminateVisibility(false);
            finish();
        }
    }

    private String[] extractTitleDetails(String s) {
        String[] items = s.split(":");
        String title = items[0];
        String link = items[1];
        String author = "";

        int pos = title.lastIndexOf('(');
        if(pos > 0) {
            title = title.substring(0, pos);
        }

        pos = title.lastIndexOf(" by ");
        if(pos > 0) {
            author = title.substring(pos + 4);
            title = title.substring(0, pos);
        }

        return new String[]{title, author, link};
    }

    private class SourcesAdapter extends ArrayAdapter<String> {
        private ArrayList<String> items;

        public SourcesAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.activity_import_gutenberg_top_100_row, null);
            }

            String line = items.get(position);

            String[] items = line.split(":");
            String title = items[0];
            String author = items[1];
            String link = items[2];

            if (title != null && link != null) {
                TextView tv_title = (TextView) v.findViewById(R.id.title);
                if(tv_title != null)
                    tv_title.setText(title);

                TextView tv_author = (TextView) v.findViewById(R.id.author);
                if(tv_title != null)
                    tv_author.setText(author);
            }
            return v;
        }
    }

}