package com.example.highstrangeness.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.highstrangeness.objects.User;
import com.example.highstrangeness.ui.user_auth.login.LoginFragment;
import com.example.highstrangeness.ui.user_auth.sign_up.SignUpFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserAuthUtility {
    public static final String TAG = "UserAuthUtility";
    public static final String SIGN_UP_INTENT_EXTRA = "SIGN UP EXTRA";
    public static final String LOG_IN_INTENT_EXTRA = "LOG IN EXTRA";
    private static String message;

    public UserAuthUtility(AppCompatActivity UserAuthActivity) {
        this.getAuthActivityContext = (GetUserAuthActivityContextListener) UserAuthActivity;
        this.checkWhetherAUserIsLoggedInListener = (CheckWhetherAUserIsLoggedInListener) UserAuthActivity;
        this.displayAddProfilePictureFragmentListener = (DisplayAddProfilePictureFragmentListener) UserAuthActivity;
    }

    public interface GetUserAuthActivityContextListener {
        public Context getContext();
    }
    public interface CheckWhetherAUserIsLoggedInListener {
        public void checkWhetherUserIsLoggedIn();
    }
    public interface DisplayAddProfilePictureFragmentListener {
        void displayAddProfilePictureFragment();
    }

    private static FirebaseUser user;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    DisplayAddProfilePictureFragmentListener displayAddProfilePictureFragmentListener;
    CheckWhetherAUserIsLoggedInListener checkWhetherAUserIsLoggedInListener;
    GetUserAuthActivityContextListener getAuthActivityContext;

    public void checkForCurrentUser() {
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            setCurrentUser(user);
            user = null;
        }
    }

    //login to firebase
    public void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener((Activity) getAuthActivityContext.getContext(), new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signIn: succeeded");
                        user = firebaseAuth.getCurrentUser();
                        setCurrentUser(user);
                        checkWhetherAUserIsLoggedInListener.checkWhetherUserIsLoggedIn();
                    }
                })
                .addOnFailureListener((Activity) getAuthActivityContext.getContext(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Sign in failed
                        Log.d(TAG, "signIn: failed " + e.getMessage());
                        Toast.makeText(getAuthActivityContext.getContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        user = null;

                        message = null;
                        message = e.getMessage();
                        Intent intent = new Intent(LoginFragment.INTENT_FILTER);
                        intent.putExtra(LOG_IN_INTENT_EXTRA, message);
                        getAuthActivityContext.getContext().sendBroadcast(intent);
                    }
                });
    }

    //sign up to firebase
    public void signUp(final String email, final String username, final String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener((Activity) getAuthActivityContext.getContext(), new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Sign up succeeded
                        Log.d(TAG, "signUp: succeeded");

                        user = authResult.getUser();
                        firebaseAuth.updateCurrentUser(user);

                        //set username
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();

                        if (user != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> docData = new HashMap<>();
                            docData.put("username", username);
                            db.collection("user").document(user.getUid()).set(docData);

                            user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    setCurrentUser(user);
                                    displayAddProfilePictureFragmentListener.displayAddProfilePictureFragment();
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener((Activity) getAuthActivityContext.getContext(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Sign up failed
                        message = null;
                        message = e.getMessage();
                        Intent intent = new Intent(SignUpFragment.INTENT_FILTER);
                        intent.putExtra(SIGN_UP_INTENT_EXTRA, message);
                        getAuthActivityContext.getContext().sendBroadcast(intent);

                        Log.d(TAG, "signUp: " + message);
                        user = null;
                    }
                });
    }

    public void setCurrentUser(FirebaseUser currentUser) {
        if (currentUser != null) {
            String id = currentUser.getUid();
            String email = currentUser.getEmail();
            String username = currentUser.getDisplayName();
            if (email != null && username != null) {
                Log.d(TAG, "setCurrentUser: ");
                Log.d(TAG, email);
                User.currentUser = new User(id, email, username);
                user = null;
            }
        }
    }
}
