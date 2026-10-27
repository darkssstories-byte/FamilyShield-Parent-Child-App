package com.familyshield.parent

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

/** Safe wrapper: the app still launches when google-services.json is missing. */
object Backend {
    @Volatile
    var configured: Boolean = false
        private set

    fun init(context: Context) {
        configured = try {
            FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
        } catch (e: Exception) {
            false
        }
    }

    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    val functions: FirebaseFunctions get() = FirebaseFunctions.getInstance()
}
