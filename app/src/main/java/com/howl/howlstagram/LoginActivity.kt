package com.howl.howlstagram

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*
import android.provider.SyncStateContract.Helpers.update
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.support.v4.app.FragmentActivity
import android.util.Base64
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null


    var googleSignInClient: GoogleSignInClient? = null

    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_button.setOnClickListener {
            createAndLoginEmail()
        }

        google_sign_in_button.setOnClickListener {
            googleLogin()
        }

        facebook_login_button.setOnClickListener {
            facebookLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        printHashKey(this)
        callbackManager = CallbackManager.Factory.create()

    }

    fun printHashKey(pContext: Context) {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("Howl", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("Howl", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("Howl", "printHashKey()", e)
        }

    }

    fun createAndLoginEmail() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        moveMainPage(auth?.currentUser)
                    } else if (task.exception?.message.isNullOrEmpty()) {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    } else {
                        signinEmail()
                    }
                }

    }

    fun signinEmail() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        moveMainPage(auth?.currentUser)
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }

                }

    }

    fun moveMainPage(user: FirebaseUser?) {

        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun googleLogin() {

        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)

    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {

            task ->
            if(task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }
        }

    }

    fun facebookLogin() {
        LoginManager
                .getInstance()
                .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager
                .getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        handleFacebookAccessToken(result?.accessToken)
                    }

                    override fun onCancel() {

                    }

                    override fun onError(error: FacebookException?) {

                    }

                })

    }

    fun handleFacebookAccessToken(token: AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }

        }

    }

    override fun onResume() {
        super.onResume()
        moveMainPage(auth?.currentUser)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            }
        }

    }
}
