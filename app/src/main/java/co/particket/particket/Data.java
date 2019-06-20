package co.particket.particket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Data {
    private static UserDetails ud; //user details
    private static ArrayList<Event> myEv; //events of the user
    private static ArrayList<Event> mySa; //events could be sold by the user
    private static ArrayList<Seller> mySe; //all the sellers of user's events
    private static ArrayList<Customer> myCu; //all the customers of the user
    private static ArrayList<Event> myTi; //all the events that take place today
    private static ArrayList<Event> sort1, sort2; //sort events by category
    private static Context context;
    private static ProgressDialog pd;
    private static Bitmap profileImg; //user's profile image
    private static Bitmap tempProfileImg;

    public static ArrayList<Customer> customersLstSearch;
    public static ArrayList<Event> eventLstSearch;
    public Data(){}

    /**
     * load user data from firebase.
     */
    public static void loadData(Context context){
        Data.context = context;
        pd = new ProgressDialog(context);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        loadMyDetails();
        myEv = new ArrayList<>();
        mySa = new ArrayList<>();
        myCu = new ArrayList<>();
        mySe = new ArrayList<>();
        myTi = new ArrayList<>();

        loadMyEvents();
        //loadMySalesEvents();
        //loadMyCustomers();
    }

    /**
     * reset all user's data (on log out)
     */
    public static void rest() {
        ud = null;
        myEv = null;
        mySa = null;
        mySe = null;
        myCu = null;
        myTi = null;
        profileImg = null;
    }

    /**
     * @return user profile image
     */
    public static Bitmap getProfileImg(){return profileImg;}

    /**
     * @param bmp the new value of profileImg
     */
    public static void setProfileImg(Bitmap bmp){
        profileImg = bmp;
    }

    /**
     * @return user details
     */
    public static UserDetails getUd() {
        if(ud == null) loadMyDetails();
        return ud;
    }

    /**
     * @return array events of the user
     */
    public static ArrayList<Event> getMyEv() {
        if(myEv == null) loadMyEvents();
        return myEv;
    }

    /**
     * @return array events could be sold by the user
     */
    public static ArrayList<Event> getMySa() {
        if(mySa == null) loadMySalesEvents();
        return mySa;
    }

    /**
     * @return array all the sellers of user's events
     */
    public static ArrayList<Seller> getMySe() {
        return mySe;
    }

    /**
     * @return array all the customers of the user
     */
    public static ArrayList<Customer> getMyCu() {
        if(myCu == null) loadMyCustomers();
        return myCu;
    }

    /**
     * @return array all the events that take place today
     */
    public static ArrayList<Event> getMyTi() {
        return myTi;
    }

    /**
     * reset the array sorted events
     */
    public static void sortRest(){
        sort1=null;
        sort2=null;
    }

    /**
     * this function returns new sorted array of event
     * @param aa sorted events that events that have occurred or not yet
     * @return sorted array of events
     */
    public static ArrayList<Event> getSortList(boolean aa){
        if(sort1 == null || sort2 == null){
            sort1= new ArrayList<>();
            sort2= new ArrayList<>();
            for(int i=0; i<mySa.size(); i++){
                if(isEventToday(mySa.get(i)) <= 0)
                    sort1.add(mySa.get(i));
                else sort2.add(mySa.get(i));
            }
        }
        if(aa) return sort1;
        else return sort2;
    }

    /**
     * this function returns new sorted array of customers
     * @param evID sorted customer of specific event
     * @return sorted array of customers who belong the same event
     */
    public static ArrayList<Customer> getSortList1(String evID){
        ArrayList<Customer> sort3 = new ArrayList<>();
        for(int i=0; i<myCu.size(); i++) {
            if(myCu.get(i).getEventId().equals(evID))
                sort3.add(myCu.get(i));
        }
        return sort3;
    }

    /**
     * this function returns new sorted array of customers
     * @return sorted array of customers who can charge their ticket
     */
    public static ArrayList<Customer> getSortList2(){
        ArrayList<Customer> sort4 = new ArrayList<>();
        for(int i=0; i<myCu.size(); i++) {
            if(myCu.get(i).isChargeable())
                sort4.add(myCu.get(i));
        }
        return sort4;
    }

    /**
     * this function returns if the event today or in the future or past
     * @param eve and determinate if it took place
     * @return -2 have not date / 0 today / -1 future / 1 past
     */
    public static int isEventToday(Event eve){
        if(eve.getDate()== null) return -2;
        else{
            String date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
            if(eve.getDate().equals(date)) return 0;
            String eventDate[] =  eve.getDate().split("/");
            String today[] = date.split("/");
            if(today[0].equals(eventDate[0])){
                if(eventDate[1].equals(today[1])){
                    if(Integer.valueOf(eventDate[2]) < Integer.valueOf(today[2])) return 1;
                    else return -1;
                }
                if(Integer.valueOf(eventDate[1]) < Integer.valueOf(today[1])) return 1;
                else return -1;
            }
            if(Integer.valueOf(eventDate[0]) < Integer.valueOf(today[0])) return 1;
            else return -1;
        }
    }

    /**
     * this function load the customers
     */
    public static void loadMyCustomers(){
        FirebaseFirestore.getInstance().collection("Customers")
                .whereEqualTo("sellerUid",FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult())
                        if (document.exists()) myCu.add(document.toObject(Customer.class));
                    Data.goIntent();
                }

            }
        });
    }
    public static void goIntent(){
        if(context!=null) {
            context.startActivity(new Intent(context, UserArea.class));
            pd.dismiss();
        }
    }

    /**
     * this function load the events that the user can sell (from firebase)
     */
    public static void loadMySalesEvents(){
        FirebaseFirestore.getInstance().collection("Sellers")
                .whereEqualTo("sellerUid",FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        if(document.exists()){
                            Seller s = document.toObject(Seller.class);
                            if(s.isActive()){
                                FirebaseFirestore.getInstance().collection("Events")
                                        .whereEqualTo("eventID",s.getEventID())
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            for(QueryDocumentSnapshot document : task.getResult())
                                                if(document.exists()) {
                                                    Event ee = document.toObject(Event.class);
                                                    int kk = isEventToday(ee);
                                                    mySa.add(ee);
                                                    if(kk==0 || kk == -2 || ee.isTicketing()) myTi.add(ee);
                                                }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
                Data.loadMyCustomers();
            }
        });
    }

    /**
     * this function load all the events that the user has created (from firebase)
     */
    public static void loadMyEvents() {
        FirebaseFirestore.getInstance().collection("Events").
                whereEqualTo("manageUid",FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        if(document.exists()) {
                            Event ee = document.toObject(Event.class);
                            myEv.add(ee);
                            int kk = isEventToday(ee);
                            mySa.add(ee);
                            if(kk==0 || kk == -2 || ee.isTicketing()) myTi.add(ee);

                            Data.loadSellersForEvent(ee.getEventID()); //load sellers for this event
                            Data.loadAllCustomersForEvent(ee.getEventID()); //load customers for this event
                        }
                    }
                }
                Data.loadMySalesEvents();
            }
        });
    }
    /**
     * this function load all the customers of specific event (from firebase)
     * @param evID which is the id of the event
     */
    public static void loadAllCustomersForEvent(String evID){
        FirebaseFirestore.getInstance().collection("Customers")
                .whereEqualTo("eventId",evID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        if(document.exists()){
                            Customer cus = document.toObject(Customer.class);
                            if(!cus.getSellerUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                myCu.add(cus);
                        }
                    }
                }
            }
        });
    }

    /**
     * this function load all the sellers of specific event (from firebase)
     * @param evID which is the id of the event
     */
    public static void loadSellersForEvent(String evID){
        FirebaseFirestore.getInstance().collection("Sellers").
                whereEqualTo("eventID",evID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        if(document.exists()) mySe.add(document.toObject(Seller.class));
                    }
                }
            }
        });
    }

    /**
     * this function load the details of user (from firebase)
     */
    public static void loadMyDetails() {
        FirebaseFirestore.getInstance().collection("UserDetails")
                .whereEqualTo("userUid", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.exists()) {
                            Data.ud = document.toObject(UserDetails.class);
                            Data.loadMyProfileImg();
                        }
                    }
                }
            }
        });
    }

    /**
     * this function returns array of sellers of specific event
     * @param id which is the id of the event
     */
    public static ArrayList<Seller> getSellersForEvent(String id){
        ArrayList<Seller> listSe = new ArrayList<>();
        for(int i=0; i<mySe.size(); i++){
            if(Data.mySe.get(i).getEventID().equals(id))
                listSe.add(Data.getMySe().get(i));
        }
        return listSe;
    }

    /**
     * this function load the profile image of the user (from firebase)
     */
    public static void loadMyProfileImg(){
        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference ref = mStorageRef.child(ud.getUserUid());
        try {
            final File localFile = File.createTempFile("Images", "bmp");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Data.profileImg = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this function search all the customers with the given barcode
     * the function loads all the events that the customer is participating
     * @param context1 the target activity after the load
     * @param ticketBarcode the barcode of the customer
     */
    public static void searchTicket(final Context context1, final String ticketBarcode){
        pd = new ProgressDialog(context1);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();

        customersLstSearch = new ArrayList<>();
        eventLstSearch = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Customers").whereEqualTo("barcode",ticketBarcode)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.exists()) {
                            Customer cus = document.toObject(Customer.class);
                            Data.customersLstSearch.add(cus);
                        }
                    }
                    if(Data.customersLstSearch.size()!=0){
                        for(int i=0; i<Data.customersLstSearch.size(); i++) {
                            boolean a1 =Data.customersLstSearch.get(i).getQuantity() > 0;
                            boolean b1 = (i+1) == Data.customersLstSearch.size();
                            loadSpecificEvent(Data.customersLstSearch.get(i).getEventId(), a1, b1, context1);
                        }

                        SharedPreferences sp = context1.getSharedPreferences("customer",0);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("qr",ticketBarcode);
                        editor.commit();
                    }
                    else {
                        Toast.makeText(context1, "The ticket not found", Toast.LENGTH_LONG).show();
                        context1.startActivity(new Intent(context1, MainActivity.class));
                    }
                }
            }
        });
    }

    /**
     * this function search all the customers with the given barcode
     * the function loads specific event to the array by the id of the event
     * @param validated will be replaced sale active just for presentation the relevant details for the customer
     * @param last if the load has finished
     * @param context1 the target activity after the load
     *
     */
    public static void loadSpecificEvent(String evId, final boolean validated, final boolean last, final Context context1){
        FirebaseFirestore.getInstance().collection("Events").whereEqualTo("eventID",evId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.exists()) {
                            Event event = document.toObject(Event.class);
                            event.setSaleActive(validated);
                            Data.eventLstSearch.add(event);
                        }
                    }
                }
                if(last){
                    context1.startActivity(new Intent(context1, Customers.class));
                    pd.dismiss();
                }
            }
        });
    }
}
