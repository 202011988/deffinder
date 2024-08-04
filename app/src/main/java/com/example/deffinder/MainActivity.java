package com.example.deffinder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {


    private static final String API_KEY = "UODxlwVyOlVo%%2BpvFpuAf6Ii7tfKHHLnJ%%2FxSERJGOuFSeOsHZwpcNvkm6t32MxLVSNgC16l4Qf2vi%%2F%%2Fa20bbm1g%%3D%%3D";

    private static final String DEF_SEARCH_URL_FORMAT_1 = "https://api.odcloud.kr/api/uws/v1/inventory?page=1&perPage=100&cond%%5Bname%%3A%%3ALIKE%%5D=" ;
    private static final String DEF_SEARCH_URL_FORMAT_2 = "&cond%%5Bcolor%%3A%%3ALIKE%%5D=";
    private static final String DEF_SEARCH_URL_FORMAT_3 = "&serviceKey=" + API_KEY;


    private static RequestQueue requestQueue;

    private static final String TAG = MainActivity.class.getSimpleName();

    private LayoutInflater layoutInflater;

    private ViewGroup result;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);

        result = findViewById(R.id.resultView);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);

        findViewById(R.id.buttonSearch).setOnClickListener(view -> {
            result.removeAllViews();


            EditText editText = findViewById(R.id.bankName);
            RadioGroup radioGroup = findViewById(R.id.radioStock);
            String stockColor = getStockColor(radioGroup);
            String bankName;
            if ((bankName = editText.getText().toString()) == null || bankName.isEmpty())
                bankName = "";
            search(bankName, stockColor);
        });

        turnProgressBar(false);


    }

    private void turnProgressBar(boolean isOn) {
        progressBar.setVisibility(isOn ? View.VISIBLE : View.GONE);
    }

    private void search (final String bankName, String stockColor){
        turnProgressBar(true);
        String temp = (DEF_SEARCH_URL_FORMAT_1 +  bankName + DEF_SEARCH_URL_FORMAT_2 + stockColor + DEF_SEARCH_URL_FORMAT_3);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,

                Uri.parse(String.format(temp)).toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        processResponse(response, bankName, stockColor);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) { processErrorResponse(); }
                }

        );

        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }



    private void processErrorResponse() {Log.d(TAG, "error");}


    public static String getStockColor (RadioGroup radioGroup){
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioNothing: return "GRAY";
            case R.id.radioFew: return "RED";
            case R.id.radioNormal: return "YELLOW";
            case R.id.radioEnough: return "GREEN";
            default: return "";
        }
    }

    private void processResponse (String response, String bankName, String stockColor) {
        Gson gson = new Gson();
        SearchDEFResult searchDEFResult = gson.fromJson(response, SearchDEFResult.class);

        boolean hasBank = false;

        TextView viewInfo = findViewById(R.id.textInfo);

        String currentCount = searchDEFResult.currentCount;

        if (currentCount.equals("0"))
            viewInfo.setText("검색결과가 없습니다.");
        else
            viewInfo.setText(currentCount + "개의 결과가 있습니다.");


        for (DEF def : searchDEFResult.data) {
            hasBank = true;
            addBank(def.name, def.addr, def.price, def.color, def.inventory, def.tel);
        }

        turnProgressBar(false);
    }

    private void addBank (String bankName, String bankAddress, String bankPrice, String stockColor, String bankStock, String bankNumber) {
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.activity_oilbank_info, result, false);

        TextView viewName = viewGroup.findViewById(R.id.showBankName);
        viewName.setText(bankName);
        TextView viewPrice = viewGroup.findViewById(R.id.showPrice);
        viewPrice.setText(bankPrice);
        TextView viewAddress = viewGroup.findViewById(R.id.showBankAddress);
        viewAddress.setText(bankAddress);
        TextView viewStock = viewGroup.findViewById(R.id.showStock);
        if (bankStock.equals("0"))
            viewStock.setText("없음");
        else
            viewStock.setText("●  " + bankStock);

        switch(stockColor) {
            case "GRAY" : viewStock.setTextColor(Color.parseColor("#444444")); break;
            case "RED" : viewStock.setTextColor(Color.parseColor("#D42316")); break;
            case "YELLOW" : viewStock.setTextColor(Color.parseColor("#DFCB1B")); break;
            case "GREEN" : viewStock.setTextColor(Color.parseColor("#2C8F30")); break;
        }

        Button callButton = viewGroup.findViewById(R.id.buttonCall);

        callButton.setOnClickListener(view -> {
            Intent phoneCall = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + bankNumber));
            startActivity(phoneCall);
        });


        result.addView(viewGroup, 0);

    }

    @Override
    public void onBackPressed() {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("애플리케이션을 종료할까요?");

            builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });

            builder.show();
    }

}