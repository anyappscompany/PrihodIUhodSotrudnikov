package ua.com.anyapps.prihodiuhodsotrudnikov.Server.UploadEmployee;

public class UploadEmployee
{
    private String data;

    private Boolean sucess;

    public String getData ()
    {
        return data;
    }

    public void setData (String data)
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
