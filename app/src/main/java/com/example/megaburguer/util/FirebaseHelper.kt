package com.example.megaburguer.util

import com.example.megaburguer.R
import com.google.firebase.auth.FirebaseAuth

class FirebaseHelper {

    companion object {

        fun getAuth() = FirebaseAuth.getInstance()

        fun isAuthenticated() = getAuth().currentUser != null

        fun getUserId() = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    }


}