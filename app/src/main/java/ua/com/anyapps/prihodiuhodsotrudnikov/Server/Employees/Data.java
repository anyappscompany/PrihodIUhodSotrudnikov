package ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees;

public class Data
{
    private String surname;

    private String pin;

    private String name;

    private String photo;

    private String _id;

    public String getSurname ()
    {
        return surname;
    }

    public void setSurname (String surname)
    {
        this.surname = surname;
    }

    public String getPin ()
    {
        return pin;
    }

    public void setPin (String pin)
    {
        this.pin = pin;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getPhoto ()
    {
        return photo;
    }

    public void setPhoto (String photo)
    {
        this.photo = photo;
    }

    public String get_id ()
    {
        return _id;
    }

    public void set_id (String _id)
    {
        this._id = _id;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [surname = "+surname+", pin = "+pin+",name = "+name+", photo = "+photo+", _id = "+_id+"]";
    }
}
