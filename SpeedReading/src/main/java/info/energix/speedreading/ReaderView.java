package info.energix.speedreading;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class ReaderView extends View {
	
	private final int MAX_FONT_SCALE = 10;
	
    private Paint paintText;
    private Paint paintBackground;

    private int textWidth = -1;
    private int textHeight = -1;
    private int textSize = MAX_FONT_SCALE;

    private int screenWidth = -1;
    private int screenHeight = -1;

    private Boolean themeChanged = true;
    private String theme = "light"; // light / dark / sepia
    private String fontStyle = "default"; // default / type_a

    private int textBufferPosition = 0;
    private int textBufferSize = 0;

    private RandomAccessFile file;
    private String actualWord;
    private char lastDirection = '+'; 

    public ReaderView(Context context) {
        super(context);
    }

    public ReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void init(String fileName, int position) {
    	try {
    		file = new RandomAccessFile(fileName, "r");
			textBufferPosition = position;
			textBufferSize = (int)file.length();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    	
    	showNextWord();
    }

    public void deinit() {
        if(file != null) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(screenWidth < 0)
           screenWidth = canvas.getWidth();

        if(screenHeight < 0)
            screenHeight = canvas.getHeight();

        setupVariables();
        setupTheme();

        String text = actualWord;

        if(text == null) text = "--- END ---";

        measureTextSize(text);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paintBackground);
        canvas.drawText(
            text,
            (screenWidth / 2.0f) - (textWidth / 2.0f),
            (screenHeight / 2.0f),
            paintText
        );
    }
  
    private void setupVariables() {
        if(paintBackground == null)
            paintBackground = new Paint();

        if(paintText == null)
            paintText = new Paint();

        setupBackground();
        setupFont("default");
    }

    private void setupBackground(){
        paintBackground.setStyle(Paint.Style.FILL);
    }

    public void switchFont(){
        if(fontStyle.equals("default")) {
            fontStyle = "type_a";
            setupFont(fontStyle);
        } else {
            fontStyle = "default";
            setupFont(fontStyle);
        }
    }

    private void setupFont(String font){
        Typeface tf;

        if(fontStyle.equals("default")) {
            tf = Typeface.createFromAsset(
                getContext().getAssets(),
                "fonts/LibreBaskerville-Regular.ttf"
            );
        } else {
            tf = Typeface.createFromAsset(
                getContext().getAssets(),
                "fonts/Ubuntu-R.ttf"
            );
        }

        paintText.setTypeface(tf);
        paintText.setAntiAlias(true);
        paintText.setTextSize(screenHeight / (5 + textSize));
    }

    private void setupTheme(){
        if(!themeChanged)
            return;

        if(theme.equals("light")) {

            paintBackground.setColor(Color.WHITE);
            paintText.setColor(Color.BLACK);

        } else if(theme.equals("dark")) {

            paintBackground.setColor(Color.BLACK);
            paintText.setColor(Color.WHITE);

        } else if(theme.equals("sepia")) {

            paintBackground.setARGB(255, 239, 224, 185);
            paintText.setARGB(255, 100, 59, 15);

        }

        themeChanged = false;
    }

    private void measureTextSize(String text){
        Rect bounds = new Rect();

        paintText.getTextBounds( text, 0, text.length(), bounds );
        textHeight = bounds.height();
        textWidth = bounds.width();
    }

    public void showNextWord(){
    	
    	if (isFinished())
    		return;
    	
    	final int BUFFER_SIZE = 64;
    	byte buffer[] = new byte[BUFFER_SIZE];
    	
    	try
		{
    		int readBytesCount = BUFFER_SIZE;
    		if (textBufferPosition + readBytesCount > textBufferSize)
    			readBytesCount = textBufferSize-textBufferPosition;
    		
    		file.seek(textBufferPosition);
			file.read (buffer, 0, readBytesCount);
			
			int i=0;
			while (Character.isWhitespace(buffer[i]))
				i++;
			
			int start = i;
			
			while (i<readBytesCount && !Character.isWhitespace(buffer[i]))
				i++;
			
			int end = i;
			
			actualWord = new String(buffer, start, end-start);
			
            textBufferPosition += end;
            if (textBufferPosition >= textBufferSize)
            	textBufferPosition = textBufferSize-1;
            
            if (lastDirection == '-')
            {
            	lastDirection = '+';
            	showNextWord();
            }
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
        
        invalidate();
    }

    public void showPreviousWord(){

    	if (textBufferPosition == 0)
    		return;

    	final int BUFFER_SIZE = 64;
    	byte buffer[] = new byte[BUFFER_SIZE];
    	
    	try
		{
    		int readBytesCount = BUFFER_SIZE;
    		
    		int seekPos = textBufferPosition-BUFFER_SIZE;
    		if (seekPos < 0)
    		{
    			readBytesCount += seekPos;
    			seekPos = 0;
    		}
    		
    		file.seek(seekPos);
			file.read (buffer, 0, readBytesCount);
			
			int i=readBytesCount-1;
			while (Character.isWhitespace(buffer[i]))
				i--;
			
			int end = i;
			
			while (i>=0 && !Character.isWhitespace(buffer[i]))
			{
				i--;
			}
			
			int start = i;
			
			actualWord = new String(buffer, start+1, end-start);
			
            textBufferPosition -= end-start+1;
            if (textBufferPosition < 0) textBufferPosition = 0;
            
            if (lastDirection == '+')
            {
            	lastDirection = '-';
            	showPreviousWord();
            }
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
        
        invalidate();
    }

    public Integer getTextSize(){
        return textSize;
    }
    
    public Integer getTextSizeLabel(){
        return MAX_FONT_SCALE - textSize + 1;
    }    

    public void setTextSize(Integer textSize) {
        this.textSize = textSize;
    }

    public void increaseFontSize(){
        if(textSize > 1)
            textSize--;
    }

    public void decreaseFontSize(){
        if(textSize < MAX_FONT_SCALE)
            textSize++;
    }

    public void setTheme(String theme){
        themeChanged = true;
        this.theme = theme;
    }

    public void setFontStyle(String style){
        this.fontStyle = style;
    }

    public String getFontStyle(){
        return fontStyle;
    }

    public void  setTextBufferPosition(int position){
        textBufferPosition = position;
    }

    public int getTextPosition(){
        return textBufferPosition;
    }

    public float getPositionAsPercentage(){
    	return textBufferPosition / (float) textBufferSize;
    }
    
    public boolean isFinished(){
    	return textBufferPosition == textBufferSize-1;
    }
    
    public void rewind() {
    	textBufferPosition = 0;
    }
}
