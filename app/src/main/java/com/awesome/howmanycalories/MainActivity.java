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
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // system
    private Context context;
    private Activity activityContext;
    private Handler threadCycleHandler = new Handler();
    private Runnable threadCycleRunnable;
    private int threadCycleInterval = 300;
    private boolean isOnline = false;
    private BroadcastReceiver receiver;
    private DisplayMetrics displayMetrics;
    private String TAG = "AppTag: ";
    private boolean adDebug = true;

    // ui
    private RelativeLayout rootView, contentView, splashScreen;
    private EditText question;
    private Button questionButton;
    private TextView answer;

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
    private LinearLayout onlineCard, subscriptionCard;
    private Button buttonBuySubscription;
    private ImageButton buttonNoAds, buttonSubscriptionClose;
    private TextView noMoreAdsText, subscriptionCardTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        activityContext = MainActivity.this;
        displayMetrics = context.getResources().getDisplayMetrics();
        prepareNetwork();

        // prepares UI
        assignViews();
        assignViewListeners();

        question.setText("potato");

        showBottomBanner();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOutAndHideImage(splashScreen);

                if (!isOnline) {
                    onlineCard.setVisibility(View.VISIBLE);
                    refreshAppUI(false);
                } else {
                    refreshAppUI(true);
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
        }, 1000);
    }

    private void threadCycle() {
        if (onlineCard.getVisibility() == View.VISIBLE && isOnline) {
            onlineCard.setVisibility(View.GONE);
            refreshAppUI(true);
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
            buttonNoAds.setImageResource(R.drawable.subscriptions);
            noMoreAdsText.setText(getString(R.string.text_manage_subscription));
            setContentViewMargin(0);
        }
        // when full version after purchase
        if (!showAds && mAdView != null) {
            rootView.removeView(mAdView);
            mAdView = null;
            setContentViewMargin(0);
        }
    }

    private void sendQuestion() {
        String questionText = question.getText().toString();
        if (!questionText.isEmpty()) {
            hideKeyboard();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.calorieninjas.com/v1/nutrition?query=" + questionText)
                    .addHeader("X-Api-Key","TgFfSH3VsxyNdR3Hger92A==It0gCuSjAYjSyGuS")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    answer.setText(responseBody);
                } else {
                    answer.setText(response.toString());
                }
            } catch (IOException e) {
                System.out.println(TAG + "{publishToTheServer} " + e.getMessage());
                answer.setText("{publishToTheServer} " + e.getMessage());
            }
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

    // ui

    private void assignViews() {
        splashScreen = findViewById(R.id.splash_screen);
        rootView = findViewById(R.id.root_view);
        contentView = findViewById(R.id.content_view);
        bottomBanner = findViewById(R.id.bottom_banner);

        onlineCard = findViewById(R.id.online_card);

        subscriptionCard = findViewById(R.id.subscription_card);
        subscriptionCardTitle = findViewById(R.id.subscription_card_title);

        buttonNoAds = findViewById(R.id.button_no_ads);
        noMoreAdsText = findViewById(R.id.text_no_more_ads);

        buttonBuySubscription = findViewById(R.id.button_buy_subscription);
        buttonSubscriptionClose = findViewById(R.id.button_subscription_close);

        question = findViewById(R.id.question);
        questionButton = findViewById(R.id.question_button);
        answer = findViewById(R.id.answer);
    }

    private void assignViewListeners() {
        buttonNoAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showAds) {
                    if (formattedPrice != null && subscriptionCardTitle != null) {
                        subscriptionCardTitle.setText(getString(R.string.subscription_title) + " · " + formattedPrice);
                    }
                    refreshAppUI(false);
                    subscriptionCard.setVisibility(View.VISIBLE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/account/subscriptions"));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                }
            }
        });
        buttonSubscriptionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionCard.setVisibility(View.GONE);
                refreshAppUI(true);
            }
        });
        buttonBuySubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fadeOutAndHideImage(subscriptionCard);
                    }
                }, 1000);
                refreshAppUI(true);
                startSubscribeFlow();
            }
        });

        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendQuestion();
            }
        });
    }

    private void refreshAppUI(boolean showUI) {
        if (showUI) {
            if (onlineCard != null) {
                if (onlineCard.getVisibility() != View.VISIBLE) {
                    if (buttonNoAds != null) buttonNoAds.setVisibility(View.VISIBLE);
                    if (noMoreAdsText != null) noMoreAdsText.setVisibility(View.VISIBLE);
                    initSubscriptions();
                }
            }
        } else {
            if (buttonNoAds != null) buttonNoAds.setVisibility(View.GONE);
            if (noMoreAdsText != null) noMoreAdsText.setVisibility(View.GONE);
        }
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

    void setContentViewMargin(int margin) {
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