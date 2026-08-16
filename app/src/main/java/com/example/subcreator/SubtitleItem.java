package com.example.subcreator;

public class SubtitleItem {
    private int id;
    private String startTime;
    private String endTime;
    private String originalText;   // Tiếng Trung
    private String translatedText; // Tiếng Việt

    public SubtitleItem(int id, String startTime, String endTime, String originalText, String translatedText) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.originalText = originalText;
        this.translatedText = translatedText;
    }

    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getOriginalText() { return originalText; }
    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public String toSrtFormat() {
        return id + "\n" + startTime + " --> " + endTime + "\n" + translatedText + "\n\n";
    }
}
