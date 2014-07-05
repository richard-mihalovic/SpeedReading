package info.energix.speedreading.models;

public class Document {
    private String title = "";
    private String path = "";
    private Integer wordCount = 0;
    private Integer wordCurrent = 0;

    public Document(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public Integer getWordCurrent() {
        return wordCurrent;
    }

    public void setWordCurrent(Integer wordCurrent) {
        this.wordCurrent = wordCurrent;
    }
}
