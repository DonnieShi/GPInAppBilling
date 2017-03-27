package com.jhonsmith.www.projectdiva;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.example.android.trivialdrivesample.util.SkuDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static final String SKU_0 = "test_product_";
    static final String SUB_0 = "test_sub_";
    static final int RC_REQUEST = 10001;

    private String TAG = "MainActivity";
    private Context mContext;
    private String base64EncodedPublicKey;
    private String mPrice = "";
    private String mCurrency = "";
    // The helper object
    IabHelper mHelper;

    private TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = (TextView) findViewById(R.id.txt);

        findViewById(R.id.btn_query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mHelper.queryInventoryAsync(mGotInventoryListener);
                ArrayList<String> al = new ArrayList<String>();
                al.add(SKU_0);
                al.add(SUB_0);
                mHelper.queryInventoryAsync(true, al, mGotInventoryListener);
            }
        });

        findViewById(R.id.btn_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHelper.launchPurchaseFlow(MainActivity.this, SKU_0, RC_REQUEST,
                        mPurchaseFinishedListener, "");
            }
        });

        findViewById(R.id.btn_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHelper.launchSubscriptionPurchaseFlow(MainActivity.this, SUB_0, RC_REQUEST,
                        mPurchaseFinishedListener, "");
            }
        });

        mContext = this;

        base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmbkVeIoLC3AGE80zZzM+p7AEVhdF+71OLOPRaWKoukGbbP3hIqxqT9F3Rv9fMDWhH63Pum6UgcyxnQEYBfde9XcfgcfRculEaqm09OkzDz487uzCAK/2n3Cg/gDEutoLlpv7pEyVRScoLuSPaU++MEhLxN+50MaDzvSvzNz/COFpRTTr9hrHrqOuRP9CJtBFXt3SNPLmu2eNX54PRN3eENeG8lkV8Ae9QjG/9+UhWkTkkOXgSSUb7RasIxvckbQj2Z0tSE6ptWSGsYaApVSkJtVZy33Iivv5tQVNVAqm66qM6bN4+PX/gM8uD7cfBrlv941iUAEEOFXGKvh6XxOfvQIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished");
                if(!result.isSuccess()){
                    Toast.makeText(mContext, "Problem setting up in-app billing: " + result, Toast.LENGTH_LONG).show();
                    return;
                }

                if (mHelper == null) return;


            }
        });
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            if (mHelper == null) return;

            if (result.isFailure()) {
                Toast.makeText(mContext, "Error purchasing: " + result, Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "Purchase successful.");
            txt.setText("purchase success:"+purchase.getSku()+"-->"+skus.get(purchase.getSku()).get("price")+","+skus.get(purchase.getSku()).get("currency"));
            if (purchase.getItemType().equals(IabHelper.ITEM_TYPE_INAPP)) {
                Log.d(TAG, "Purchase is gas. Starting gas consumption.");
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
            else if (purchase.getItemType().equals(IabHelper.ITEM_TYPE_SUBS)) {
                // bought the infinite gas subscription
                Log.d(TAG, "Infinite gas subscription purchased.");
            }
        }
    };

    Map<String, Map<String, String>> skus = new HashMap<String, Map<String, String>>();

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (mHelper == null) return;

            if (result.isFailure()){
                Toast.makeText(mContext, "Failed to query inventory: " + result, Toast.LENGTH_LONG).show();
                return;
            }

            skus = inventory.getAllSkus();

            String skutxt = "sku:";
            List<SkuDetails> listSkus = new ArrayList<>();
            listSkus = inventory.getAllSkuDetails();
            for (SkuDetails sku : listSkus) {
                skutxt += sku.getSku()+"-->"+sku.getPrice()+","+sku.getCurrency()+"\n";
            }
            String purtxt = "purchase:";
            List<Purchase> listPurchases = new ArrayList<>();
            listPurchases = inventory.getAllPurchases();
            for (Purchase pur : listPurchases) {
                purtxt += pur.getSku()+"\n";
                mHelper.consumeAsync(pur, mConsumeFinishedListener);
            }
            txt.setText(skutxt+purtxt);
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (mHelper == null) return;

            if (result.isSuccess()){
                Toast.makeText(mContext, "Consumption successful. Provisioning." + result, Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(mContext, "Error while consuming: " + result, Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data + ")");
        if (mHelper == null) return;

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {

            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
            String purchaseData = data.getStringExtra(IabHelper.RESPONSE_INAPP_PURCHASE_DATA);
            String dataSignature = data.getStringExtra(IabHelper.RESPONSE_INAPP_SIGNATURE);

            String productId = "null";
            if (purchaseData != null) {
                try {
                    JSONObject o = new JSONObject(purchaseData);
                    productId = o.optString("productId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Log.e(TAG, "purchase:"+productId+"-->"+skus.get(productId).get("price")+","+skus.get(productId).get("currency"));

            /*AppsFlyerLib.getInstance().validateAndTrackInAppPurchase(mContext, base64EncodedPublicKey, dataSignature, purchaseData,
                    "HK$8.00", "HKD", new HashMap<String, String>());*/
        }
    }
}
