package com.example.connectus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.connectus.daos.PostDao
import kotlinx.android.synthetic.main.activity_create_post.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var postDao: PostDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        postDao = PostDao()

        post_button.setOnClickListener {

            val input = input_text.text.toString().trim()

            if(input.isNotEmpty()){
                postDao.addPost(input)
                finish()
            }
        }


    }
}