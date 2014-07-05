package info.energix.speedreading;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import info.energix.speedreading.models.Document;
import info.energix.speedreading.utils.IO;

public class ImportFileActivity extends SherlockListActivity {
    private static String scanPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED))
            scanPath = Environment.getExternalStorageDirectory().getPath();

        setListAdapter(
            new FilesAdapter(
                this,
                R.layout.activity_browser_row,
                scanDirectory()
            )
        );
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        String item = (String) l.getItemAtPosition(position);

        if(item.startsWith("D:")) {
            if(item == "D:..")
                scanPath = new File(scanPath).getParentFile().getPath();
            else
                scanPath = scanPath + '/' + item.substring(2);

            setListAdapter(
                new FilesAdapter(
                    v.getContext(),
                    R.layout.activity_browser_row,
                    scanDirectory()
                )
            );
        } else {
            final String fileName = scanPath + '/' + item.substring(2);
            final String title = item.substring(2);

            final Context context = v.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder
                .setTitle("SAVE")
                .setMessage("Add file: `" + title + "` to reading list ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Document document = new Document();
                            document.setTitle(title);
                            document.setPath(fileName);
                            document.setWordCount(0);
                            document.setWordCurrent(0);
                            Settings.addDocument(context, document);

                            finish();
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
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
    }

    private ArrayList<String> scanDirectory(){
        ArrayList<String> listDirs = new ArrayList<String>();
        ArrayList<String> listFiles = new ArrayList<String>();
        ArrayList<String> list = new ArrayList<String>();

        File[] dirs = IO.listFiles(scanPath, true);
        if(dirs != null) {
            for(File f : dirs) {
                if(!f.getName().startsWith("."))
                    listDirs.add("D:" + f.getName());
            }
        }

        File[] files = IO.listFiles(scanPath, false);
        if(files != null) {
            for(File f : files) {
                listFiles.add("F:" + f.getName());
            }
        }

        Collections.sort(listDirs);
        Collections.sort(listFiles);

        if(!scanPath.equals("") && !scanPath.equals("/"))
            list.add("D:..");

        list.addAll(listDirs);
        list.addAll(listFiles);

        return list;
    }

    private class FilesAdapter extends ArrayAdapter<String> {
        private ArrayList<String> items;

        public FilesAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.activity_browser_row, null);
            }

            String item = items.get(position);
            if (item != null) {
                TextView title = (TextView) v.findViewById(R.id.title);
                ImageView icon = (ImageView) v.findViewById(R.id.icon);

                if (title != null) {
                    if(item.startsWith("D:"))
                        title.setTypeface(null, Typeface.BOLD);
                    else
                        title.setTypeface(null, Typeface.NORMAL);

                    title.setText(item.substring(2));
                }

                if(icon != null) {
                    if(item.startsWith("D:"))
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.folder));
                    else
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.file));
                }
            }
            return v;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }
}
