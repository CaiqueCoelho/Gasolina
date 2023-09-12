package caiquecoelho.com.gasolina

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class Login : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var RC_SIGN_IN = 2
    private lateinit var btnLogin: SignInButton
    private lateinit var btnGuest: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        val account = GoogleSignIn.getLastSignedInAccount(this)

        account?.id?.let {
            Log.i("Login-Auth", "user logged in")
            Log.i("Login-Auth", account.id!!)
            val intentMain = Intent(this@Login, MainActivity::class.java)
            intentMain.putExtra("user_id", account.id)
            intentMain.putExtra("user_name", account.displayName)
            intentMain.putExtra("user_email", account.email)
            intentMain.putExtra("user_photo", account.photoUrl)
            this.finish()
            startActivity(intentMain)
        }

        if(account == null || account.id == null){
            Log.i("Login-Auth", "user not logged")
        }

        btnLogin = findViewById(R.id.sign_in_button)

        btnLogin.setOnClickListener(View.OnClickListener {
            signIn()
        })

        btnGuest = findViewById(R.id.guest)
        btnGuest.setOnClickListener(View.OnClickListener {
            val intentMain = Intent(this@Login, MainActivity::class.java)
            startActivity(intentMain)
        })
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            //updateUI(account)
            Log.i("Login-Auth", account.id.toString())
            val intentMain = Intent(this@Login, MainActivity::class.java)
            intentMain.putExtra("user_id", account.id)
            intentMain.putExtra("user_name", account.displayName)
            intentMain.putExtra("user_email", account.email)
            intentMain.putExtra("user_photo", account.photoUrl)
            this.finish()
            startActivity(intentMain)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Login-Auth", "signInResult:failed code=" + e.statusCode)
            //updateUI(null)
        }
    }
}