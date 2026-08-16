package com.example.subcreator;

public class SubtitleItem {
    private int id;
    private String startTime;
    private String endTime;
    private String originalText;

    public SubtitleItem(int id, String startTime, String endTime, String originalText) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.originalText = originalText;
    }

    public int getId() { return id; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getOriginalText() { return originalText; }

    public String toSrtFormat() {
        return id + "\n" + startTime + " --> " + endTime + "\n" + originalText + "\n\n";
    }
}
