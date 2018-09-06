package com.howl.howlstagram

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.howl.howlstagram.model.AlarmDTO
import com.howl.howlstagram.model.ContentDTO
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class CommentActivity : AppCompatActivity() {

    var contentUid : String? = null
    var user : FirebaseAuth? = null
    var destinationUid : String? = null
    var fcmPush : FcmPush? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        user = FirebaseAuth.getInstance()
        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")
        fcmPush = FcmPush()
        comment_recyclerview.adapter = CommentRecylcerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)
        comment_btn_send.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser!!.email
            comment.comment = comment_edit_message.text.toString()
            comment.uid = FirebaseAuth.getInstance().currentUser!!.uid
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            commentAlarm(destinationUid!!,comment_edit_message.text.toString())
            comment_edit_message.setText("")
        }

    }

    fun commentAlarm(destinationUid: String,message: String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid =  destinationUid
        alarmDTO.userId = user?.currentUser?.email
        alarmDTO.uid = user?.currentUser?.uid
        alarmDTO.kind = 1
        alarmDTO.message = message
        alarmDTO.timestamp = System.currentTimeMillis()

        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var message = user?.currentUser?.email + getString(R.string.alarm_who) + message + getString(R.string.alarm_comment)
        fcmPush?.sendMessage(destinationUid,"알림 메세지 입니다.",message)
    }

    inner class CommentRecylcerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val comments :ArrayList<ContentDTO.Comment>
        init {
            comments = ArrayList()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                comments.clear()
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot.documents!!){
                    comments.add(snapshot.toObject(ContentDTO.Comment::class.java))
                }
                notifyDataSetChanged()

            }

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var view = holder.itemView
            view.commentviewitem_textview_comment.text = comments[position].comment
            view.commentviewitem_textview_profile.text = comments[position].userId

            FirebaseFirestore.getInstance().collection("profileImages")?.document(comments[position].uid!!)?.get()?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    var url = task.result["image"]
                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewItem_imageview_profile)
                }
            }


        }

    }
}
