package im.in.github;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.IOException;

public class HTMLDecompile extends Activity {

    private static final int REQ_PICK_HTML = 1234;
    private static final int REQ_SAVE_HTML = 5678;

    private TextView  tvTime;
    private SeekBar   sbTime;
    private CheckBox  cbNoNet;
    private Button    btnStart;

    private int    runSeconds;
    private boolean allowNet;
    private Uri    htmlUri;
    private String htmlContent;
    private WebView webView;
    private AlertDialog progressDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_decompile);
        setTitle("HTML动态解密");

        initViews();
        initListeners();
    }

    private void initViews() {
        tvTime  = (TextView) findViewById(R.id.get_html_run_time);
        sbTime  = (SeekBar)   findViewById(R.id.set_html_run_time);
        cbNoNet = (CheckBox)  findViewById(R.id.set_html_run_can_use_wifi);
        btnStart= (Button)    findViewById(R.id.start_html_run);

        sbTime.setMax(59);
        sbTime.setProgress(4);
        runSeconds = sbTime.getProgress() + 1;
        tvTime.setText("将会动态模拟运行 " + runSeconds + " 秒");

        webView = new WebView(this);
        webView.setVisibility(WebView.GONE);
    }

    private void initListeners() {
        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					runSeconds = progress + 1;
					tvTime.setText("将会动态模拟运行 " + runSeconds + " 秒");
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			});

        btnStart.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickHtmlFile();
				}
			});
    }

    private void pickHtmlFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("text/html");
        startActivityForResult(Intent.createChooser(i, "选择HTML文件"), REQ_PICK_HTML);
    }

    private void saveHtmlFile() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("text/html");
        i.putExtra(Intent.EXTRA_TITLE, "TRav之超级HTML动态解密.html");
        startActivityForResult(i, REQ_SAVE_HTML);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQ_PICK_HTML) {
            htmlUri = data.getData();
            allowNet = !cbNoNet.isChecked();
            startDecrypt();
        } else if (requestCode == REQ_SAVE_HTML) {
            writeHtmlToUri(data.getData());
        }
    }

    private void startDecrypt() {
        progressDlg = new AlertDialog.Builder(this)
			.setTitle("正在解密")
			.setMessage("还需要 " + runSeconds + " 秒，请耐心等待。")
			.setCancelable(false)
			.create();
        progressDlg.show();

        new CountDownTimer(runSeconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                int left = (int) (millisUntilFinished / 1000);
                progressDlg.setMessage("还需要 " + left + " 秒，请耐心等待。");
            }
            @Override
            public void onFinish() {
                progressDlg.dismiss();
                runWebViewAndExtract();
            }
        }.start();
    }

    private void runWebViewAndExtract() {
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        if (!allowNet) {
            ws.setBlockNetworkLoads(true);
            ws.setBlockNetworkImage(true);
        }

        webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					view.evaluateJavascript(
                        "(function(){return document.documentElement.outerHTML;})();",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String html) {
                                htmlContent = html.replace("\\u003C", "<")
									.replace("\\n", "\n")
									.replace("\\\"", "\"");
                                if (htmlContent.startsWith("\"") && htmlContent.endsWith("\"")) {
                                    htmlContent = htmlContent.substring(1, htmlContent.length() - 1);
                                }
                                saveHtmlFile();
                            }
                        });
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
					return false;
				}
			});

        webView.loadUrl(htmlUri.toString());
    }

    private void writeHtmlToUri(Uri targetUri) {
        FileOutputStream fos = null;
        try {
            fos = (FileOutputStream) getContentResolver().openOutputStream(targetUri);
            if (fos == null) throw new IOException("无法打开输出流");
            fos.write(htmlContent.getBytes());
            Toast.makeText(this, "HTML动态解密：\n保存成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "HTML动态解密：\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (fos != null) try { fos.close(); } catch (IOException ignore) {}
        }
    }
}

