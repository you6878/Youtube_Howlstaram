package com.howl.howlstagram

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.howl.howlstagram.model.ContentDTO
import com.howl.howlstagram.model.FollowDTO
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {
    var fragmentView: View? = null
    var PICK_PROFILE_FROM_ALBUM = 10
    var firestore: FirebaseFirestore? = null
    //현재 나의 uid
    var currentUserUid: String? = null
    //내가 선택한 uid
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if(arguments != null){

            uid = arguments!!.getString("destinationUid")

        }



        firestore = FirebaseFirestore.getInstance()
        fragmentView = inflater.inflate(R.layout.fragment_user, container, false)
        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPcikerIntent = Intent(Intent.ACTION_PICK)
            photoPcikerIntent.type = "image/*"
            activity?.startActivityForResult(photoPcikerIntent, PICK_PROFILE_FROM_ALBUM)

        }
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecycldrViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!,3)
        getProfileImages()

        fragmentView?.account_btn_follow_signout?.setOnClickListener {
            requestFollow()
        }
        return fragmentView
    }

    fun requestFollow(){
        var tsDocFollowing = firestore!!.collection("users").document(currentUserUid!!)

        firestore?.runTransaction {
            transaction ->
            var followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)
            if(followDTO == null){
                //아무도 팔로잉 하지 않았을 경우
                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO.followings[uid!!] = true

                transaction.set(tsDocFollowing,followDTO)
                return@runTransaction
            }

            if(followDTO.followings.containsKey(uid)){
                //내 아이디가 제3를 이미 팔로잉 하고 있을 경우 -> 제3자가 나를 팔로워 취소한다.
                followDTO?.followingCount = followDTO?.followingCount -1
                followDTO?.followings.remove(uid)
            }else{
                //내가 제3를 팔로잉 하지 않았을 경우 -> 제3자가 나를 팔로워 한다.
                followDTO.followingCount = followDTO.followingCount  + 1
                followDTO.followings[uid!!] = true
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore!!.collection("users").document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)

            if(followDTO == null){
                //아무도 팔로워 하지 않았을 경우
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true

                transaction.set(tsDocFollower,followDTO)
                return@runTransaction

            }

            if(followDTO.followers.containsKey(currentUserUid!!)){
                //제3자의 유저를 내가 팔로잉 하고 있을 경우 -> 팔로워 취소 하겠다.

                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)



            }else{
                //제3자를 내가 팔로워 하지 않았을 경우 -> 팔로워 하겠다.

                followDTO.followerCount = followDTO.followerCount + 1
                followDTO.followers[currentUserUid!!]  = true


            }
            transaction.set(tsDocFollower,followDTO)
            return@runTransaction



        }



    }

    fun getProfileImages() {
        firestore?.collection("profileImages")?.document(currentUserUid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot.data != null) {
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity).load(url).apply(RequestOptions().circleCrop()).into(fragmentView!!.account_iv_profile)
            }

        }

    }

    inner class UserFragmentRecycldrViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO>

        init {
            contentDTOs = ArrayList()
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                for (snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java))
                }
                account_tv_post_count.text = contentDTOs.size.toString()
                notifyDataSetChanged()

            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)
        }

       private inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }
    }
}

