package  com.bandwidth.android.ui.login;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import com.amplifyframework.auth.AuthException;
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
import com.bandwidth.android.ListUsersActivity;
import com.bandwidth.android.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     binding = ActivityLoginBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
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
                    System.out.println("Login attempt for user " + userName);

                    Amplify.addPlugin(new AWSCognitoAuthPlugin());
                    Amplify.addPlugin(new AWSApiPlugin());
                    Amplify.addPlugin(new AWSDataStorePlugin());
                    Amplify.configure(getApplicationContext());

                    // FIXME hardcoded password for demo
                    Amplify.Auth.signIn(
                            userName,
                            "raleigh123",
                            result -> loginSuccess(result),
                            error -> loginFail(error, userName)
                    );
                } catch(Exception e) {
                    System.out.println("Error logging in to cognito: " + e.getMessage());
                }

//                loadingProgressBar.setVisibility(View.VISIBLE);
//                loginViewModel.login(usernameEditText.getText().toString(),
//                        ""); // FIXME password set to empty string for demo purposes
            }
        });
    }

    private void loginSuccess(AuthSignInResult result) {
        if(result.isSignInComplete()) {
            System.out.println("SIGN IN Succeeded");
            registerClientAndShowUsers();
        } else {
            // TODO error handling
            System.out.println("SIGN IN Failed: " + result.toString());
        }
    }


    private void loginFail(AuthException err, String userName) {
        System.out.println("Sign in Exception");
        if(err.toString().contains("User does not exist")) {
            System.out.println("Need to signup user");
            registerUser(userName);
        }

    }

    // Registers a FCM device token with bandwidth's push notifier api, if it doesn't exist
    // and launches the next activity of showing a list of users
    private void registerClientAndShowUsers() {
        String token = getFirebaseDeviceToken();
        System.out.println("DEVICE TOKEN=" + token);
        try {
            Amplify.DataStore.query(DeviceInfo.class, Where.matches(DeviceInfo.DEVICE_TOKEN.eq(token)),
                    devices -> {
                        System.out.println("DEVICES: "  + devices.toString());
                        boolean exists = false;
                        while (devices.hasNext()) {
                            // FIXME there has to be a better way to determine if this device token exists...
                            exists = true;
                            break;
                        }
                        if(!exists) {
                            System.out.println("Registering device!");
                            registerClient(token);
                        }
                        showUsers();
                    },
                    failure -> System.out.println("existsDeviceToken query failed: " + failure.toString())
            );
        } catch (Exception e) {
            System.out.println("Exception querying for device: " + e.getMessage());
        }
    }

    private void registerUser(String userName) {
        AuthSignUpOptions options = AuthSignUpOptions.builder()
                // TODO change Cognito User Pool to remove email requirement for demo purposes
                //      currently, email hardcoded to Srikants BW email
                .userAttribute(AuthUserAttributeKey.email(), "sayengar@bandwidth.com")
                .build();

        // TODO insert into Person table; create UI for FirstName and LastName

        System.out.println("Attempting to register user " + userName);
        Amplify.Auth.signUp(
                userName,
                "raleigh123", // FIXME for demo, we will use this password for everyone
                options,
                result -> registerSuccess(result),
                // TODO handle registration failure
                error -> System.out.println("Register Failure: " + error.toString())
                );
    }

    private void registerSuccess(AuthSignUpResult result) {
        System.out.println("Signup succeeded: " + result.toString());
        registerClientAndShowUsers();
    }

    private void showUsers() {
        System.out.println("showUsers called()");
        Intent i = new Intent(this, ListUsersActivity.class);
        startActivity(i);
    }

    private String getFirebaseDeviceToken() {
        String deviceToken = null;
        try {
            Task tokenTask = FirebaseMessaging.getInstance().getToken();
            Tasks.await(tokenTask);
            deviceToken = (String)tokenTask.getResult();
            System.out.println("getFirebaseDeviceToken = " + deviceToken);
        } catch(Exception e) {
            System.out.println("Error getting firebaseDeviceToken: " + e.getMessage());
        } finally {
            return deviceToken;
        }
    }

    private void registerClient(String deviceToken) {
        try {
            // TODO
            // is notifyType supposed to be FCM or GCM ?
            String json = "{" +
                    "\"action\": \"register\"," +
                    "\"notifyType\": \"GCM\"," +
                    "\"deviceToken\":\"" + deviceToken + "\"" +
                    "}";

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
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error registering client " + e.getMessage());
        }
    }

//    private void updateUiWithUser(LoggedInUserView model) {
//        String welcome = getString(R.string.welcome) + model.getDisplayName();
//        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
//    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}