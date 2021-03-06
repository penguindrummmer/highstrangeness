package com.example.highstrangeness.ui.user_auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.highstrangeness.R;
import com.example.highstrangeness.objects.User;
import com.example.highstrangeness.ui.main.MainActivity;
import com.example.highstrangeness.ui.user_auth.add_profile_picture.AddProfilePictureFragment;
import com.example.highstrangeness.ui.user_auth.login.LoginFragment;
import com.example.highstrangeness.ui.user_auth.reset_password.ResetPasswordActivity;
import com.example.highstrangeness.ui.user_auth.sign_up.SignUpFragment;
import com.example.highstrangeness.utilities.NetworkUtility;
import com.example.highstrangeness.utilities.PermissionsUtility;
import com.example.highstrangeness.utilities.UserAuthUtility;
import com.google.firebase.auth.FirebaseAuth;

public class UserAuthActivity extends AppCompatActivity implements
        UserAuthUtility.GetUserAuthActivityContextListener, LoginFragment.LoginListener,
        LoginFragment.DisplaySignUpFragmentListener, SignUpFragment.SignUpListener,
        SignUpFragment.DisplayLoginFragmentListener,
        UserAuthUtility.CheckWhetherAUserIsLoggedInListener,
        LoginFragment.NavigateToResetPasswordActivity,
        AddProfilePictureFragment.NavigateToMainScreenListener,
        UserAuthUtility.DisplayAddProfilePictureFragmentListener {

    public static final String TAG = "UserAuthActivity";
    public static final int REQUEST_CODE_LOGGED_OUT = 0x068;


    @Override
    public void navigateToMainScreen() {
        navigateToMainActivity();
    }

    @Override
    public void checkWhetherUserIsLoggedIn() {
        checkIfUserIsAlreadySignedIn();
    }

    @Override
    public void signUp(String email, String username, String password) {
        if (NetworkUtility.CheckNetworkConnection(this)) {
            userAuthUtility.signUp(email, username, password);
        };
    }

    @Override
    public void displayLogIn() {
        displayLogInFragment();
    }

    @Override
    public void displaySignUp() {
        displaySignUpFragment();
    }

    @Override
    public void login(String email, String password) {
        if (NetworkUtility.CheckNetworkConnection(this)) {
            userAuthUtility.login(email, password);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }


    private UserAuthUtility userAuthUtility;
    int requestCode = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) {
            Context context;
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.setTitle("Permissions");
            alertDialog.setMessage("High Strangeness requires permissions for Location, Internet, and File Access");
            alertDialog.show();
        }
    }

    private void checkPermissions() {
        requestCode += 1;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: request");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA},
                    requestCode);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_auth);
        checkPermissions();
        ;
        userAuthUtility  = new UserAuthUtility(this);
        userAuthUtility.checkForCurrentUser();
        checkIfUserIsAlreadySignedIn();
    }

    public void checkIfUserIsAlreadySignedIn() {
        if (User.currentUser != null) {
            Log.d(TAG, "checkIfUserIsAlreadySignedIn: true");
            navigateToMainActivity();
        }else {
            Log.d(TAG, "checkIfUserIsAlreadySignedIn: false");
            displayLogInFragment();
        }
    }

    public void displayAddProfilePictureFragment() {
        Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutUserAuth, new AddProfilePictureFragment()).commit();
    }

    public void displayLogInFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutUserAuth, LoginFragment.newInstance()).commit();
    }

    public void displaySignUpFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutUserAuth, SignUpFragment.newInstance()).commit();
    }

    public void navigateToResetPasswordActivity() {
        Intent intent = new Intent(UserAuthActivity.this, ResetPasswordActivity.class);
        UserAuthActivity.this.startActivity(intent);
    }

    public void navigateToMainActivity() {
        Intent intent = new Intent(UserAuthActivity.this, MainActivity.class);
        UserAuthActivity.this.startActivityForResult(intent, REQUEST_CODE_LOGGED_OUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGGED_OUT) {
            userAuthUtility.checkForCurrentUser();
            checkIfUserIsAlreadySignedIn();
        }
    }
}