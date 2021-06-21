package com.example.connectus

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.connectus.daos.UserDao
import com.example.connectus.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.rpc.context.AttributeContext
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 123

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val TAG = "SignInActivity Tag"

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        // Set the onclick listener on the google sign in button
        SignIn.setOnClickListener{
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent =mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Start the sign in process after the sign in with google button is pressed
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    // Method to handle the sign in task
    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try{

            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        }
        catch (e:ApiException){
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        // Get the credentials associated with that account using the idToken
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        //Set the visibility of progress bar and SignIn Button
        SignIn.visibility = View.GONE
        ProgressBar.visibility = View.VISIBLE


        // To run signInWithCredential Method on background thread.
        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user

            //To switch from IO thread to Main Thread because UI can only be updated from Main Thread
            withContext(Dispatchers.Main){
                updateUI(firebaseUser)
            }
        }
    }

    // Method to update the UI
    private fun updateUI(firebaseUser: FirebaseUser?) {

        // If firebaseUser is not null then take that user to the main activity
        if(firebaseUser != null) {

            //Create a User Object
            val user = User(firebaseUser.uid, firebaseUser.displayName.toString(), firebaseUser.photoUrl.toString())

            //Add the user to the database
            val userDao = UserDao()
            userDao.addUser(user)

            //Navigate to Main Activity
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        }

        // Else if it is null then keep the user on that page and update the UI
        else{
            SignIn.visibility = View.VISIBLE
            ProgressBar.visibility = View.GONE
        }
    }

}