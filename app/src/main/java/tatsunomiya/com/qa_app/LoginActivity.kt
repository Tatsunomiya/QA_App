package tatsunomiya.com.qa_app

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import tatsunomiya.com.qa_app.Const.Companion.NameKey
import tatsunomiya.com.qa_app.Const.Companion.UsersPath


class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDatabaseReference: DatabaseReference


    private var mIsCreateAccount = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mAuth = FirebaseAuth.getInstance()

        mCreateAccountListener = OnCompleteListener { task ->

            if (task.isSuccessful) {
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email, password)
            } else {
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show()

                progressBar.visibility = View.GONE
            }
        }

        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) {

                val user = mAuth.currentUser
                val userRef = mDatabaseReference.child(UsersPath).child(user!!.uid)


                if (mIsCreateAccount) {
                    val name = nameText.text.toString()

                    val data = HashMap<String, String>()
                    data["name"] = name
                    userRef.setValue(data)


                    saveName(name)

                } else {
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            saveName(data!!["name"] as String)
                        }


                        override fun onCancelled(firebaseError: DatabaseError) {}

                    })
                }

                progressBar.visibility = View.GONE

                finish()
            } else {
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()


                progressBar.visibility = View.GONE


            }

        }







        title = "ログイン"




        createButton.setOnClickListener { v ->

            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)


            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()



            if (email.length != 0 && password.length >= 6 && name.length != 0) {

                mIsCreateAccount = true


                createAccount(email, password)

            } else {
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()


            }

        }



        loginButton.setOnClickListener { v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)


            val email = emailText.text.toString()
            val password = passwordText.text.toString()



            if (email.length != 0 && password.length >= 6) {

                mIsCreateAccount = false


                login(email, password)

            } else {
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }

        }

    }

            private fun createAcount(email: String, password: String){


            progressBar.visibility = View.VISIBLE

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
        }


        private fun saveName(name: String) {

            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sp.edit()
            editor.putString(NameKey, name)

            editor.commit()


        }

    private fun login(email:String, password: String) {

        progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(mLoginListener)
    }

    private fun createAccount(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)
    }

    }

