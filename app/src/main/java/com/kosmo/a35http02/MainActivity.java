package com.kosmo.a35http02;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    String TAG = "KOSMO61";

    //전역변수
    EditText user_id, user_pw;
    TextView textResult;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위젯얻어오기
        textResult = (TextView)findViewById(R.id.text_result);
        user_id = (EditText)findViewById(R.id.user_id);
        user_pw = (EditText)findViewById(R.id.user_pw);
        Button btnLogin = (Button)findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //http://IP주소:서버포트변호/경로명

                /*
                execute()메소드를 통해 doInBackground()메소드를 호출한다.
                이때 전달하는 파라미터는 총 3가지로 첫번째는 요청 URL,
                두번째와 세번째는 서버로 전송할 파라미터 임
                 */
                new AsyncHttpServer().execute
                        ("http://192.168.219.200:8282/k12springapi/android/memberLogin.do",
                                "id=" + user_id.getText().toString(), "pass=" + user_pw.getText().toString());
            }
        });

        //진행대화창을 띄울 준빌를 함
        dialog = new ProgressDialog(this);
        //Back버튼을 누를 때 창이 닫히게 설정
        dialog.setCancelable(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIcon(android.R.drawable.ic_dialog_email);
        dialog.setTitle("로그인 처리중");
        dialog.setMessage("서버로부터 응답을 기다리고 있습니다.");
    }////onCreate End

    class AsyncHttpServer extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //응답대기 대화창을 띄워줌
            if(!dialog.isShowing()) dialog.show();
        }//// onPreExecute End

        /*
        execute()를 통해 전달한 3개의 파라미터를 가변인자를 통해 전달받는다.
        해당 값은 배열 형태로 사용하게 된다.
         */
        @Override
        protected String doInBackground(String... strings) {

            StringBuffer receiveData = new StringBuffer();

            try {
                //파라미터 1 : 요청 URL
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStream out = conn.getOutputStream();
                //파라미터 2 : 사용자아이디
                out.write(strings[1].getBytes());
                //&를 사용하여 쿼리스트링 형태로 만들어준다.
                out.write("&".getBytes());
                //파라미터 3 : 사용자패스워드
                out.write(strings[2].getBytes());
                out.flush();
                out.close();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //스프링 서버에 연결성공한 경우 JSON 데이터를 읽어서 저장한다.
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String data;
                    while ((data = reader.readLine()) != null) {
                        receiveData.append(data + "\r\n");
                    }
                    reader.close();
                }
                else {
                    Log.i(TAG, "HTTP_OK 안됨. 연결실패");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //로그출력
            Log.i(TAG, receiveData.toString());

            //서버에서 내려준 JSON정보를 저장 후 반환
            return receiveData.toString();
        }//// doInBackground End

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }//// onProgressUpdate End

        /*
        doInBackground()에서 반환된 값은 해당 함수로 전달된다.
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            StringBuffer sb = new StringBuffer();
            try {
                /*
                {
                "isLogin":1,
                "memberInfo":{"id":"kosmo","pass":"1111","name":"코스모테스트","regidate":"2020-05-26"}
                }
                서버에서 읽어온 JSON을 파싱한다.
                 */
                JSONObject jsonObject = new JSONObject(s);
                int success = Integer.parseInt(jsonObject.getString("isLogin"));

                //파싱 후 로그인 성공인 경우
                if(success == 1) {
                    sb.append("로그인 성공!\n");

                    String id = jsonObject.getJSONObject("memberInfo").getString("id").toString();
                    String pass = jsonObject.getJSONObject("memberInfo").getString("pass").toString();
                    String name = jsonObject.getJSONObject("memberInfo").getString("name").toString();

                    sb.append("회원정보\n");
                    sb.append("아이디 : " + id + "\n");
                    sb.append("패스워드 : " + pass + "\n");
                    sb.append("이름 : " + name + "\n");
                }
                else {
                    sb.append("로그인 실패");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //결과출력
            dialog.dismiss();
            textResult.setText(sb.toString());
            Toast.makeText(getApplicationContext(),
                    sb.toString(),
                    Toast.LENGTH_LONG).show();
        }//// onPostExecute End
    }//// AsyncHttpServer End
}


























