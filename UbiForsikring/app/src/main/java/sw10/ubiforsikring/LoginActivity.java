package sw10.ubiforsikring;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    Button mLoginButton;
    SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginButton = (Button) findViewById(R.id.LoginButton);
        mLoginButton.setOnClickListener(LoginListener);
    }

    @Override
    public void onResume() {
        //Skip login if relevant
        SharedPreferences preferences = getSharedPreferences(getString(R.string.LoginPreferences), Context.MODE_PRIVATE);
        if(preferences.getBoolean(getString(R.string.LoginStatus), false)) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        }

        super.onResume();
    }

    Button.OnClickListener LoginListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Login();
        }
    };

    public void Login() {
        //Validate if input is syntactically correct
        if (!ValidateLogin()) {
            return;
        }

        //Disable further use of the Activity while trying to login
        EditText emailEditText = (EditText) findViewById(R.id.EmailEditText);
        EditText passwordEditText = (EditText) findViewById(R.id.PasswordEditText);
        mLoginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.Authenticating));
        progressDialog.show();

        final String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // TODO: Login

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if(1 == 1) {
                            OnLoginSuccess(email);
                        } else {
                            OnLoginFailed();
                        }
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void OnLoginSuccess(String email) {
        //Save the login status
        mPreferences = getSharedPreferences(getString(R.string.LoginPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(getString(R.string.LoginStatus), true);
        editor.putString(getString(R.string.StoredEmail), email);
        editor.apply();

        startActivity(new Intent(this, MainMenuActivity.class));
        finish();
    }

    public void OnLoginFailed() {
        Toast.makeText(this, getString(R.string.AuthenticationFailed), Toast.LENGTH_SHORT).show();

        Button loginButton = (Button) findViewById(R.id.LoginButton);
        loginButton.setEnabled(true);
    }

    public boolean ValidateLogin() {
        boolean valid = true;

        EditText emailEditText = (EditText) findViewById(R.id.EmailEditText);
        EditText passwordEditText = (EditText) findViewById(R.id.PasswordEditText);
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.EmailError));
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        if (password.isEmpty() || password.length() < 8 || password.length() > 32) {
            passwordEditText.setError(getString(R.string.PasswordError));
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }
}