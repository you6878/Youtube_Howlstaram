package com.howl.howlstagram

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_button.setOnClickListener {
            createAndLoginEmail()
        }


    }

    fun createAndLoginEmail(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
                ?.addOnCompleteListener {
            task->
            if(task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }else if(task.exception?.message.isNullOrEmpty()){
                Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
            }else{
                signinEmail()
            }
        }

    }
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
                ?.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        moveMainPage(auth?.currentUser)
                    }else{
                        Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                    }

        }

    }
    fun moveMainPage(user :FirebaseUser?){

        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}
