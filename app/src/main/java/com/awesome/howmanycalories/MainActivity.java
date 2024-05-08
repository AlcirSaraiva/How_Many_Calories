package com.awesome.howmanycalories;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.BILLING_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.DEVELOPER_ERROR;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.ERROR;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_NOT_OWNED;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.NETWORK_ERROR;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_DISCONNECTED;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_TIMEOUT;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.metrics.Event;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // system
    private Context context;
    private Activity activityContext;
    private MainActivity applicationContext;
    private Handler threadCycleHandler = new Handler();
    private Runnable threadCycleRunnable;
    private int threadCycleInterval = 300;
    private boolean isOnline = false;
    private BroadcastReceiver receiver;
    private DisplayMetrics displayMetrics;
    private String TAG = "AppTag: ";
    private boolean adDebug = true;
    private String[] answers, saved, savedTemp;
    private boolean timeToBuildAnswer = false, showInfo = false;
    private String filesPath, saveFile = "data.txt";
    private String infoMessage = "";
    private boolean firstRelease = true;

    // ui
    private RelativeLayout rootView, contentView, splashScreen;
    private LinearLayout mainScreen, resultsButtonUnderline, savedButtonUnderline, settingsScreen, infoScreen;
    private EditText question;
    private ImageButton questionButton, buttonSettings, buttonSettingsClose;
    private ListView answerListView, savedListView;
    private Typeface typeRegular;
    private Button resultsButton, savedButton;
    private TextView infoTextView;

    // Ads
    private ImageView bottomBanner;
    private AdView mAdView;
    private AdRequest adRequest;
    private int adHeight = 0, contentViewMargin = 0, lastContentViewMargin = 0;
    private boolean adInitDone = false;
    private boolean showAds = true;
    private boolean legalTextAlreadyCalled = false;
    private ConsentInformation consentInformation;

    // subscriptions
    private PurchasesUpdatedListener purchasesUpdatedListener;
    private BillingClient billingClient;
    private BillingClientStateListener billingClientStateListener;
    private QueryProductDetailsParams queryProductDetailsParams;
    private boolean subscriptionsInitialized = false;
    private ProductDetails productDetails;
    private String offerToken, formattedPrice;
    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener;
    private String subscriptionId = "basic_subscription";
    private boolean fullVersionUpdateUI = false;
    private boolean isFullVersion = false;
    private LinearLayout onlineCard;
    private Button buttonBuySubscription;
    private TextView subscriptionCardTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        applicationContext = this;
        activityContext = MainActivity.this;
        displayMetrics = context.getResources().getDisplayMetrics();
        filesPath = context.getExternalFilesDir(null).getAbsolutePath();
        prepareNetwork();

        assignViews();
        assignViewListeners();
        assignFonts();

        infoTextView.setText(getText(R.string.results_empty));

        checkFirstRelease();

        showBottomBanner();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOutAndHideImage(splashScreen);

                if (!isOnline) {
                    onlineCard.setVisibility(View.VISIBLE);
                } else {
                    initSubscriptions();
                }

                threadCycleRunnable = new Runnable() {
                    @Override
                    public void run() {
                        threadCycle();
                        threadCycleHandler.postDelayed(this, threadCycleInterval);
                    }
                };
                threadCycleHandler.postDelayed(threadCycleRunnable, threadCycleInterval);
            }
        }, 2000);
    }

    private void threadCycle() {
        if (onlineCard.getVisibility() == View.VISIBLE && isOnline) {
            onlineCard.setVisibility(View.GONE);
            initSubscriptions();
        }
        // show ads
        if (contentViewMargin == 0 && mAdView != null && showAds) {
            adHeight = mAdView.getHeight();
        }
        if (adHeight != 0 && contentView != null && contentViewMargin == 0 && lastContentViewMargin != adHeight && showAds) {
            contentViewMargin = adHeight;
            lastContentViewMargin = contentViewMargin;
            setContentViewMargin(adHeight);
        }
        // when full version on start
        if (fullVersionUpdateUI) {
            fullVersionUpdateUI = false;
            setContentViewMargin(0);
        }
        // when full version after purchase
        if (!showAds && mAdView != null) {
            rootView.removeView(mAdView);
            mAdView = null;
            setContentViewMargin(0);
        }
        if (timeToBuildAnswer) {
            timeToBuildAnswer = false;
            buildAnswer();
            showResults();
            question.clearFocus();
        }
        if (showInfo) {
            showInfo = false;
            infoScreen.setVisibility(View.VISIBLE);
            infoTextView.setText(infoMessage);
            question.clearFocus();
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            System.out.println(TAG + "{hideKeyboard} " + e.getMessage());
        }
    }

    private void showKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } catch (Exception e) {
            System.out.println(TAG + "{showNoteInput} " + e.getMessage());
        }
    }

    private void buildAnswer() {
        savedTemp = readFile().split("\n");
        AnswerListAdapter answerListAdapter = new AnswerListAdapter(context, R.layout.answer_list, answers);
        answerListView.setAdapter(answerListAdapter);
    }

    private void buildSaved() {
        saved = readFile().split("\n");
        ArrayList<String> temp = new ArrayList<String>();

        for (String s : saved) {
            if (!s.isEmpty()) temp.add(s);
        }
        saved = temp.toArray(new String[temp.size()]);

        Collections.reverse(Arrays.asList(saved));

        SavedListAdapter savedListAdapter = new SavedListAdapter(context, R.layout.saved_list, saved);
        savedListView.setAdapter(savedListAdapter);
    }

    public class AnswerListAdapter extends ArrayAdapter {
        private Context ctx;
        private String[] answerRaw;
        private int resource;
        private TextView nameValue, caloriesValue, servingValue, items, values, subscribeTitle;
        private Button saveButton;

        public AnswerListAdapter(@NonNull Context ctx, int resource, String[] answerRaw) {
            super(ctx, resource, answerRaw);
            this.ctx = ctx;
            this.answerRaw = answerRaw;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(ctx).inflate(resource, parent, false);

            try {
                nameValue = convertView.findViewById(R.id.answer_name);
                caloriesValue = convertView.findViewById(R.id.answer_calories);
                servingValue = convertView.findViewById(R.id.answer_serving);
                items = convertView.findViewById(R.id.answer_items);
                values = convertView.findViewById(R.id.answer_values);
                subscribeTitle = convertView.findViewById(R.id.answer_subscribe);
                saveButton = convertView.findViewById(R.id.answer_save_button);

                nameValue.setTypeface(typeRegular);
                caloriesValue.setTypeface(typeRegular);
                servingValue.setTypeface(typeRegular);
                items.setTypeface(typeRegular);
                values.setTypeface(typeRegular);
                subscribeTitle.setTypeface(typeRegular);
                saveButton.setTypeface(typeRegular);

                if (!showAds) subscribeTitle.setText("");

                String[] temp = answerRaw[position].split(",");
                String upperString;
                String vTemp;

                if (temp.length == 12) {
                    upperString = temp[0].substring(0, 1).toUpperCase() + temp[0].substring(1);
                    nameValue.setText(upperString);
                    vTemp = temp[1] + " Kcal";
                    caloriesValue.setText(vTemp);
                    vTemp = "Serving size: " + temp[2] + " g";
                    servingValue.setText(vTemp);
                    if (showAds) {
                        values.setText(getString(R.string.no_value));
                        items.setTextColor(getColor(R.color.grey_50));
                        values.setTextColor(getColor(R.color.grey_50));
                    } else {
                        vTemp = temp[3] + " g\n" +
                                temp[4] + " g\n" +
                                temp[5] + " mg\n" +
                                temp[6] + " mg\n" +
                                temp[7] + " g\n" +
                                temp[8] + " g\n" +
                                temp[9] + " g\n" +
                                temp[10] + " g\n" +
                                temp[11] + " g";
                        values.setText(vTemp);
                        items.setTextColor(getColor(R.color.grey_text));
                        values.setTextColor(getColor(R.color.grey_text));
                    }
                }

                boolean found = false;
                for (int i = 0; i < savedTemp.length; i ++) {
                    if (savedTemp[i].contains(answerRaw[position])) {
                        saveButton.setText(getString(R.string.saved));
                        saveButton.setTextColor(getColor(R.color.primary_1));
                        found = true;
                    }
                }

                if (!found) {
                    saveButton.setText(getString(R.string.save));
                    saveButton.setTextColor(getColor(R.color.white));
                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            save(answerRaw[position]);
                            buildAnswer();
                            saveButton.setText(getString(R.string.saved));
                            saveButton.setTextColor(getColor(R.color.primary_1));
                            answerListView.setSelection(position);
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(TAG + "{AnswerListAdapter getView()} " + e.getMessage());
            }

            return convertView;
        }
    }

    public class SavedListAdapter extends ArrayAdapter {
        private Context ctx;
        private String[] savedRaw;
        private int resource;
        private Button nameValue, caloriesValue, deleteButton;

        public SavedListAdapter(@NonNull Context ctx, int resource, String[] savedRaw) {
            super(ctx, resource, savedRaw);
            this.ctx = ctx;
            this.savedRaw = savedRaw;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(ctx).inflate(resource, parent, false);

            try {
                nameValue = convertView.findViewById(R.id.saved_name);
                caloriesValue = convertView.findViewById(R.id.saved_calories_per_serving);
                deleteButton = convertView.findViewById(R.id.saved_delete_button);

                nameValue.setTypeface(typeRegular);
                caloriesValue.setTypeface(typeRegular);
                deleteButton.setTypeface(typeRegular);

                String[] temp = savedRaw[position].split(",");
                String upperString;
                String vTemp;

                if (temp.length == 12) {
                    upperString = temp[0].substring(0, 1).toUpperCase() + temp[0].substring(1);
                    nameValue.setText(upperString);
                    vTemp = temp[1] + " Kcal / " + temp[2] + " g";
                    caloriesValue.setText(vTemp);
                }

                nameValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = -1;
                        if (answers != null) {
                            if (answers.length != 0) {
                                for (int i = 0; i < answers.length; i ++) {
                                    if (answers[i].contains(savedRaw[position])) {
                                        index = i;
                                    }
                                }
                                if (index == -1) {
                                    ArrayList<String> tempItems = new ArrayList<>(Arrays.asList(answers));
                                    tempItems.add(savedRaw[position]);
                                    answers = tempItems.toArray(new String[0]);
                                    index = answers.length - 1;
                                }
                            } else {
                                answers = new String[1];
                                answers[0] = savedRaw[position];
                            }
                        } else {
                            answers = new String[1];
                            answers[0] = savedRaw[position];
                        }

                        buildAnswer();
                        showResults();
                        if (index != -1) {
                            answerListView.setSelection(index);
                        }
                    }
                });

                caloriesValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = -1;
                        if (answers != null) {
                            if (answers.length != 0) {
                                for (int i = 0; i < answers.length; i ++) {
                                    if (answers[i].contains(savedRaw[position])) {
                                        index = i;
                                    }
                                }
                                if (index == -1) {
                                    ArrayList<String> tempItems = new ArrayList<>(Arrays.asList(answers));
                                    tempItems.add(savedRaw[position]);
                                    answers = tempItems.toArray(new String[0]);
                                    index = answers.length - 1;
                                }
                            } else {
                                answers = new String[1];
                                answers[0] = savedRaw[position];
                            }
                        } else {
                            answers = new String[1];
                            answers[0] = savedRaw[position];
                        }

                        buildAnswer();
                        showResults();
                        if (index != -1) {
                            answerListView.setSelection(index);
                        }
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFromFile(savedRaw[position]);
                        showSaved();
                        if (answers != null) buildAnswer();

                    }
                });
            } catch (Exception e) {
                System.out.println(TAG + "{SavedListAdapter getView()} " + e.getMessage());
            }

            return convertView;
        }
    }

    // ui

    private void assignViews() {
        splashScreen = findViewById(R.id.splash_screen);
        rootView = findViewById(R.id.root_view);
        contentView = findViewById(R.id.content_view);
        bottomBanner = findViewById(R.id.bottom_banner);

        onlineCard = findViewById(R.id.online_card);

        settingsScreen = findViewById(R.id.settings);
        subscriptionCardTitle = findViewById(R.id.subscription_card_title);

        buttonBuySubscription = findViewById(R.id.button_buy_subscription);
        buttonSettingsClose = findViewById(R.id.button_settings_close);

        question = findViewById(R.id.question);
        questionButton = findViewById(R.id.question_button);

        mainScreen = findViewById(R.id.main_screen);
        answerListView = findViewById(R.id.answer_list_view);
        savedListView = findViewById(R.id.saved_list_view);

        resultsButton = findViewById(R.id.results_button);
        savedButton = findViewById(R.id.saved_button);
        resultsButtonUnderline = findViewById(R.id.results_button_underline);
        savedButtonUnderline = findViewById(R.id.saved_button_underline);
        buttonSettings = findViewById(R.id.settings_button);

        infoScreen = findViewById(R.id.info);
        infoTextView = findViewById(R.id.info_text_view);
    }

    private void assignViewListeners() {
        /* show subscriptions
        Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/account/subscriptions"));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
         */
        buttonSettingsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSettings();
            }
        });
        buttonBuySubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainScreen.setVisibility(View.VISIBLE);
                        fadeOutAndHideImage(settingsScreen);
                    }
                }, 1000);
                initSubscriptions();
                startSubscribeFlow();
            }
        });
        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryCalories();
            }
        });
        resultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResults();
            }
        });
        savedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSaved();
            }
        });
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
        question.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    queryCalories();
                    return true;
                }
                return false;
            }
        });
    }

    private void assignFonts() {
        typeRegular = Typeface.createFromAsset(getAssets(), "regular.ttf");
        TextView questionTitle = findViewById(R.id.question_title);
        questionTitle.setTypeface(typeRegular);
        resultsButton.setTypeface(typeRegular);
        savedButton.setTypeface(typeRegular);
        infoTextView.setTypeface(typeRegular);
        question.setTypeface(typeRegular);
    }

    private void fadeOutAndHideImage(final View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }

    private void setContentViewMargin(int margin) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentView.getLayoutParams();
        params.setMargins(0, 0, 0, margin);
        contentView.setLayoutParams(params);
        bottomBanner.getLayoutParams().height = margin;
    }

    private void showBottomBanner() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFullVersion) {
                    setContentViewMargin(displayMetrics.heightPixels / 11);
                }
            }
        }, 1500);
    }

    private void showResults() {
        if (answers != null) {
            if (answers.length != 0) {
                infoScreen.setVisibility(View.GONE);
                answerListView.setVisibility(View.VISIBLE);
                savedListView.setVisibility(View.GONE);
                resultsButton.setTextColor(getColor(R.color.black));
                savedButton.setTextColor(getColor(R.color.grey_button_text));
                resultsButtonUnderline.setBackgroundColor(getColor(R.color.primary));
                savedButtonUnderline.setBackgroundColor(getColor(R.color.transparent));
            } else {
                infoScreen.setVisibility(View.VISIBLE);
                answerListView.setVisibility(View.GONE);
                savedListView.setVisibility(View.GONE);
                resultsButton.setTextColor(getColor(R.color.black));
                savedButton.setTextColor(getColor(R.color.grey_button_text));
                resultsButtonUnderline.setBackgroundColor(getColor(R.color.primary));
                savedButtonUnderline.setBackgroundColor(getColor(R.color.transparent));
                infoTextView.setText(getText(R.string.results_empty));
            }
        } else {
            infoScreen.setVisibility(View.VISIBLE);
            answerListView.setVisibility(View.GONE);
            savedListView.setVisibility(View.GONE);
            resultsButton.setTextColor(getColor(R.color.black));
            savedButton.setTextColor(getColor(R.color.grey_button_text));
            resultsButtonUnderline.setBackgroundColor(getColor(R.color.primary));
            savedButtonUnderline.setBackgroundColor(getColor(R.color.transparent));
            infoTextView.setText(getText(R.string.results_empty));
        }
    }

    private void showSaved() {
        buildSaved();
        if (saved != null) {
            if (saved.length != 0) {
                infoScreen.setVisibility(View.GONE);
                answerListView.setVisibility(View.GONE);
                savedListView.setVisibility(View.VISIBLE);
                resultsButton.setTextColor(getColor(R.color.grey_button_text));
                savedButton.setTextColor(getColor(R.color.black));
                resultsButtonUnderline.setBackgroundColor(getColor(R.color.transparent));
                savedButtonUnderline.setBackgroundColor(getColor(R.color.primary));
            } else {
                infoScreen.setVisibility(View.VISIBLE);
                answerListView.setVisibility(View.GONE);
                savedListView.setVisibility(View.GONE);
                resultsButton.setTextColor(getColor(R.color.grey_button_text));
                savedButton.setTextColor(getColor(R.color.black));
                resultsButtonUnderline.setBackgroundColor(getColor(R.color.transparent));
                savedButtonUnderline.setBackgroundColor(getColor(R.color.primary));
                infoTextView.setText(getText(R.string.saved_empty));
            }
        } else {
            infoScreen.setVisibility(View.VISIBLE);
            answerListView.setVisibility(View.GONE);
            savedListView.setVisibility(View.GONE);
            resultsButton.setTextColor(getColor(R.color.grey_button_text));
            savedButton.setTextColor(getColor(R.color.black));
            resultsButtonUnderline.setBackgroundColor(getColor(R.color.transparent));
            savedButtonUnderline.setBackgroundColor(getColor(R.color.primary));
            infoTextView.setText(getText(R.string.saved_empty));
        }
    }

    private void openSettings() {
        settingsScreen.setVisibility(View.VISIBLE);
        mainScreen.setVisibility(View.GONE);
    }

    private void closeSettings() {
        settingsScreen.setVisibility(View.GONE);
        mainScreen.setVisibility(View.VISIBLE);
    }

    private void checkFirstRelease() {
        if (firstRelease) {
            buttonSettings.setVisibility(View.GONE);
            showAds = false;
            fullVersionUpdateUI = true;
        }
    }

    // IO

    private void save(String dataToAdd) {
        String dataOnFile = readFile();

        String[] itemsOnFile = dataOnFile.split("\n");

        int index = -1;
        for (int i = 0; i < itemsOnFile.length; i ++) {
            if (itemsOnFile[i].contains(dataToAdd)) {
                index = i;
            }
        }
        if (index == -1) {
            if (dataOnFile.isEmpty()) {
                dataOnFile = dataToAdd;
            } else {
                dataOnFile = dataOnFile + "\n" + dataToAdd;
            }
        }

        writeFile(dataOnFile);

    }

    private String readFile() {
        String dataRead = "";
        File file = new File(filesPath, saveFile);
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytes);
            fileInputStream.close();
            dataRead = new String(bytes);
        } catch (Exception e) {
            System.out.println(TAG + "{readFile} " + e.getMessage());
        }
        return dataRead;
    }

    private void writeFile(String data) {
        File file = new File(filesPath, saveFile);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println(TAG + "{writeFile} " + e.getMessage());
        }
    }

    private void deleteFromFile(String dataToDelete) {
        String dataOnFile = readFile();

        String[] itemsOnFile = dataOnFile.split("\n");

        String newData = "";
        for (int i = 0; i < itemsOnFile.length; i ++) {
            if (!itemsOnFile[i].contains(dataToDelete) && !itemsOnFile[i].isEmpty()) {
                if (newData.isEmpty()) {
                    newData = itemsOnFile[i];
                } else {
                    newData = newData + "\n" + itemsOnFile[i];
                }
            }
        }

        writeFile(newData);
    }

    // network

    private void prepareNetwork() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        } catch (Exception e) {
            System.out.println(TAG + "{prepareNetwork} " + e.getMessage());
        }
        isOnline = isNetworkAvailable();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context cxt, Intent intent) {
                isOnline = isNetworkAvailable();
            }
        };
        registerReceiver(receiver, filter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    private void queryCalories() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                infoMessage = getString(R.string.working);
                showInfo = true;
                String questionText = question.getText().toString();
                hideKeyboard();
                if (!questionText.isEmpty()) {
                    hideKeyboard();

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://api.calorieninjas.com/v1/nutrition?query=" + questionText)
                            .addHeader("X-Api-Key","TgFfSH3VsxyNdR3Hger92A==It0gCuSjAYjSyGuS")
                            .build();

                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject jObject = new JSONObject(responseBody);
                            JSONArray jArray = jObject.getJSONArray("items");
                            JSONObject item;

                            answers = new String[jArray.length()];

                            if (answers.length == 0) {
                                infoMessage = getString(R.string.not_found);
                                showInfo = true;
                            }

                            for (int i = 0; i < jArray.length(); i ++) {
                                item = jArray.getJSONObject(i);

                                answers[i] = item.getString("name") + "," +
                                        item.getString("calories") + "," +
                                        item.getString("serving_size_g") + "," +
                                        item.getString("sugar_g") + "," +
                                        item.getString("fiber_g") + "," +
                                        item.getString("sodium_mg") + "," +
                                        item.getString("potassium_mg") + "," +
                                        item.getString("fat_saturated_g") + "," +
                                        item.getString("fat_total_g") + "," +
                                        item.getString("cholesterol_mg") + "," +
                                        item.getString("carbohydrates_total_g") + "," +
                                        item.getString("protein_g");
                            }
                            timeToBuildAnswer = true;
                        } else {
                            infoMessage = getString(R.string.something_wrong);
                            showInfo = true;
                        }
                    } catch (Exception e) {
                        infoMessage = getString(R.string.something_wrong);
                        showInfo = true;
                        System.out.println(TAG + "{queryCalories} " + e.getMessage());
                    }
                } else {
                    infoMessage = getString(R.string.empty);
                    showInfo = true;
                }
            }
        });
    }

    // Ads

    private void initAds() {
        if (showAds && !adInitDone && subscriptionsInitialized) {
            adInitDone = true;
            showLegalTextIfNeeded();
        }
    }

    private void showLegalTextIfNeeded() {
        if (!legalTextAlreadyCalled) {
            legalTextAlreadyCalled = true;
            ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
            consentInformation = UserMessagingPlatform.getConsentInformation(context);
            consentInformation.requestConsentInfoUpdate(activityContext, params,
                    new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                        @Override
                        public void onConsentInfoUpdateSuccess() {
                            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activityContext,
                                    new ConsentForm.OnConsentFormDismissedListener() {
                                        @Override
                                        public void onConsentFormDismissed(@Nullable FormError loadAndShowError) {
                                            if (loadAndShowError != null) {
                                                if (adDebug) System.out.println(TAG + "Consent gathering failed (" + loadAndShowError.getErrorCode() + "): " + loadAndShowError.getMessage());
                                            }
                                            if (adDebug) System.out.println(TAG + "Ads consent has been gathered");
                                            MobileAds.initialize(context, new OnInitializationCompleteListener() {
                                                @Override
                                                public void onInitializationComplete(InitializationStatus initializationStatus) {
                                                    if (adDebug) System.out.println(TAG + "AdMob initialized");
                                                    setAds();
                                                }
                                            });
                                        }
                                    });
                        }
                    },
                    new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                        @Override
                        public void onConsentInfoUpdateFailure(@NonNull FormError requestConsentError) {
                            if (adDebug) System.out.println(TAG + "Consent gathering failed (" + requestConsentError.getErrorCode() + "): " + requestConsentError.getMessage());
                        }
                    });
        }
    }

    private void setAds() {

        mAdView = new AdView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rootView.addView(mAdView, layoutParams);

        AdSize adSize = getAdSize();
        mAdView.setAdSize(adSize);

        // test id ca-app-pub-3940256099942544/9214589741
        mAdView.setAdUnitId("ca-app-pub-3940256099942544/9214589741");

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                if (adDebug) System.out.println(TAG + "onAdClicked");
            }

            @Override
            public void onAdClosed() {
                if (adDebug) System.out.println(TAG + "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                if (adDebug) System.out.println(TAG + "onAdFailedToLoad " + adError.toString());
            }

            @Override
            public void onAdImpression() {
                if (adDebug) System.out.println(TAG + "onAdImpression");
            }

            @Override
            public void onAdLoaded() {
                if (adDebug) System.out.println(TAG + "onAdLoaded");
                contentViewMargin = 0;
                adHeight = mAdView.getHeight();
            }

            @Override
            public void onAdOpened() {
                if (adDebug) System.out.println(TAG + "onAdOpened");
            }
        });
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private AdSize getAdSize() {
        float adWidthPixels = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    // subscriptions

    private void initSubscriptions() {
        if (!subscriptionsInitialized) {
            // Responds when user buys
            purchasesUpdatedListener = new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (Purchase purchase : purchases) {
                            System.out.println(TAG + "Purchase updated: " + purchase.toString());
                            handlePurchase(purchase);
                        }
                    } else {
                        System.out.println(TAG + "Purchase update: " + getResultMessage(billingResult.getResponseCode()));
                    }
                }
            };
            billingClient = BillingClient.newBuilder(context)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build();

            billingClientStateListener = new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == OK) {
                        subscriptionsInitialized = true;
                        System.out.println(TAG + "Subscriptions initialized");
                        initAds();
                        queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                                .setProductList(
                                        ImmutableList.of(
                                                QueryProductDetailsParams.Product.newBuilder()
                                                        .setProductId(subscriptionId)
                                                        .setProductType(BillingClient.ProductType.SUBS)
                                                        .build()))
                                .build();

                        billingClient.queryProductDetailsAsync(
                                queryProductDetailsParams,
                                new ProductDetailsResponseListener() {
                                    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                                        if (billingResult.getResponseCode() == OK) {
                                            for (ProductDetails each : productDetailsList) {
                                                productDetails = each;
                                                for (ProductDetails.SubscriptionOfferDetails sod : each.getSubscriptionOfferDetails()) {
                                                    offerToken = sod.getOfferToken();
                                                    for (ProductDetails.PricingPhase pp : sod.getPricingPhases().getPricingPhaseList()) {
                                                        formattedPrice = pp.getFormattedPrice();
                                                    }
                                                }
                                            }
                                        } else {
                                            System.out.println(TAG + "Product Detail Response: " + getResultMessage(billingResult.getResponseCode()));
                                        }
                                    }
                                }
                        );

                        // Responds on start
                        billingClient.queryPurchasesAsync(
                                QueryPurchasesParams.newBuilder()
                                        .setProductType(BillingClient.ProductType.SUBS)
                                        .build(),
                                new PurchasesResponseListener() {
                                    @Override
                                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                                        if (billingResult.getResponseCode() == OK) {
                                            for (Purchase each : list) {
                                                for (String product : each.getProducts()) {
                                                    System.out.println(TAG + "Query Purchases Response: " + product);
                                                    if (product.equals(subscriptionId)) {
                                                        fullVersion();
                                                    } else {
                                                        initAds();
                                                    }
                                                }
                                            }
                                        } else {
                                            System.out.println(TAG + "Query Purchases Response: " + getResultMessage(billingResult.getResponseCode()));
                                            initAds();
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    billingClient.startConnection(billingClientStateListener);
                }
            };

            acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == OK) {
                        System.out.println(TAG + "Purchase acknowledged");
                        fullVersion();
                    } else {
                        System.out.println(TAG + "Acknowledge Purchase Response: " + getResultMessage(billingResult.getResponseCode()));
                    }
                }
            };


            billingClient.startConnection(billingClientStateListener);
        }
    }

    private void startSubscribeFlow() {
        if (subscriptionsInitialized && productDetails != null && offerToken != null) {
            ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                    ImmutableList.of(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                    .build()
                    );

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build();

            BillingResult billingResult = billingClient.launchBillingFlow(activityContext, billingFlowParams);
        }
    }

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    private String getResultMessage(int response) {
        String result = "";
        switch (response) {
            case BILLING_UNAVAILABLE :
                result = "Billing unavailable";
                break;
            case DEVELOPER_ERROR :
                result = "Developer error";
                break;
            case ERROR :
                result = "Error";
                break;
            case FEATURE_NOT_SUPPORTED :
                result = "Feature not supported";
                break;
            case ITEM_ALREADY_OWNED :
                result = "Item already owned";
                break;
            case ITEM_NOT_OWNED :
                result = "Item not owned";
                break;
            case ITEM_UNAVAILABLE :
                result = "Item unavailable";
                break;
            case NETWORK_ERROR :
                result = "Network error";
                break;
            case OK :
                result = "Ok";
                break;
            case SERVICE_DISCONNECTED :
                result = "Service disconnected";
                break;
            case SERVICE_TIMEOUT :
                result = "Service timeout";
                break;
            case SERVICE_UNAVAILABLE :
                result = "Service unavailable";
                break;
            case USER_CANCELED :
                result = "User canceled";
                break;
        }
        return result;
    }

    private void fullVersion() {
        showAds = false;
        fullVersionUpdateUI = true;
        isFullVersion = true;
    }
}