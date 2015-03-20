package models;

public class Gatherstatus {
    public enum JobStatus {
	queued, running, paused, succeded, failed
    }

    public JobStatus jobStatus;
}
