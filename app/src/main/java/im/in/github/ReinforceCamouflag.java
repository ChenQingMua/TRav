package im.in.github;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ReinforceCamouflag extends Activity {

    private static final int REQ_PICK_APK = 1111;
    private static final int REQ_SAVE_APK = 2222;

    private final String[] CN_NAMES = {
            "360加固企业版","360加固普通版",
            "腾讯加固企业版","腾讯加固普通版","网易易盾加固",
            "Arm加固","Google加固","ShadowSafety加固",
            "阿里加固","爱加密企业版","爱加密普通版",
            "百度加固","梆梆加固普通版","梆梆加固企业版",
            "顶象加固","几维加固","启明星辰加固",
            "珊瑚灵御加固","新百度加固","中国移动加固"
    };
    private final String[] ZIP_FILES = {
            "pro_360qyb.zip","pro_360jg.zip",
            "pro_txqyb.zip","pro_txjg.zip","pro_wyyd.zip",
            "pro_armjg.zip","pro_googlejg.zip","pro_shadowsafety.zip",
            "pro_aljg.zip","pro_ajgqyb.zip","pro_ajg.zip",
            "pro_bdjg.zip","pro_bbjg.zip","pro_bbjgqyb.zip",
            "pro_dxjg.zip","pro_jwjg.zip","pro_qmxc.zip",
            "pro_shly.zip","pro_xbdjg.zip","pro_zgydjg.zip"
    };

    private RadioGroup radioGroup;
    private CheckBox   dexCheck;  
    private File       proDir;  

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reinforce_camouflage);
        setTitle("特征伪加固");

        LinearLayout container = (LinearLayout) findViewById(R.id.set_pro_option);
        radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        radioGroup.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < CN_NAMES.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(CN_NAMES[i]);
            rb.setId(i);
            rb.setLayoutParams(new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT));
            radioGroup.addView(rb);
        }
        radioGroup.check(0);
        container.removeAllViews();
        container.addView(radioGroup);


        Button startBtn = (Button) findViewById(R.id.start_pro);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickApk();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    unzipProOnce();
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toast("解压失败：" + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void unzipProOnce() throws IOException {
        File cacheDir = getCacheDir();
        proDir = new File(cacheDir, "pro");
        if (proDir.exists()) {
            
            return;
        }
        InputStream in = null;
        ZipInputStream zin = null;
        try {
            in = getAssets().open("ReinforceCamouflag/pro.zip");
            zin = new ZipInputStream(in);
            ZipEntry entry;
            int cnt = 0;
            while ((entry = zin.getNextEntry()) != null) {
                File outFile = new File(proDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                outFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buf = new byte[8192];
                int n;
                while ((n = zin.read(buf)) != -1) fos.write(buf, 0, n);
                fos.close();
                zin.closeEntry();
                cnt++;
            }
        } finally {
            if (zin != null) zin.close();
            if (in != null) in.close();
        }
    }

    private void pickApk() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("application/vnd.android.package-archive");
        startActivityForResult(Intent.createChooser(i, "选择APK"), REQ_PICK_APK);
    }

    private void saveApk() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/vnd.android.package-archive");
        i.putExtra(Intent.EXTRA_TITLE, "camouflage.apk");
        startActivityForResult(i, REQ_SAVE_APK);
    }

    private File resultApk;  

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;

        if (requestCode == REQ_PICK_APK) {
            new ProcessThread(uri).start();
        } else if (requestCode == REQ_SAVE_APK) {
            try {
				copyApkToUri(resultApk, uri);
			} catch (IOException e) {}
        }
    }

    private class ProcessThread extends Thread {
        private final Uri srcUri;
        ProcessThread(Uri uri) { this.srcUri = uri; }

        @Override
        public void run() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog dlg = new AlertDialog.Builder(ReinforceCamouflag.this)
                                .setTitle("正在处理")
                                .setMessage("正在注入伪加固特征，请稍后。")
                                .setCancelable(false)
                                .create();
                        dlg.show();
                        ReinforceCamouflag.this.waitDlg = dlg;
                    }
                });

                File workDir = new File(getCacheDir(), "camou_" + System.currentTimeMillis());
                if (!workDir.mkdirs()) throw new IOException("mkdir fail");

                File srcApk = new File(workDir, "src.apk");
               
                InputStream in = getContentResolver().openInputStream(srcUri);
                FileOutputStream out = new FileOutputStream(srcApk);
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) != -1) out.write(b, 0, n);
                in.close(); out.close();

                int selected = radioGroup.getCheckedRadioButtonId();   
                File zipFile = new File(proDir, ZIP_FILES[selected]); 

                resultApk = new File(workDir, "result.apk");
                copyAssetsOnly(srcApk, zipFile, resultApk);   

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (waitDlg != null) waitDlg.dismiss();
                        saveApk();
                    }
                });
            } catch (final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (waitDlg != null) waitDlg.dismiss();
                        toast("特征伪加固：\n处理失败：" + e.getMessage());
                    }
                });
            }
        }
    }

    private AlertDialog waitDlg;

 
    private void copyAssetsOnly(File srcApk, File zipFile, File outApk) throws IOException {
        long now = System.currentTimeMillis(); 

        File srcDir = new File(srcApk.getParent(), "src");
        ZipFile zf = new ZipFile(srcApk);
        Enumeration<? extends ZipEntry> en = zf.entries();
        while (en.hasMoreElements()) {
            ZipEntry e = en.nextElement();
            File outFile = new File(srcDir, e.getName());
            if (e.isDirectory()) {
                outFile.mkdirs();
                continue;
            }
            outFile.getParentFile().mkdirs();
            InputStream in = zf.getInputStream(e);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) fos.write(buf, 0, n);
            in.close(); fos.close();
        }
        zf.close();

        File proDir = new File(srcApk.getParent(), "pro");
        ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            File outFile = new File(proDir, entry.getName());
            if (entry.isDirectory()) {
                outFile.mkdirs();
                continue;
            }
            outFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buf = new byte[8192];
            int n;
            while ((n = zin.read(buf)) != -1) fos.write(buf, 0, n);
            fos.close();
            zin.closeEntry();
        }
        zin.close();

        
        File proAssets = new File(proDir, "assets");
        File srcAssets = new File(srcDir, "assets");
        if (proAssets.exists()) {
            int cnt = 0;
            File[] files = proAssets.listFiles();
            if (files != null) {
                for (File f : files) {
                    File target = new File(srcAssets, f.getName());
                    if (f.isDirectory()) {
                        copyDirInline(f, target);
                        continue;
                    }
                    InputStream in = new FileInputStream(f);
                    FileOutputStream out = new FileOutputStream(target);
                    byte[] b = new byte[8192];
                    int n;
                    while ((n = in.read(b)) != -1) out.write(b, 0, n);
                    in.close(); out.close();
                    cnt++;
                }
            }
        } else {
        }

        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outApk));
     
        zipDirInline(srcDir, "", zout, now);
        zout.close();
        long sizeKB = outApk.length() / 1024;
        toast("特征伪加固：\n打包完成，大小：" + sizeKB + " 千字节。");
    }

    private int copyDirInline(File src, File dst) throws IOException {
        if (!dst.exists()) dst.mkdirs();
        File[] files = src.listFiles();
        int cnt = 0;
        if (files == null) return 0;
        for (File f : files) {
            File target = new File(dst, f.getName());
            if (f.isDirectory()) {
                cnt += copyDirInline(f, target);
                continue;
            }
            InputStream in = new FileInputStream(f);
            FileOutputStream out = new FileOutputStream(target);
            byte[] b = new byte[8192];
            int n;
            while ((n = in.read(b)) != -1) out.write(b, 0, n);
            in.close(); out.close();
            cnt++;
        }
        return cnt;
    }

    private void zipDirInline(File dir, String base, ZipOutputStream zout, long now) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String entryName = base + f.getName();
            if (f.isDirectory()) {
                zipDirInline(f, entryName + "/", zout, now);
                continue;
            }
            ZipEntry ze = new ZipEntry(entryName);
            ze.setTime(now);   
            zout.putNextEntry(ze);
            FileInputStream in = new FileInputStream(f);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) zout.write(buf, 0, n);
            in.close();
            zout.closeEntry();
        }
    }

    private void copyApkToUri(File src, Uri uri) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = getContentResolver().openOutputStream(uri);
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) != -1) out.write(b, 0, n);
        in.close(); out.close();
        toast("特征伪加固：\n保存成功，你可能需要工具重签该应用，以正常使用，推荐使用MT管理器。");
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ReinforceCamouflag.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
