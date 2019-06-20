package co.particket.particket;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Customers extends AppCompatActivity implements AdapterView.OnItemClickListener {
    TextView tv, dName, dDate, dAvailable, dUsed, dPrice, dHead;
    Button dClose;

    ListView lst;
    ArrayAdapter<Event> eventAdapter;
    ImageView img;
    ProgressDialog p;
    Dialog dialog;
    Customer customer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);
        lst = findViewById(R.id.lst);
        tv = (TextView)findViewById(R.id.tv);
        SharedPreferences sp = getSharedPreferences("customer",0);
        String str = sp.getString("qr",null);
        tv.setText(str);
        img = findViewById(R.id.barcode);

        DownLoadImages d = new DownLoadImages();
        d.execute("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="+str+"&color=000000.png");

        setEventAdapter();
        lst.setOnItemClickListener(this);
    }

    /**
     * this function set the adapter
     */
    public void setEventAdapter(){
        eventAdapter = new ArrayAdapter<Event>(this, R.layout.list_status, R.id.text1, Data.eventLstSearch) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Event ev = (Event) lst.getAdapter().getItem(position);
                TextView text = view.findViewById(R.id.text1);
                ImageView icon = view.findViewById(R.id.icon);
                text.setText(ev.getEventName());
                int pp = 2;
                if(Data.isEventToday(ev) <= 0 && ev.isSaleActive()) pp=1;
                int iconResId = getResources().getIdentifier("status" + pp, "drawable", getPackageName());
                icon.setImageResource(iconResId);
                return view;
            }
        };
        lst.setAdapter(eventAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.button_bar3,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.exit){
            SharedPreferences sp = getSharedPreferences("customer",0);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("qr");
            editor.commit();

            Intent intent = new Intent(this,Scanner1.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        customer = getCustomerByEventId(eventAdapter.getItem(i).getEventID());
        if(customer!=null) dialogShow(eventAdapter.getItem(i).getEventName());
    }

    /**
     * this function open dialog with the details of the chosen ticket
     * @param title which is the title of the dialog
     */
    public void dialogShow(String title) {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_customer);

        dName = dialog.findViewById(R.id.cus_name);
        dDate = dialog.findViewById(R.id.per_date);
        dAvailable = dialog.findViewById(R.id.available);
        dUsed = dialog.findViewById(R.id.used);
        dPrice = dialog.findViewById(R.id.price);
        dHead = dialog.findViewById(R.id.head_line);
        dClose = dialog.findViewById(R.id.btn_ok);

        dHead.setText(title);
        dName.setText(customer.getCustomerName());
        if(customer.getDate() != null) {
            String[] kk = customer.getDate().split("/");
            dDate.setText(kk[2]+"/"+kk[1]+"/"+kk[0]);
        }
        else dDate.setVisibility(View.GONE);

        dAvailable.setText(String.valueOf(customer.getQuantity()));
        dUsed.setText(String.valueOf(customer.getNumberEnters()));
        dPrice.setText(getDoubleString(customer.getTotalPrice()));

        dClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Customers.this.dialog.dismiss();
            }
        });

        dialog.show();
    }

    public String getDoubleString(double da){
        if(da%1 ==0) return String.valueOf((int)da);
        else return String.format("%.2f", da);
    }

    /**
     * this function search the customer by the event id
     * @return the customer
     */
    public Customer getCustomerByEventId(String evId){
        for(int i=0; i<Data.customersLstSearch.size(); i++){
            Customer cus = Data.customersLstSearch.get(i);
            if(cus.getEventId().equals(evId)) return cus;
        }
        return null;
    }


    /**
     * this class set the image of the barcode
     */
    public class DownLoadImages extends AsyncTask<String, Integer, Bitmap>
    {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            p = new ProgressDialog(Customers.this);
            p.setMessage("Loading");
            p.setCancelable(false);
            p.show();

        }
        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO Auto-generated method stub

            Bitmap b=null;
            try {
                URL url=new URL(params[0]);
                HttpURLConnection httpcon=(HttpURLConnection) url.openConnection();

                if(httpcon.getResponseCode()!=200)
                    throw new Exception("Faild to connect");

                InputStream is=httpcon.getInputStream();
                b = BitmapFactory.decodeStream(is);
            }
            catch (MalformedURLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return b;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            ImageView iv=(ImageView) findViewById(R.id.barcode);
            if(result!=null)
                iv.setImageBitmap(result);
            p.dismiss();


        }
    }

}
