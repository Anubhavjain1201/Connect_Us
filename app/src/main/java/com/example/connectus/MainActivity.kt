package com.example.connectus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.connectus.Adapter.IPostAdapter
import com.example.connectus.Adapter.PostAdapter
import com.example.connectus.daos.PostDao
import com.example.connectus.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), IPostAdapter {

    private lateinit var adapter: PostAdapter
    private lateinit var postdao: PostDao
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        fab.setOnClickListener {

            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerViewAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        signOut()
        return true
    }

    private fun signOut() {

        //Sign out from firebase
        Firebase.auth.signOut()     // --> This will only sign the user out from firebase and not from google
                                    //     so the next time the user signs in, it will not show the account chooser dialog box
                                    //     because we haven't signed out from google.

        // So sign out from google as well

        //sign out from google account
        GlobalScope.launch(Dispatchers.IO) {
            googleSignInClient.signOut().await()

            withContext(Dispatchers.Main){
                updateUI()
            }
        }
    }

    private fun updateUI() {

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupRecyclerViewAdapter() {

        postdao = PostDao()

        val postsCollection = postdao.postCollection
        val query = postsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val firestoreRecyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        adapter = PostAdapter(firestoreRecyclerViewOptions, this)
        PostsList.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postdao.updateLikes(postId)
    }
}