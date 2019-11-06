package org.techtown.randompassword;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    /*지문인식 start*/
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    private static final String SECRET_MESSAGE = "Very secret message";
    private static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";
    static final String DEFAULT_KEY_NAME = "default_key";

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private SharedPreferences mSharedPreferences;

    private Cipher defaultCipher;
    private Cipher cipherNotInvalidated;

    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;
    /*지문인식 end*/

    private EditText idEditText;    //  id입력창
    private EditText pwEditText;    //  pw입력창
    private TextView textView;    //  pw 보이는 textView

    private Button loginButton, joinButton, findPwButton;

    private static RequestQueue RequestQueue;    //  Volley 리퀘스트

    private void inIt() {
        loginButton = findViewById(R.id.loginButton);
        joinButton = findViewById(R.id.joinButton);
        findPwButton = findViewById(R.id.findPwButton);
        idEditText = findViewById(R.id.idEditText);
        pwEditText = findViewById(R.id.pwEditText);
        textView = findViewById(R.id.textView);
        textView.setVisibility(View.GONE);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.loginButton:    //  로그인 버튼
                String login_id = idEditText.getText().toString();
                String login_pwd = pwEditText.getText().toString();
                if(login_id.length() == 0)    //  아이디를 입력하지 않았을 경우
                    Toast.makeText(getApplicationContext(), "아이디를 입력해주세요", Toast.LENGTH_LONG).show();
                else if(login_pwd.length() == 0)    //  비밀번호를 입력하지 않았을 경우
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_LONG).show();
                else
                    loginRequest(login_id, login_pwd);
                break;
            case R.id.joinButton:    //  회원가입 버튼
                Intent joinIntent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(joinIntent);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inIt();    //  초기화
        fingerprint();    //  지문인식

        loginButton.setOnClickListener(this);    //  로그인 버튼
        joinButton.setOnClickListener(this);    //  회원가입 버튼
        findPwButton.setOnClickListener(new PurchaseButtonClickListener(defaultCipher, DEFAULT_KEY_NAME));    //  비밀번호 get 버튼 + 지문인식
    }

    private void loginRequest(final String id, final String pwd) {    //  로그인 리퀘스트
        String url = "http://comstering.synology.me:7070/RandomPassword/PhoneConnection.jsp";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("로그인", response);

                if(response.equals("true")) {
                    Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                    loginIntent.putExtra("id", id);
                    startActivity(loginIntent);
                }
                else if(response.equals("false"))
                    Toast.makeText(getApplicationContext(),"패스워드가 틀렸습니다.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "아이디가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("로그인", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("pwd", pwd);
                params.put("type", "login");

                return params;
            }
        };

        if(RequestQueue == null)
            RequestQueue = Volley.newRequestQueue(getApplicationContext());

        request.setShouldCache(false);
        RequestQueue.add(request);
    }

    private void findPWRequest(final String id) {    //  비밀번호 get 리퀘스트
        String url = "http://comstering.synology.me:7070/RandomPassword/PhoneConnection.jsp";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("noID")){
                    Toast.makeText(getApplicationContext(), "존재하지 않는 아이디 입니다.", Toast.LENGTH_LONG).show();
                } else if(response.equals("error")) {
                    Toast.makeText(getApplicationContext(), "에러", Toast.LENGTH_LONG).show();
                } else {
                    pwEditText.setText(response);
                    textView.setText(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("find", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("type", "find");

                return params;
            }
        };

        if(RequestQueue == null)
            RequestQueue = Volley.newRequestQueue(getApplicationContext());

        findViewById(R.id.textView).setVisibility(View.VISIBLE);    //  비밀번호 textView 보여주기
        Toast.makeText(getApplicationContext(), "비밀번호를 가져왔습니다.", Toast.LENGTH_LONG).show();

        request.setShouldCache(false);
        RequestQueue.add(request);
    }

    private void fingerprint() {    //  지문인식
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }

        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch(NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }

        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        keyguardManager = getSystemService(KeyguardManager.class);
        fingerprintManager = getSystemService(FingerprintManager.class);

        if(!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(this, "디바이스에 지문을 등록해 주세요.", Toast.LENGTH_LONG).show();
            findPwButton.setEnabled(false);
            return;
        }

        if(!fingerprintManager.hasEnrolledFingerprints()) {
            Toast.makeText(this, "디바이스에 지문을 등록해 주세요.", Toast.LENGTH_LONG).show();
            findPwButton.setEnabled(false);
            return;
        }

        createKey(DEFAULT_KEY_NAME, true);
        createKey(KEY_NAME_NOT_INVALIDATED, false);
        findPwButton.setEnabled(true);

        //  참고 : GitHub googlearchive / android-FingerprintDialog   https://github.com/googlearchive/android-FingerprintDialog
    }

    private void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        try {
            mKeyStore.load(null);
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    public void onPurchased(boolean withFingerprint, @Nullable FingerprintManager.CryptoObject cryptoObject) {
        if (withFingerprint) {
            assert cryptoObject != null;
            tryEncrypt(cryptoObject.getCipher());
        } else {
            showConfirmation(null);
        }
    }

    private void showConfirmation(byte[] encrypted) {    //  지문인식이 완료되었을경우
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        String find_id = sharedPreferences.getString("id", "");
        findPWRequest(find_id);    //  비밀번호 가져오기
    }

    private void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal(SECRET_MESSAGE.getBytes());
            showConfirmation(encrypted);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Toast.makeText(this, "Failed to encrypt the data with the generated key. "
                    + "Retry the purchase", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    private class PurchaseButtonClickListener implements View.OnClickListener{
        Cipher mCipher;
        String mKeyName;

        PurchaseButtonClickListener(Cipher cipher, String keyName) {
            mCipher = cipher;
            mKeyName = keyName;
        }

        @Override
        public void onClick(View view) {
            if (initCipher(mCipher, mKeyName)) {
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                String find_id = sharedPreferences.getString("id", "");
                if(find_id.equalsIgnoreCase("")||find_id.length()==0)    //  회원가입이 되어있지 않을 경우
                    Toast.makeText(getApplicationContext(), "회원가입이 되어있지 않습니다.", Toast.LENGTH_LONG).show();
                else {    //  회원가입 되어있을 경우 지문인식
                    FingerPrintDialog fragment = new FingerPrintDialog();
                    fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                    fragment.setStage(FingerPrintDialog.Stage.FINGERPRINT);
                    fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                }
            } else {
                FingerPrintDialog fragment = new FingerPrintDialog();
                fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                fragment.setStage(FingerPrintDialog.Stage.NEW_FINGERPRINT_ENROLLED);
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        }
    }
}
