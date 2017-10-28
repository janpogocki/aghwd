package pl.janpogocki.agh.wirtualnydziekanat.javas;

/**
 * Created by Jan on 22.10.2017.
 */

public class Appointment {
    public long startTimestamp;
    public long stopTimestamp;
    public String name;
    public String description;
    public String location;
    public boolean lecture;
    public boolean aghEvent;
    public int tag;
    public double group;
    public boolean showDateBar;

    public Appointment(long startTimestamp, long stopTimestamp, String name, String description, String location, boolean lecture, boolean aghEvent, int tag, double group, boolean showDateBar) {
        this.startTimestamp = startTimestamp;
        this.stopTimestamp = stopTimestamp;
        this.name = name;
        this.description = description;
        this.location = location;
        this.lecture = lecture;
        this.aghEvent = aghEvent;
        this.tag = tag;
        this.group = group;
        this.showDateBar = showDateBar;
    }
}
