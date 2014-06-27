package info.energix.speedreading;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import info.energix.speedreading.models.AppSettings;
import info.energix.speedreading.utils.Hash;

public class ReaderActivity extends Activity {
	
	private final long FADE_TIMEOUT = 5000;
	
    private ReaderView readerView;
    private Timer timer;
    private AppSettings appSettings;

    private int textSpeed = 200;
    private Boolean isPaused = true;

    private String theme = "light";

    private Button buttonSpeedUp;
    private Button buttonSpeedDown;
    private Button buttonFontUp;
    private Button buttonFontDown;
    private ImageButton buttonChangeTheme;
    private ImageButton buttonPlay;
    private Button buttonJumpNext;
    private Button buttonJumpPrevious;
    private Button buttonFontSwitch;
    private TextView speedText, fontSizeText;
    private ProgressBar textProgress;
    private View topButtonsLayout, playButtonsLayout;

    private static String md5Id;

    private long fadeTimerStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_reader);

        String fileName = getIntent().getExtras().getString("fileName");
        md5Id = Hash.md5(fileName);

        int position = 0;
        String textPosition = Settings.read(this, md5Id + "_position");
        if(textPosition != null)
        	position = Integer.parseInt(textPosition);

        readerView = (ReaderView) findViewById(R.id.reader);
        readerView.init(fileName, position);

        appSettings = Settings.loadAppSettings(this);
        if(appSettings != null) {
            if(appSettings.getTextSpeed() != null)
                textSpeed = appSettings.getTextSpeed();
            if(appSettings.getTheme() != null) {
                theme = appSettings.getTheme();
                readerView.setTheme(theme);
            }
            if(appSettings.getFontSize() != null)
                readerView.setTextSize(appSettings.getFontSize());
            if(appSettings.getFontStyle() != null)
                readerView.setFontStyle(appSettings.getFontStyle());

        } else appSettings = new AppSettings();

        setupUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();

        updateProgress();
        setupTimer();
    }

    @Override
    public void onPause(){
        super.onPause();

        appSettings.setFontSize(readerView.getTextSize());
        appSettings.setFontStyle(readerView.getFontStyle());
        appSettings.setTextSpeed(textSpeed);
        appSettings.setTheme(theme);
        Settings.saveAppSettings(this, appSettings);

        Settings.save(
            this,
            md5Id + "_position",
            Integer.toString(readerView.getTextPosition())
        );
        Settings.save(
            this,
            md5Id + "_progress",
            Integer.toString(
                (int) (readerView.getPositionAsPercentage() * 100)
            )
        );
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        readerView.deinit();
        timer.cancel();
    }

    private void setupUI(){
        buttonFontUp = (Button) findViewById(R.id.buttonFontUp);
        buttonFontDown = (Button) findViewById(R.id.buttonFontDown);

        buttonSpeedDown = (Button) findViewById(R.id.buttonSpeedDown);
        buttonSpeedUp = (Button) findViewById(R.id.buttonSpeedUp);

        buttonChangeTheme = (ImageButton) findViewById(R.id.buttonChangeTheme);

        buttonFontSwitch = (Button) findViewById(R.id.buttonFontSwitch);

        buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
        buttonJumpNext = (Button) findViewById(R.id.buttonJumpNext);
        buttonJumpPrevious = (Button) findViewById(R.id.buttonJumpPrevious);
        
        speedText = (TextView) findViewById(R.id.speedText);
        fontSizeText = (TextView) findViewById(R.id.fontSizeText);
        textProgress = (ProgressBar) findViewById(R.id.textProgress);
        textProgress.setMax(100);
        
        topButtonsLayout = findViewById(R.id.topButtonsLayout);
        playButtonsLayout = findViewById(R.id.playButtonsLayout);

        updateTextSize();
        updateSpeed();
        
        readerView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				isPaused = !isPaused;
                showButtons(true);
				resetFadeTimer();
			}
		});
        
        buttonFontUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	readerView.increaseFontSize();
            	updateTextSize();
            	readerView.invalidate();
            }
        });

        buttonFontDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readerView.decreaseFontSize();
                updateTextSize();
                readerView.invalidate();
            }
        });

        buttonSpeedUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textSpeed -= 10;
                if (textSpeed <= 100)
                	textSpeed = 100;
                
                timerChangeSpeed(textSpeed);
                updateSpeed();
                readerView.invalidate();
            }
        });

        buttonSpeedDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textSpeed += 10;

                timerChangeSpeed(textSpeed);
                updateSpeed();
                readerView.invalidate();
            }
        });

        buttonChangeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(theme.equals("light") || theme.equals(""))
                    theme = "dark";
                else if(theme.equals("dark"))
                    theme = "sepia";
                else if(theme.equals("sepia"))
                    theme = "light";

                readerView.setTheme(theme);
                readerView.invalidate();
                resetFadeTimer();
            }
        });

        buttonFontSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readerView.switchFont();
                readerView.invalidate();
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            	if (readerView.isFinished())
            	{
            		readerView.rewind();
            		isPaused = false;
            	}
            	else
            		isPaused = !isPaused;
            	
            	resetFadeTimer();
            }
        });

        buttonJumpPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readerView.showPreviousWord();
                isPaused = true;
                updateProgress();
            }
        });

        buttonJumpNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readerView.showNextWord();
                isPaused = true;
                updateProgress();
            }
        });
    }

    private void updateTextSize(){
    	int textSize = readerView.getTextSizeLabel();
    	fontSizeText.setText(Html.fromHtml("<b>FONT</b><br/>" + textSize));
    	resetFadeTimer();
	}

    private void updateSpeed(){
    	int speed = Math.round(1000.0f / textSpeed * 60.0f);
    	speedText.setText(Html.fromHtml("<b>SPEED</b><br/>" + speed + " wpm"));
    	resetFadeTimer();
	}

    private void updateProgress(){
    	textProgress.setProgress((int) (readerView.getPositionAsPercentage()*100));
    }
    
    private void showButtons(boolean show){
    	topButtonsLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    	playButtonsLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    	textProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void resetFadeTimer(){
    	fadeTimerStart = System.currentTimeMillis();
    }

    private void setupTimer(){
    	timerChangeSpeed(textSpeed);
    }

    
    private void timerChangeSpeed(int speed){
    	if (timer != null)
    	{
	        timer.cancel();
	        timer.purge();
	        timer = null;
    	}

        timer = new Timer();
        timer.scheduleAtFixedRate(
            new TimerTask() {
            	
                @Override
                public void run() {
                	
                    if(!isPaused) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            	
                            	// Fade out
                            	if (System.currentTimeMillis() - fadeTimerStart > FADE_TIMEOUT)
                            		showButtons(false);
                            	
                                readerView.showNextWord();
                                updateProgress();
                                
                                if (readerView.isFinished())
                                	isPaused = true;
                            }
                        });
                    }
                }
            },
            0,
            speed
        );
    }
}