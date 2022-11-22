package com.example.android_sns_project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_sns_project.data.Content
import com.example.android_sns_project.databinding.ContentItemBinding
import com.example.android_sns_project.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class UserFragment : Fragment() {
    var photoUri : Uri? = null
    var photoBitmap: Bitmap? = null
    val db = Firebase.firestore
    val rootRef = Firebase.storage.reference
    private var adapter: UserFragmentAdapter? = null
    private val itemsCollectionRef = db.collection("content")
    private var binding: FragmentUserBinding? = null
    var items = mutableListOf<Item>()
    var uid:String? = null
    var auth : FirebaseAuth? = null

    val database = Firebase.database
    var roofRef2 = Firebase.database.reference


    //실시간 변경 데이터 추적
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        // recyclerview setup
        binding!!.recyclerView.layoutManager = GridLayoutManager(activity,3)
        adapter = UserFragmentAdapter()

        Log.d("User",auth?.currentUser?.email.toString() )
        roofRef2.child("Users").orderByChild("email").
        equalTo(auth?.currentUser?.email).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //snapshot에 쿼리문에 맞는 Users 배열이 들어옴
                for (snapshot in dataSnapshot.children) {
                    Log.d("User", "ValueEventListener : " + snapshot.value)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
//            Log.d("User",it.value.toString() )
//            binding!!.followerCount.text= it.value.toString()
//        }.addOnFailureListener {
//            Log.d("User","Filed" )
//            // ...
//        }
//        // 데이터베이스 읽기 #1. ValueEventListener
//        FirebaseDatabase.getInstance().reference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (snapshot in dataSnapshot.children) {
//                    Log.d("MainActivity", "ValueEventListener : " + snapshot.value)
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {}
//        })
        //프로필 사진 바꾸는 이벤트
        binding!!.accountProfile.setOnClickListener{
            var photoIntent = Intent(Intent.ACTION_PICK)
            photoIntent.type = "image/*"
            startActivityForResult(photoIntent, 0)
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PostingActivity.REQUEST_CODE
            )
        }
//        adapter?.setOnItemClickListener {
//            //updateList()
//        }
//        binding!!.accountBtnFollow.setOnClickListener{
//            updateList()
//        }


        binding!!.recyclerView.adapter = adapter
       // updateList()
        return binding?.root
    }

    //갤러리에서 돌아올 때
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      //  super.onActivityResult(requestCode)
        Log.d("IMAGETEST", requestCode.toString())
        if(resultCode == Activity.RESULT_OK){
            Log.d("IMAGETEST", "사진 클릭하면 사진 바뀌기ㅅ")
            photoUri = data?.data
            binding!!.accountProfile.setImageURI(photoUri)
//        if(requestCode == 0){
//
//            }else{
//              //  finish()
//            }
       }
    }

    inner class MyViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    inner class UserFragmentAdapter : RecyclerView.Adapter<MyViewHolder>(){
        private val itemsCollectionRef = db.collection("content")
        var contents : ArrayList<Content> = arrayListOf()
        var items : ArrayList<Item> = arrayListOf()

        init {
            //content collect에 접근
            itemsCollectionRef?.whereEqualTo("uid",auth?.currentUser?.uid)
                ?.addSnapshotListener { snapshot, error ->
                    // var items = mutableListOf<Item>()
                    if(snapshot == null) return@addSnapshotListener

                    //      CoroutineScope(Dispatchers.Main).launch {
                    for(snapshot in snapshot.documents) {
                        contents.add(snapshot.toObject(Content::class.java)!!)
                        Log.d("TAG","전 ${contents.size}")

                        notifyDataSetChanged()
                        Log.d("TAG","후 ${contents.size}")
                    }

                    //   }
                    //adapter?.updateList(items)
                }

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding =ContentItemBinding.inflate(inflater, parent, false)
            var width = resources.displayMetrics.widthPixels/3
            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            /*경미 추가 부분 */
            imageView.setOnClickListener{
                Log.d("TAG","클릭 ${contents.size}")
            }
            /*경미 추가 부분 */
            return MyViewHolder(imageView)
            Log.d("TAG","onCreateViewHolder ${contents.size}")
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if(contents.isEmpty()) return
            var imageView =(holder as MyViewHolder).imageView
            Log.d("TAG","onBindViewHolder ${contents.size}")
          //  for (content in contents) {
                val ref = rootRef.child(contents[position].imagePath.toString())

                ref.getBytes(Long.MAX_VALUE).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result!!.size)
                        imageView.setImageBitmap(bmp)
                        //imgView?.setImageBitmap(bmp)
                       // items?.add(Item(content, bmp))

                    }
                }
          //  }
          //  val item = items[position]
            //imageView.setImageBitmap(item.bmp)
        }

        override fun getItemCount(): Int {
            return contents.size
        }
    }
    private fun updateList() {
//        //content collect에 접근
//        itemsCollectionRef.get().addOnSuccessListener {
//           // var items = mutableListOf<Item>()
//            for (doc in it) {
//                val ref = rootRef.child(doc["imagePath"].toString())
//
//                ref.getBytes(Long.MAX_VALUE).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result!!.size)
//                        //imgView?.setImageBitmap(bmp)
//                        items.add(Item(doc, bmp))
//
//                    }
//                }
//            }
//            adapter?.updateList(items)
//        }
    }


//    private fun getImage(path:String){
//        val token = path.split("gs://android-sns-youu.appspot.com/")
//        val ref = rootRef.child(token[1])
//
//        ref.getBytes(Long.MAX_VALUE).addOnCompleteListener {
//            if (it.isSuccessful) {
//                val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result!!.size)
//                val contentImage: ImageView = customLayout.findViewById<ImageView>(R.id.contentImage)
//                contentImage.setImageBitmap(bmp)
//            }
//        }
//    }
override fun onRequestPermissionsResult(requestCode: Int,
                                        permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PostingActivity.REQUEST_CODE) {
        if ((grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

        }
    }
}
}