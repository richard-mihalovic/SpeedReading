package info.energix.speedreading;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class ImportActivity extends SherlockActivity{
    private Button b_import_file;
    private Button b_import_clipboard;
    private Button b_import_gutenberg_top_100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        b_import_file = (Button) findViewById(R.id.button_import_file);
        b_import_clipboard = (Button) findViewById(R.id.button_import_clipboard);
        b_import_gutenberg_top_100 = (Button) findViewById(R.id.button_import_gutenberg_top_100);

        b_import_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ImportFileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        b_import_clipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ImportClipboardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        b_import_gutenberg_top_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ImportGutenbergTop100Activity.class);
                startActivity(intent);
                finish();
            }
        });
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
