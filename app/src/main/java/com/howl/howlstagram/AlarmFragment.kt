package com.howl.howlstagram

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.howl.howlstagram.model.AlarmDTO
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment() {

    var alarmSnaphot: ListenerRegistration? = null
    var recylcerview: RecyclerView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_alarm, container, false)
         recylcerview = view.findViewById<RecyclerView>(R.id.alarmfragment_recyclerview)


        return view
    }

    override fun onResume() {
        super.onResume()
        recylcerview?.adapter = AlarmRecyclerViewAdapter()
        recylcerview?.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStop() {
        super.onStop()
        alarmSnaphot?.remove()

        //스냅샷을 사용할때는
        // - ListenerRegistration at Resume at Stop
        // - Null 맨상단
    }

    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val alarmDTOlist = ArrayList<AlarmDTO>()

        init {
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            alarmSnaphot = FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener
                alarmDTOlist.clear()
                for (snapshot in querySnapshot.documents!!) {
                    alarmDTOlist.add(snapshot.toObject(AlarmDTO::class.java))

                }
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomVieHolder(view)
        }

        inner class CustomVieHolder(view: View?) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOlist.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val profileImage = holder.itemView.commentviewItem_imageview_profile
            val commentTextview = holder.itemView.commentviewitem_textview_profile


            FirebaseFirestore.getInstance().collection("profileImages")?.document(alarmDTOlist[position].uid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var url = task.result["image"]
                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(profileImage)
                }
            }

            when (alarmDTOlist[position].kind) {

                0 -> {
                    val str_0 = alarmDTOlist[position].userId + getString(R.string.alarm_favorite)
                    commentTextview.text = str_0
                }
                1 -> {
                    val str_1 = alarmDTOlist[position].userId +
                            getString(R.string.alarm_who) + "\"" +
                            alarmDTOlist[position].message + "\"" +
                            getString(R.string.alarm_comment)
                    commentTextview.text = str_1
                }
                2 -> {
                    val str_2 = alarmDTOlist[position].userId + getString(R.string.alarm_follow)
                    commentTextview.text = str_2

                }

            }


        }

    }
}