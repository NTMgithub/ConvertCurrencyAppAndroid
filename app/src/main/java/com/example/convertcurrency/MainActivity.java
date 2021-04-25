package com.example.convertcurrency;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Documented;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ProgressDialog pDialog;
    Button buttonDoiTien, buttonDoiVitri;
    EditText soTienDoi, et_ketQua, et_LichSuDoi;
    Spinner spinnerCurrencyDau, spinnerCurrencyDich;
    ArrayList<String> arrayCurrency = new ArrayList<>();
    static String donViTienDau_Static = "";
    static String donViTienDich_Static = "";

    StringBuilder txtLichSu = new StringBuilder();

    //Regex để check có phải nhập vào số dạng double hay không
    String DOUBLE_PATTERN = "[0-9]+(\\.){0,1}[0-9]*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new ReadRSS().execute("https://aud.fxexchangerate.com/rss.xml");

        DoiTien();

        HoanDoiViTri();
    }

    //Dùng cho doInBackground
    public String ConnectToURL(String linkURL){
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(linkURL);
            InputStreamReader inputStreamReader = new InputStreamReader(url.openConnection().getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = "";

            while ( (line = bufferedReader.readLine()) != null ){
                content.append(line);
            }

            bufferedReader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private class ReadRSS extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // display a progress dialog for good user experiance
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Đang lấy dữ liệu...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            return ConnectToURL(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Tắt progress dialog ở onPreExcute
            pDialog.dismiss();

            XMLDOMParser parser = new XMLDOMParser();
            Document document = parser.getDocument(s);
            NodeList nodeList = document.getElementsByTagName("item");

            String[] title;

            for (int i = 0 ; i < nodeList.getLength(); i++){
                Element element = (Element) nodeList.item(i);
                //Cắt chuỗi title để lấy tên các loại tiên, sau đó add vào ArrayList
                title = parser.getValue(element, "title").split("/");

                arrayCurrency.add(title[1]);
            }

            spinnerCurrencyDau = (Spinner) findViewById(R.id.spinner_TienDau);
            spinnerCurrencyDich = (Spinner) findViewById(R.id.spinner_TienDich);

            //Set các loại tiền vào spinner
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, arrayCurrency);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
            spinnerCurrencyDau.setAdapter(arrayAdapter);
            spinnerCurrencyDich.setAdapter(arrayAdapter);

        }

    }

    public void DoiTien(){
        buttonDoiTien = findViewById(R.id.btn_DoiTien);

        buttonDoiTien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueCurrencySpinnerDau = spinnerCurrencyDau.getSelectedItem().toString();
                String valueCurrencySpinnerDich = spinnerCurrencyDich.getSelectedItem().toString();

                //Dùng hàm hàm split regex để cắt chuỗi, lấy đơn vị tiền
                String[] valueCurrencyDau = valueCurrencySpinnerDau.split("[\\\\(||\\\\)]" );
                String[] valueCurrencyDich = valueCurrencySpinnerDich.split("[\\\\(||\\\\)]" );

                //Link rss chỉ nhận lowercase nên lowerCase đơn vị tiền để gán vào link
                String donViTienDau = valueCurrencyDau[1].toLowerCase();
                String donViTienDich = valueCurrencyDich[1].toLowerCase();

                donViTienDau_Static = valueCurrencyDau[1];
                donViTienDich_Static = valueCurrencyDich[1];

                if (donViTienDau.equals(donViTienDich)){
                    soTienDoi = findViewById(R.id.input_TienDau);
                    et_ketQua = findViewById(R.id.input_TienDich);
                    et_LichSuDoi = findViewById(R.id.input_LichSuDoi);

                    if (Pattern.matches(DOUBLE_PATTERN, soTienDoi.getText().toString()) ){
                        et_ketQua.setText(soTienDoi.getText().toString());

                        txtLichSu.append(soTienDoi.getText().toString() + " " + donViTienDau_Static + " = "
                                + soTienDoi.getText().toString() + " " +donViTienDich_Static + "\n");

                        et_LichSuDoi.setText(txtLichSu);

                    }else{
                        Toast.makeText(MainActivity.this, "Vui lòng chỉ nhập số!", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    String urlRSS = "https://"+ donViTienDau +".fxexchangerate.com/"+ donViTienDich +".xml";
                    new ConvertCurrencyRSS().execute(urlRSS);
                }
            }
        });


    }

    private class ConvertCurrencyRSS extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // display a progress dialog for good user experiance
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Đang lấy dữ liệu...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            return ConnectToURL(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Tắt progress dialog ở onPreExcute
            pDialog.dismiss();

            XMLDOMParser parser = new XMLDOMParser();
            Document document = parser.getDocument(s);
            NodeList nodeList = document.getElementsByTagName("item");
            Element element = (Element) nodeList.item(0);

            //Vì tag description dạng CDATA, nên phải gán thêm 1 nodeList
            NodeList nodeListDescription = element.getElementsByTagName("description");
            Element elementDescription = (Element) nodeListDescription.item(0);

            String description = getCharacterDataFromElement(elementDescription);

            //Xử lý chuỗi cdata để lấy tỷ giá cuối cùng
            String[] tyGiaDich = description.split("=");
            String[] tyGiaDich2 = tyGiaDich[1].split("<");
            String[] tyGiaDich3 = tyGiaDich2[0].split("\\s");

            soTienDoi = findViewById(R.id.input_TienDau);
            et_ketQua = findViewById(R.id.input_TienDich);

            //Kiểm tra input và thực hiện tích kết quả
            if (Pattern.matches(DOUBLE_PATTERN, soTienDoi.getText().toString()) ){
                double soTienDoiFinal = Double.parseDouble(soTienDoi.getText().toString());
                double tyGiaDichFinal = Double.parseDouble(tyGiaDich3[1]);

                //Làm tròn đến 3 chữ số thập phân
                double ketQuaDoi = (double) Math.round(soTienDoiFinal * tyGiaDichFinal * 1000) / 1000;

                et_ketQua.setText(Double.toString(ketQuaDoi));


                txtLichSu.append(soTienDoi.getText().toString() + " " + donViTienDau_Static + " = "
                        + Double.toString(ketQuaDoi) + " " + donViTienDich_Static + "\n");

                et_LichSuDoi.setText(txtLichSu);

            }else{
                Toast.makeText(MainActivity.this, "Vui lòng chỉ nhập số!", Toast.LENGTH_SHORT).show();
            }
        }


    }

    //Hàm lấy CDATA
    public String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    public void HoanDoiViTri(){
        spinnerCurrencyDau = (Spinner) findViewById(R.id.spinner_TienDau);
        spinnerCurrencyDich = (Spinner) findViewById(R.id.spinner_TienDich);
        buttonDoiVitri = (Button) findViewById(R.id.btn_DoiViTri);

        buttonDoiVitri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lấy vị trí spinner
                int viTriSpinnerDau = spinnerCurrencyDau.getSelectedItemPosition();
                int viTriSpinnerDich = spinnerCurrencyDich.getSelectedItemPosition();

                //Thực hiện hoán đổi, spinner dau = spinner dich và ngược lại
                spinnerCurrencyDau.setSelection(viTriSpinnerDich);
                spinnerCurrencyDich.setSelection(viTriSpinnerDau);
            }
        });


    }



}