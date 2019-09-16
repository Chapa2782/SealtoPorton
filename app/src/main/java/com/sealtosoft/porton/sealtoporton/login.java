package com.sealtosoft.porton.sealtoporton;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class login extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference ref1,ref2;
    EditText mEmail,mPass,mPass2;
    Button btnIniciar,btnRegistrar;
    SignInButton btnSignin;
    GoogleSignInClient mGoogleSignInClient;
    Boolean modo_Inicio = true;
    CallbackManager callbackManager;
    LoginButton fbButton;
    FirebaseAuth.AuthStateListener listener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);

        fbButton = findViewById(R.id.btnFacebook);
        fbButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        callbackManager = CallbackManager.Factory.create();

        fbButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFaceBook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("datas_face",error.toString());
            }
        });
        database = FirebaseDatabase.getInstance();
        ref1 = database.getReference("Dispositivos");
        ref2 = database.getReference("Usuarios");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        updateUI(account);




        btnSignin = findViewById(R.id.btnGoogle);
        mEmail = findViewById(R.id.Email);
        mPass = findViewById(R.id.Pass);
        mPass2 = findViewById(R.id.Pass2);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnIniciar = findViewById(R.id.btnIniciar);


        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modo_Inicio){
                    modo_Inicio = false;
                    mEmail.setText("");
                    mPass.setText("");
                    mPass2.setText("");
                    mPass2.setVisibility(View.VISIBLE);
                }else{
                    String Email = mEmail.getText().toString();
                    String Pass = mPass.getText().toString();
                    String Pass2 = mPass2.getText().toString();
                    if(!Email.isEmpty() && !Pass.isEmpty()){
                        if(Pass.equals(Pass2)){
                            if(Pass.length() > 5){
                                auth.createUserWithEmailAndPassword(Email,Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        modo_Inicio = true;
                                        mEmail.setText("");
                                        mPass.setText("");
                                        mPass2.setText("");
                                        mPass2.setVisibility(View.GONE);
                                        Toast.makeText(login.this,"Se creo el usuario correctamente",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else{
                                mPass.setError("Minimo 6 caracteres");
                            }
                        }else{
                            mPass.setError("No coinciden las contraseñas");
                        }
                    }else{
                        if(Email.isEmpty()){
                            mEmail.setError("Especifique un email");
                        }
                        if(Pass.isEmpty()){
                            mPass.setError("Especifique una contraseña");
                        }
                        if(Pass2.isEmpty()){
                            mPass2.setError("Especifique una contraseña");
                        }
                    }

                }
            }
        });

        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!modo_Inicio){
                    modo_Inicio = true;
                    mEmail.setText("");
                    mPass.setText("");
                    mPass2.setText("");
                    mPass2.setVisibility(View.GONE);
                    return;
                }
                String email = mEmail.getText().toString();
                String pass = mPass.getText().toString();
                auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = auth.getCurrentUser();
                        updateUIEmail(user);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                    }
                });

            }
        });
    }
    private void useLoginInformation(AccessToken accessToken) {
        /**
         Creating the GraphRequest to fetch user details
         1st Param - AccessToken
         2nd Param - Callback (which will be invoked once the request is successful)
         **/
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String name = object.getString("name");
                    String email = object.getString("email");
                    String image = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    Log.d("FaceBookInit",email);
                    Log.d("FaceBookInit",name);
                    Log.d("FaceBookInit",image);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    private void updateUI(GoogleSignInAccount account) {
        if(account == null) return;
        try {
            Log.d("FaceBookInit",account.getEmail());
            Log.d("FaceBookInit",account.getPhotoUrl().toString());
            Log.d("FaceBookInit",account.getDisplayName());

        }catch (Exception e){
            Log.d("FaceBookInit",e.toString());
        }
    }
    private void updateUIEmail(final FirebaseUser account){
        if(account != null) {
            try {
                Log.d("FaceBookInitt", account.getEmail());
                //Log.d("FaceBookInit", account.getPhotoUrl().toString());
                Log.d("FaceBookInitt", account.getDisplayName());

            } catch (Exception e) {
                Log.d("FaceBookInit", "Este Error");
            }
        }else{
            Log.d("FaceBookInit","Sin usuario");
        }
        ref2 = database.getReference("Usuarios/" + account.getUid());
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuariosStructura user = dataSnapshot.getValue(usuariosStructura.class);
                if(user == null){
                    Map<String,usuariosStructura> datos = new HashMap<>();
                    datos.put("usuario",new usuariosStructura(account.getEmail(),account.getDisplayName()));
                    ref2.setValue(datos);

                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void handleFaceBook(final AccessToken token){
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("FaceBookInit",task.getResult().toString());
                        if (task.isSuccessful()) {
                            useLoginInformation(token);
                        } else {

                            Log.w("FaceBookInit", "signInWithCredential:failure", task.getException());
                            Log.d("FaceBookInit","FALLO CREDENCIAL");
                        }

                    }
                });
    }
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            updateUIEmail(user);

                        } else {

                            Log.w("DATAs", "signInWithCredential:failure", task.getException());

                        }

                    }
                });
    }

    private void SignIn() {
        Intent SignInten = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(SignInten, 1111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1111){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);

            updateUI(account);
        } catch (ApiException e) {
            Log.w("DATA", "signInResult:failed code=" + e.getStatusCode());
        }
    }

}
