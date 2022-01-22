package  com.bandwidth.android.ui.login;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.api.rest.RestResponse;
import com.amplifyframework.auth.AuthException;
import com.amplifyframework.auth.AuthSession;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.auth.result.AuthSignInResult;
import com.amplifyframework.auth.result.AuthSignUpResult;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.query.Where;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.DeviceInfo;
import com.amplifyframework.datastore.generated.model.Person;
import com.bandwidth.android.BWLibrary;
import com.bandwidth.android.ListUsersActivity;
import com.bandwidth.android.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    // deviceId is the id from DeviceInfo table (this table is specific to demo app implementation)
    // store it in static variable so it can be used later on when we are calling someone
    public static String deviceId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     binding = ActivityLoginBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final EditText firstnameEditText = binding.firstname;
        final EditText lastnameEditText = binding.lastname;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
//                if (loginFormState.getPasswordError() != null) {
//                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
//                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
//                if (loginResult.getSuccess() != null) {
//                    updateUiWithUser(loginResult.getSuccess());
//                }
//                setResult(Activity.RESULT_OK);

//                //Complete and destroy login activity once successful
//                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        "");
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);

        // FIXME commented out for demo purposes
//        passwordEditText.addTextChangedListener(afterTextChangedListener);
//        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    loginViewModel.login(usernameEditText.getText().toString(),
//                            passwordEditText.getText().toString());
//                }
//                return false;
//            }
//        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String userName = usernameEditText.getText().toString();
                    String firstname = firstnameEditText.getText().toString();
                    String lastname = lastnameEditText.getText().toString();

                    System.out.println("Login attempt for user " + userName + ": " + firstname + " : " + lastname);

                    BWLibrary.configureAmplify(getApplicationContext());

                    signIn(userName, firstname, lastname);

                } catch(Exception e) {
                    System.out.println("Error logging in to cognito: " + e.getMessage());
                }

//                loadingProgressBar.setVisibility(View.VISIBLE);
//                loginViewModel.login(usernameEditText.getText().toString(),
//                        "");
            }
        });
    }

    private void signIn(String userName, String firstname, String lastname) {
        // FIXME hardcoded password for demo
        Amplify.Auth.signIn(
                userName,
                "raleigh123",
                result -> loginSuccess(result, firstname, lastname),
                error -> loginFail(error, userName, firstname, lastname)
        );
    }

    private void loginSuccess(AuthSignInResult result, String fname, String lname) {
        if(result.isSignInComplete()) {
            showUsers();
        } else {
            // TODO error handling
            System.out.println("SIGN IN Failed: " + result.toString());
        }
    }


    private void loginFail(AuthException err, String userName, String fname, String lname) {
        if(err.toString().contains("User does not exist")) {
            registerUser(userName, fname, lname);
        }

    }

    // Registers a FCM device token with bandwidth's push notifier api, if it doesn't exist
    // and launches the next activity of showing a list of users
    private void registerClientAndShowUsers(String fname, String lname) {
        String token = BWLibrary.getFirebaseDeviceToken();

        try {
            deviceId = registerClient(token);

            savePerson(fname, lname, deviceId);
            showUsers();
        } catch (Exception e) {
            System.out.println("Exception querying for device: " + e.getMessage());
        }
    }

    private void savePerson(String fname, String lname, String deviceId) {
        Amplify.DataStore.observe(Person.class,
                started -> System.out.println("Observation began."),
                change -> System.out.println(change.item().toString()),
                failure -> System.out.println("Observation failed." + failure),
                () -> System.out.println("Observation complete.")
        );

        Person person = Person.builder()
                .firstName(fname)
                .lastName(lname)
                .clientId(deviceId)
                .build();

        Amplify.DataStore.save(person,
                success -> System.out.println("Saved person " + person.getFirstName()),
                error -> System.out.println("Could not save person to Datastore" + error)
        );
    }

    private void registerUser(String userName, String fname, String lname) {
        AuthSignUpOptions options = AuthSignUpOptions.builder()
                // TODO get the email from UI for registrations
                .userAttribute(AuthUserAttributeKey.email(), "test@test.com")
                .build();

        System.out.println("Attempting to register user " + userName);
        Amplify.Auth.signUp(
                userName,
                "raleigh123", // FIXME for demo, we will use this password for everyone
                options,
                result -> registerSuccess(result, fname, lname),
                // TODO error handling
                error -> System.out.println("Register Failure: " + error.toString())
                );
    }

    private void registerSuccess(AuthSignUpResult result, String fname, String lname) {
        System.out.println("Signup succeeded: " + result.toString());
        registerClientAndShowUsers(fname, lname);
    }

    // fires a new activity, ListUsersActivity
    private void showUsers() {
        System.out.println("showUsers called()");
        Intent i = new Intent(this, ListUsersActivity.class);
        i.putExtra("redirectFromLogin", "1");
        startActivity(i);
    }


    // This is how you would do API posts using the Amplify wrapper
    private void apiPostUsingAmplify(String deviceToken) {
        String json = "{" +
                "\"action\": \"register\"," +
                "\"notifyType\": \"GCM\"," +
                "\"deviceToken\":\"" + deviceToken + "\"" +
                "}";
        try {
            System.out.println("Trying to use AWSApiPlugin...");
            RestOptions options = RestOptions.builder()
                    .addPath("/api")
                    .addBody(json.getBytes())
                    .build();

            Amplify.API.post(options,
                    response -> System.out.println(response.getData().toString()),
                    error -> System.out.println("LoginActivity: POST failed." + error.toString())
            );
        } catch (Exception e) {
            System.out.println("LoginActivity: Exception trying AWSApiPlugin: " + e.getMessage());
        }
    }

    private String registerClient(String deviceToken) {
        String json = "{" +
                "\"action\": \"register\"," +
                "\"notifyType\": \"GCM\"," +
                "\"deviceToken\":\"" + deviceToken + "\"" +
                "}";

        try {
            String registerUrl = "https://eys0a9ycb7.execute-api.us-east-1.amazonaws.com/default/webrtcPushNotifier-staging";

            URL url = new URL(registerUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true );
            connection.connect();

            OutputStream os = connection.getOutputStream();
            os.write(json.getBytes("UTF-8"));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String output;
            while ((output = bufferedReader.readLine()) != null) {
                stringBuilder.append(output);
            }
            output = stringBuilder.toString();
            System.out.println("GETTING RESP " + output );
            JSONObject resp = new JSONObject(output);

            return (String)resp.get("id");
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error registering client " + e.getMessage());
            return null;
        }
    }

//    private void updateUiWithUser(LoggedInUserView model) {
//        String welcome = getString(R.string.welcome) + model.getDisplayName();
//        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
//    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    public static void isSignedIn(Context context, AuthSession result) {
        System.out.println("isSignedIn()");

        if(!result.isSignedIn()) {
            System.out.println("Not logged in");

            Intent i = new Intent(context, LoginActivity.class);
            context.startActivity(i);
        }
    }
}