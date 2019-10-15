package ua.com.anyapps.prihodiuhodsotrudnikov.Server.AccessKey;

public class AccessKey {
    private Data data;

    private Boolean sucess;

    public Data getData ()
    {
        return data;
    }

    public void setData (Data data)
    {
        this.data = data;
    }

    public Boolean getSucess ()
    {
        return sucess;
    }

    public void setSucess (Boolean sucess)
    {
        this.sucess = sucess;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [data = "+data+", sucess = "+sucess+"]";
    }
}
