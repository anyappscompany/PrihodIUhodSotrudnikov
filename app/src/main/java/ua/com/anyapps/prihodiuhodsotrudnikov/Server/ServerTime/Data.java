package ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerTime;

public class Data {
    private String server_time;

    public String getServer_time ()
    {
        return server_time;
    }

    public void setServer_time (String server_time)
    {
        this.server_time = server_time;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [server_time = "+server_time+"]";
    }
}
