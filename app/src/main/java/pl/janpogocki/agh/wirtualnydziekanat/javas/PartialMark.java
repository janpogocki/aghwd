package pl.janpogocki.agh.wirtualnydziekanat.javas;

/**
 * Created by Jan on 31.10.2017.
 * Partial Mark Class
 */

public class PartialMark {
    public String mark;
    public String title;
    public String subjectName;
    public String lectureName;
    public long timestamp;
    public String description;
    public String currentSemester;

    public PartialMark(String mark, String title, String subjectName, String lectureName, long timestamp, String description, String currentSemester) {
        this.mark = mark;
        this.title = title;
        this.subjectName = subjectName;
        this.lectureName = lectureName;
        this.timestamp = timestamp;
        this.description = description;
        this.currentSemester = currentSemester;
    }
}
