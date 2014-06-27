package info.energix.speedreading;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import info.energix.speedreading.models.Source;

public class ImportClipboardActivity extends SherlockActivity{
    private Button b_save;
    private TextView tv_pastedText;
    private EditText et_saveFileAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setContentView(R.layout.activity_import_from_clipboard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        b_save = (Button) findViewById(R.id.button_save);
        tv_pastedText = (TextView) findViewById(R.id.pastedText);
        et_saveFileAs = (EditText) findViewById(R.id.saveFileAs);

        android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        if(clipboard.hasText())
            tv_pastedText.setText(clipboard.getText());

        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_saveFileAs.getText().toString().equals("")) {
                    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(v.getContext());
                    dlgAlert.setMessage("Please enter valid 'save as' string.");
                    dlgAlert.setTitle("Warning");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();

                    return;
                }

                final Context context = v.getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder
                    .setTitle("SAVE ?")
                    .setMessage("Save clipboard content to reading list ?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                File f = new File("/data/data/info.energix.speedreading/" + et_saveFileAs.getText());

                                FileOutputStream fos = new FileOutputStream(f);

                                OutputStreamWriter osw = new OutputStreamWriter(fos);
                                osw.append(tv_pastedText.getText());
                                osw.close();

                                fos.close();

                                String fileName = f.getAbsolutePath();

                                Source source = new Source();
                                source.setTitle(et_saveFileAs.getText().toString());
                                source.setPath(fileName);
                                source.setWordCount(0);
                                source.setWordCurrent(0);
                                Settings.addSource(context, source);

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
        });
    }
}

// TODO: potvrdit pridanie textu a/n
