package ua.com.anyapps.prihodiuhodsotrudnikov.Server.DeviceLogin;

public class Data {
    private String access_key;

    public String getAccess_key ()
    {
        return access_key;
    }

    public void setAccess_key (String access_key)
    {
        this.access_key = access_key;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [access_key = "+access_key+"]";
    }
}
