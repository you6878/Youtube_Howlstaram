package com.howl.howlstagram

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.howl.howlstagram.R.id.addphoto_edit_explain
import com.howl.howlstagram.model.ContentDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    val PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        addphoto_image.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                addphoto_image.setImageURI(data?.data)
            } else {
                finish()
            }
        }

    }

    fun contentUpload() {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        storageRef?.putFile(photoUri!!)?.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()
            //업로드된 이미지 주소
            var uri = taskSnapshot.downloadUrl

            var contentDTO = ContentDTO()
            //이미지 주소
            contentDTO.imageUrl = uri!!.toString()

            //유저의 UID
            contentDTO.uid = auth?.currentUser?.uid

            //게시물 설명
            contentDTO.explain = addphoto_edit_explain.text.toString()

            //유저 아이디
            contentDTO.userId = auth?.currentUser?.email

            //게시물 업로드 시간
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()


        }

    }
}
