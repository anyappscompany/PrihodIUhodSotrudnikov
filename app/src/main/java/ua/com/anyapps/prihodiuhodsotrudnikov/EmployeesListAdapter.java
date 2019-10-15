package ua.com.anyapps.prihodiuhodsotrudnikov;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Data;

public class EmployeesListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater lInflater;
    //ArrayList<Product> objects;
    ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Data objects[];
    private static final String TAG = "debapp";

    public EmployeesListAdapter(Context _context, Data[] _objects) {
        this.context = _context;
        this.objects = _objects;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.length;
    }

    @Override
    public Object getItem(int position) {
        return objects[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.employees_list_item, parent, false);
        }

        ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Data e = getEmployee(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        TextView tvEmployeeNameSurname;
        ImageView ivEmployeePhoto;
        ConstraintLayout layEmployeeListItem;
        tvEmployeeNameSurname = (TextView) view.findViewById(R.id.tvEmployeeNameSurname);
        ivEmployeePhoto = (ImageView) view.findViewById(R.id.ivEmployeePhoto);
        layEmployeeListItem = (ConstraintLayout) view.findViewById(R.id.layEmployeeListItem);

        layEmployeeListItem.setTag(e.get_id() + ":" + e.getPin());
        tvEmployeeNameSurname.setText(e.getSurname() + " " + e.getName());

        //Log.d(TAG, "PHOTOPATH - " + e.getPhoto());

        Picasso.get().load(e.getPhoto()).into(ivEmployeePhoto);

        return view;
    }

    // товар по позиции
    ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Data getEmployee(int position) {
        return ((ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Data) getItem(position));
    }
}
