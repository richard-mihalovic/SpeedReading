package info.energix.speedreading;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.util.ArrayList;

import info.energix.speedreading.models.Document;
import info.energix.speedreading.utils.Hash;

public class MainActivity extends SherlockListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        refreshSources();
    }

    private void refreshSources(){
        setListAdapter(
            new DocumentsAdapter(
                this,
                R.layout.activity_main_row,
                Settings.loadDocuments(this)
            )
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("New")
            .setIcon(R.drawable.plus)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, ImportActivity.class);
        startActivity(intent);

        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        Document item = (Document) l.getItemAtPosition(position);

        if(!new File(item.getPath()).exists()) {
            Toast.makeText(
                getApplicationContext(),
                "ERROR: Can\'t read file from storage. Maybe it was deleted.",
                Toast.LENGTH_LONG
            ).show();
            return;
        }

        Intent intent = new Intent(this, ReaderActivity.class);
        intent.putExtra("fileName", item.getPath());
        startActivity(intent);
    }

    private class DocumentsAdapter extends ArrayAdapter<Document> {
        private ArrayList<Document> items;

        public DocumentsAdapter(Context context, int textViewResourceId, ArrayList<Document> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.activity_main_row, null);
            }

            final Document item = items.get(position);
            if (item != null) {
                TextView title = (TextView) v.findViewById(R.id.title);
                ImageButton btnDelete = (ImageButton) v.findViewById(R.id.delete);
                ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.readingProgress);

                if (title != null) {
                    title.setText(item.getTitle());
                    title.setTypeface(null, Typeface.NORMAL);
                }

                final String md5Id = Hash.md5(item.getPath());
                String progressStr = Settings.read(v.getContext(), md5Id + "_progress");
                if(progressStr != null && progressBar != null) {
                    progressBar.setProgress(
                        Integer.parseInt(progressStr)
                    );
                }

                if(btnDelete != null) {
                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                            dialog.setTitle("DELETE");
                            dialog.setMessage("Delete file: " + item.getTitle() + " ?");
                            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Settings.deleteDocument(MainActivity.this, md5Id);
                                    dialog.dismiss();
                                    refreshSources();
                                }
                            });
                            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.create().show();
                        }
                    });
                }

            }
            return v;
        }
    }

}
