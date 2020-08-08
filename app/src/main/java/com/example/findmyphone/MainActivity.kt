package com.example.findmyphone

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_ticket.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    var adapter:contactAdapter?=null
    var myRef:DatabaseReference?=null
    var contactList= ArrayList<UserContact>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userinfo=userData(this)
        userinfo.loadFirst()
        myRef = FirebaseDatabase.getInstance().reference

//        contactList.add(UserContact("Ashish","2364872"))
//        contactList.add(UserContact("Chadnan","3964712"))
//        contactList.add(UserContact("Chakra","82438922"))


        adapter= contactAdapter(this, contactList)
        list.adapter=adapter

        list.onItemClickListener=AdapterView.OnItemClickListener { parent, view, pos, id ->
            val df=SimpleDateFormat("yyyy/MM/dd hh:MM:ss" )
            val date=Date()
            val userinfo=contactList[pos]
            val myRef = FirebaseDatabase.getInstance().reference
            myRef.child("Users").child(userinfo.phone.toString())
                .child("request").setValue(df.format(date).toString())
            val intent= Intent(applicationContext,MapsActivity::class.java)
            intent.putExtra("PhoneNumber",userinfo.phone)
            startActivity(intent)

        }


        //refreshUsers()

    }
   // var IsAccessLoc=false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val udata=userData(this)
        if(udata.loadNumber()=="empty")
            return
        refreshUsers()
        if(MyService.isServiceRunning)
            return
        checkpermission()
        checkLocapermission()
    }

    fun refreshUsers(){
        val userData= userData(this)
        myRef!!.child("Users").
        child(userData.loadNumber().toString()).
        child("Finders").addValueEventListener(object :
            ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val td = dataSnapshot!!.value as HashMap<String,Any>

                    contactList.clear()

                    if (td==null){
                        contactList.add(UserContact("NO_USERS","nothing"))
                        adapter!!.notifyDataSetChanged()
                        return
                    }

                    for (key in td.keys){
                        val name=listContacts[key]
                        contactList.add(UserContact(name.toString(),key))

                    }

                    adapter!!.notifyDataSetChanged()
                }catch (ex:Exception){
                    contactList.clear()
                    contactList.add(UserContact("NO_USERS","nothing"))
                    adapter!!.notifyDataSetChanged()
                    return
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater=menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.addtracker->
            {
                val intent=Intent(this,myTrackers::class.java)
                startActivity(intent)
            }
            R.id.help->
            {
                //TODO()
            }

        }


        return super.onOptionsItemSelected(item)
    }
    class contactAdapter: BaseAdapter
    {
        var context: Context?=null
        var contactList=ArrayList<UserContact>()
        constructor(context: Context, contactList:ArrayList<UserContact>)
        {
            this.context=context
            this.contactList=contactList

        }
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val userlists=contactList[p0]
            if (userlists.name.equals("NO_USERS"))
            {
                var inflater=context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val myView=inflater.inflate(R.layout.no_user,null)
                return myView
            }
            else
            {
                var inflater=context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val myView=inflater.inflate(R.layout.contact_ticket,null)
                myView.nametv.text=userlists.name
                myView.phonetv.text=userlists.phone
                return myView
            }
        }

        override fun getItem(p0: Int): Any {
            return contactList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return contactList.size
        }

    }
    val contact_code=123
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkpermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),contact_code)
                return
            }

        }
        loadcontact()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            contact_code->{
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    loadcontact()
                else{
                    Toast.makeText(this,"ACCESS DENIED", Toast.LENGTH_LONG).show()
                }
            }
            locCode->
            {
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    getUserLoc()
                else{
                    Toast.makeText(this,"ACCESS DENIED", Toast.LENGTH_LONG).show()
                }
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    val listContacts=HashMap<String,String>()
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadcontact()
    {
        listContacts.clear()
        //Cursor will have all the contacts in the phone only.
        val  cursor=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null)
        cursor!!.moveToFirst()
        do{
            val name=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val num= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            listContacts.put(name,num)


        }while (cursor.moveToNext())
    }
    val locCode=2345
    fun checkLocapermission() {
         //IsAccessLoc=true
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),locCode)
                return
            }
            getUserLoc()

        }

    }

    fun getUserLoc()
    {

        if(!MyService.isServiceRunning){
            val intent= Intent(baseContext,MyService::class.java)
            startService(intent)
        }

    }

}