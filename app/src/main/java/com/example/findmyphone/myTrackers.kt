package com.example.findmyphone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.LayoutInflater
import android.provider.ContactsContract
import android.service.autofill.UserData
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_my_trackers.*
import kotlinx.android.synthetic.main.contact_ticket.view.*

class myTrackers : AppCompatActivity() {
    var adapter:myTrackers.contactAdapter?=null
    var contactList= ArrayList<UserContact>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_trackers)

//        contactList.add(UserContact("Ashish","2364872"))
//        contactList.add(UserContact("Chadnan","3964712"))
//        contactList.add(UserContact("Chakra","82438922"))
        adapter= contactAdapter(this,contactList)
        list.adapter=adapter
        list.onItemClickListener=AdapterView.OnItemClickListener{
                parent,view,position,id->
                val uInfo=contactList[position]
                userData.myTracker.remove(uInfo.phone)
                refreshData()
            //Used to save in shared preferences
            val userdata=userData(applicationContext)
            userdata.saveContact()

            val myRef = FirebaseDatabase.getInstance().reference
            myRef.child("Users").child(uInfo.phone.toString())
                .child("Finders").child(userdata.loadNumber().toString()).removeValue()
        }
        val uData=userData(this)
        uData.loadContact()
        refreshData()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater=menuInflater
        inflater.inflate(R.menu.trackers,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.AddNewPerson->
            {
                checkpermission()
            }
            R.id.Done ->
            {
                finish()
            }

        }


        return super.onOptionsItemSelected(item)
    }
    val contact_code=123
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
        pickcontact()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            contact_code->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    pickcontact()
                else{
                    Toast.makeText(this,"ACCESS DENIED",Toast.LENGTH_LONG).show()
                }
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    val contactcode=234
    fun pickcontact()
    {
        val intent=Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent,contactcode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode)
        {
            contactcode->
            {
                if (resultCode==Activity.RESULT_OK)
                {
                    val contactdata=data!!.data
                    val c= contactdata?.let { contentResolver.query(it,null,null,null,null) }

                    if(c!!.moveToFirst())
                    {
                        val id=c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val hasPhone=c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        if(hasPhone.equals("1"))
                        {
                            val phones=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,null,null)

                            phones!!.moveToFirst()
                            var phonenum=phones!!.getString(phones!!.getColumnIndex("data1"))
                            val name=c!!.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            phonenum=userData.formatnum(phonenum)
                            userData.myTracker.put(phonenum,name)
                            refreshData()
                            //Saving to shared preferences.
                            var userdata=userData(this)
                            userdata.saveContact()

                            val myRef = FirebaseDatabase.getInstance().reference
                            myRef.child("Users").child(phonenum)
                                .child("Finders").child(userdata.loadNumber().toString()).setValue(true)
                        }


                    }
                }
            }
        }


        super.onActivityResult(requestCode, resultCode, data)
    }
    fun refreshData()
    {
        contactList.clear()
        for((key,value) in userData.myTracker)
        {
            contactList.add(UserContact(value,key))
        }
            adapter!!.notifyDataSetChanged()
    }

     class contactAdapter:BaseAdapter
     {
         var context:Context?=null
         var contactList=ArrayList<UserContact>()
         constructor(context:Context,contactList:ArrayList<UserContact>)
         {
             this.context=context
             this.contactList=contactList

         }
         override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
             val userlists=contactList[p0]
             var inflater=context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
             val myView=inflater.inflate(R.layout.contact_ticket,null)
             myView.nametv.text=userlists.name
             myView.phonetv.text=userlists.phone

             return myView
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

}