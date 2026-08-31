package kr.zombi.jarvismobile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int REQ_SPEECH = 5001;
    private static final String PREFS = "jarvis_mobile";
    private static final String KEY_URL = "server_url";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_AUTO = "voice_auto_send";

    private EditText serverEdit;
    private EditText tokenEdit;
    private EditText commandEdit;
    private TextView statusText;
    private TextView resultText;
    private CheckBox autoSendCheck;
    private Button sendButton;
    private Button voiceButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        setContentView(buildUi());
        loadSettings();
    }

    private View buildUi() {
        int pad = dp(16);
        int gap = dp(10);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(17, 24, 32));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView title = text("JARVIS Mobile", 28, Color.WHITE, true);
        root.addView(title);
        TextView sub = text("휴대폰 → PC JARVIS 명령 제어", 14, Color.rgb(170, 190, 205), false);
        root.addView(sub, lpTop(2));

        statusText = text("● 연결 대기", 14, Color.rgb(255, 196, 96), true);
        root.addView(statusText, lpTop(14));

        root.addView(section("PC 연결", gap));
        serverEdit = edit("서버 주소  예: http://192.168.0.10:8765", false);
        root.addView(serverEdit);
        tokenEdit = edit("PC JARVIS 모바일 토큰", true);
        root.addView(tokenEdit, lpTop(8));

        LinearLayout connectRow = horizontal();
        Button save = button("저장");
        save.setOnClickListener(v -> saveSettings(true));
        Button test = button("연결 테스트");
        test.setOnClickListener(v -> testConnection());
        connectRow.addView(save, weight());
        connectRow.addView(space());
        connectRow.addView(test, weight());
        root.addView(connectRow, lpTop(8));

        root.addView(section("명령", gap));
        commandEdit = edit("명령 입력  예: 크롬 열어", false);
        commandEdit.setMinLines(3);
        commandEdit.setGravity(Gravity.TOP | Gravity.START);
        root.addView(commandEdit);

        autoSendCheck = new CheckBox(this);
        autoSendCheck.setText("음성 인식 후 바로 실행");
        autoSendCheck.setTextColor(Color.rgb(220, 228, 234));
        root.addView(autoSendCheck, lpTop(6));

        LinearLayout commandRow = horizontal();
        voiceButton = button("🎙 음성 명령");
        voiceButton.setOnClickListener(v -> startVoiceInput());
        sendButton = button("명령 실행");
        sendButton.setOnClickListener(v -> sendCurrentCommand());
        commandRow.addView(voiceButton, weight());
        commandRow.addView(space());
        commandRow.addView(sendButton, weight());
        root.addView(commandRow, lpTop(6));

        root.addView(section("빠른 명령", gap));
        root.addView(quickRow("PC 상태", "현재 상황 알려줘", "할 일", "할 일 보여줘"));
        root.addView(quickRow("기억", "기억 보여줘", "연동 목록", "연동 목록"), lpTop(8));
        root.addView(quickRow("열린 창", "뭐 켜져 있어", "아침점검", "아침점검"), lpTop(8));

        root.addView(section("실행 결과", gap));
        resultText = text("아직 실행된 명령이 없습니다.", 15, Color.rgb(228, 234, 238), false);
        resultText.setBackgroundColor(Color.rgb(27, 37, 47));
        resultText.setPadding(dp(12), dp(12), dp(12), dp(12));
        resultText.setMinHeight(dp(160));
        resultText.setTextIsSelectable(true);
        root.addView(resultText);

        TextView notice = text("보안: 이 앱을 공인 인터넷에 직접 포트포워딩하지 마세요. 같은 Wi-Fi 또는 Tailscale 같은 사설망 사용을 권장합니다.", 12, Color.rgb(150, 168, 180), false);
        root.addView(notice, lpTop(14));
        return scroll;
    }

    private TextView section(String s, int top) {
        TextView v = text(s, 17, Color.rgb(159, 231, 255), true);
        v.setPadding(0, top, 0, dp(7));
        return v;
    }

    private LinearLayout quickRow(String a, String cmdA, String b, String cmdB) {
        LinearLayout row = horizontal();
        Button ba = button(a);
        ba.setOnClickListener(v -> runQuick(cmdA));
        Button bb = button(b);
        bb.setOnClickListener(v -> runQuick(cmdB));
        row.addView(ba, weight());
        row.addView(space());
        row.addView(bb, weight());
        return row;
    }

    private void runQuick(String command) {
        commandEdit.setText(command);
        commandEdit.setSelection(command.length());
        sendCommand(command);
    }

    private void loadSettings() {
        serverEdit.setText(prefs.getString(KEY_URL, ""));
        tokenEdit.setText(prefs.getString(KEY_TOKEN, ""));
        autoSendCheck.setChecked(prefs.getBoolean(KEY_AUTO, true));
    }

    private void saveSettings(boolean toast) {
        String normalized = normalizeBaseUrl(serverEdit.getText().toString());
        serverEdit.setText(normalized);
        prefs.edit().putString(KEY_URL, normalized).putString(KEY_TOKEN, tokenEdit.getText().toString().trim()).putBoolean(KEY_AUTO, autoSendCheck.isChecked()).apply();
        if (toast) Toast.makeText(this, "JARVIS 연결정보를 저장했습니다.", Toast.LENGTH_SHORT).show();
    }

    private String normalizeBaseUrl(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return "";
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://" + s;
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private void testConnection() {
        saveSettings(false);
        final String base = normalizeBaseUrl(serverEdit.getText().toString());
        final String token = tokenEdit.getText().toString().trim();
        if (!validateConnectionFields(base, token)) return;
        setBusy(true, "연결 확인 중...");
        executor.execute(() -> {
            ApiResult r = request("GET", base + "/api/status", token, null);
            main.post(() -> { setBusy(false, r.ok ? "연결됨" : "연결 실패"); setResult(r.ok, "연결 테스트", r.message); });
        });
    }

    private void sendCurrentCommand() {
        String command = commandEdit.getText().toString().trim();
        if (command.isEmpty()) { Toast.makeText(this, "명령을 입력하세요.", Toast.LENGTH_SHORT).show(); return; }
        sendCommand(command);
    }

    private void sendCommand(String command) {
        saveSettings(false);
        final String base = normalizeBaseUrl(serverEdit.getText().toString());
        final String token = tokenEdit.getText().toString().trim();
        if (!validateConnectionFields(base, token)) return;
        setBusy(true, "명령 실행 중...");
        resultText.setText("JARVIS에게 전송 중...\n\n" + command);
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject(); body.put("command", command);
                ApiResult r = request("POST", base + "/api/command", token, body.toString());
                main.post(() -> { setBusy(false, r.ok ? "연결됨" : "명령 실패"); setResult(r.ok, command, r.message); });
            } catch (Exception e) {
                main.post(() -> { setBusy(false, "명령 실패"); setResult(false, command, "명령 생성 오류: " + e.getMessage()); });
            }
        });
    }

    private boolean validateConnectionFields(String base, String token) {
        if (base.isEmpty()) { statusText.setText("● 서버 주소 필요"); statusText.setTextColor(Color.rgb(255, 110, 110)); Toast.makeText(this, "PC JARVIS의 모바일 서버 주소를 입력하세요.", Toast.LENGTH_LONG).show(); return false; }
        if (token.isEmpty()) { statusText.setText("● 모바일 토큰 필요"); statusText.setTextColor(Color.rgb(255, 110, 110)); Toast.makeText(this, "PC JARVIS 화면의 모바일 토큰을 입력하세요.", Toast.LENGTH_LONG).show(); return false; }
        return true;
    }

    private ApiResult request(String method, String url, String token, String body) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection(); conn.setRequestMethod(method); conn.setConnectTimeout(5000); conn.setReadTimeout(20000); conn.setRequestProperty("Accept", "application/json"); conn.setRequestProperty("X-JARVIS-TOKEN", token);
            if (body != null) { conn.setDoOutput(true); conn.setRequestProperty("Content-Type", "application/json; charset=utf-8"); byte[] bytes = body.getBytes(StandardCharsets.UTF_8); conn.setFixedLengthStreamingMode(bytes.length); try (OutputStream out = conn.getOutputStream()) { out.write(bytes); } }
            int code = conn.getResponseCode(); InputStream stream = code >= 200 && code < 400 ? conn.getInputStream() : conn.getErrorStream(); String text = readAll(stream); boolean transportOk = code >= 200 && code < 300;
            if (text == null || text.trim().isEmpty()) return new ApiResult(transportOk, "HTTP " + code);
            try { JSONObject json = new JSONObject(text); boolean ok = transportOk && json.optBoolean("ok", false); Object result = json.opt("result"); String msg = result instanceof JSONObject ? ((JSONObject) result).toString(2) : (result == null ? text : String.valueOf(result)); return new ApiResult(ok, msg); }
            catch (Exception ignored) { return new ApiResult(transportOk, "HTTP " + code + "\n" + text); }
        } catch (java.net.SocketTimeoutException e) { return new ApiResult(false, "연결 시간 초과. PC JARVIS가 실행 중인지, 같은 네트워크인지 확인하세요."); }
        catch (java.net.ConnectException e) { return new ApiResult(false, "PC JARVIS 서버에 연결할 수 없습니다. 서버 주소와 포트 8765를 확인하세요."); }
        catch (Exception e) { return new ApiResult(false, "연결 오류: " + e.getClass().getSimpleName() + " - " + e.getMessage()); }
        finally { if (conn != null) conn.disconnect(); }
    }

    private String readAll(InputStream input) throws Exception {
        if (input == null) return ""; StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) { String line; while ((line = br.readLine()) != null) sb.append(line).append('\n'); }
        return sb.toString().trim();
    }

    private void startVoiceInput() {
        saveSettings(false); Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ko-KR"); intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS 명령을 말씀하세요"); intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        try { startActivityForResult(intent, REQ_SPEECH); }
        catch (ActivityNotFoundException e) { setResult(false, "음성 명령", "이 휴대폰에 사용 가능한 음성 인식기가 없습니다. Google 음성 인식 서비스를 확인하세요."); }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); if (requestCode != REQ_SPEECH || resultCode != RESULT_OK || data == null) return;
        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); if (results == null || results.isEmpty()) { setResult(false, "음성 명령", "음성을 인식하지 못했습니다."); return; }
        String command = results.get(0).trim(); commandEdit.setText(command); commandEdit.setSelection(command.length()); setResult(true, "음성 인식", command); if (autoSendCheck.isChecked() && !command.isEmpty()) sendCommand(command);
    }

    private void setBusy(boolean busy, String state) { sendButton.setEnabled(!busy); voiceButton.setEnabled(!busy); statusText.setText("● " + state); statusText.setTextColor(busy ? Color.rgb(255, 196, 96) : (state.contains("연결됨") ? Color.rgb(111, 230, 150) : Color.rgb(255, 110, 110))); }
    private void setResult(boolean ok, String command, String message) { String time = new SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(new Date()); resultText.setText("[" + time + "] " + (ok ? "PASS" : "FAIL") + "\n" + command + "\n\n" + message); }
    private TextView text(String s, int sp, int color, boolean bold) { TextView v = new TextView(this); v.setText(s); v.setTextSize(sp); v.setTextColor(color); if (bold) v.setTypeface(v.getTypeface(), android.graphics.Typeface.BOLD); return v; }
    private EditText edit(String hint, boolean password) { EditText e = new EditText(this); e.setHint(hint); e.setHintTextColor(Color.rgb(115, 135, 150)); e.setTextColor(Color.WHITE); e.setTextSize(16); e.setSingleLine(!hint.startsWith("명령")); e.setPadding(dp(12), dp(10), dp(12), dp(10)); e.setBackgroundColor(Color.rgb(27, 37, 47)); if (password) e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); return e; }
    private Button button(String label) { Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(15); b.setTextColor(Color.WHITE); b.setBackgroundColor(Color.rgb(47, 67, 84)); b.setMinHeight(dp(48)); return b; }
    private LinearLayout horizontal() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private LinearLayout.LayoutParams weight() { return new LinearLayout.LayoutParams(0, -2, 1f); }
    private View space() { View v = new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(dp(8), 1)); return v; }
    private LinearLayout.LayoutParams lpTop(int topDp) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.topMargin = dp(topDp); return p; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onDestroy() { executor.shutdownNow(); super.onDestroy(); }
    private static class ApiResult { final boolean ok; final String message; ApiResult(boolean ok, String message) { this.ok = ok; this.message = message == null ? "" : message; } }
}
