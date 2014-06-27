package info.energix.speedreading.models;

public class AppSettings {
    private String fontStyle = null;
    private Integer fontSize = null;
    private Integer textSpeed = null;
    private String theme = null;

    public AppSettings(){
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public Integer getTextSpeed() {
        return textSpeed;
    }

    public void setTextSpeed(Integer textSpeed) {
        this.textSpeed = textSpeed;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}