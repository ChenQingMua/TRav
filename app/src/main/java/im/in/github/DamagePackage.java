package im.in.github;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DamagePackage extends Activity {

    private static final int REQ_PICK_APK = 1111;
    private static final int REQ_SAVE_APK = 2222;

    private Uri srcApkUri;
    private File workDir;
    private File damagedApk;   
    private AlertDialog waitDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_package);
        setTitle("一键损坏应用包");

        Button btn = (Button) findViewById(R.id.start_damage_package);
        btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickApk();
				}
			});
    }

    private void pickApk() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("application/vnd.android.package-archive");
        startActivityForResult(Intent.createChooser(i, "选择APK"), REQ_PICK_APK);
    }

    private void saveApk() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/vnd.android.package-archive");
        i.putExtra(Intent.EXTRA_TITLE, "damaged.apk");
        startActivityForResult(i, REQ_SAVE_APK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;

        if (requestCode == REQ_PICK_APK) {
            srcApkUri = uri;
            new DamageThread().start();
        } else if (requestCode == REQ_SAVE_APK) {
            copyToUri(damagedApk, uri);   
        }
    }

    private class DamageThread extends Thread {
        @Override
        public void run() {
            try {
                runOnUiThread(new Runnable() {
						@Override
						public void run() {
							waitDlg = new AlertDialog.Builder(DamagePackage.this)
                                .setTitle("正在处理")
                                .setMessage("任务处理中，请稍等。")
                                .setCancelable(false)
                                .create();
							waitDlg.show();
						}
					});

                workDir = new File(getCacheDir(), "damage_" + System.currentTimeMillis());
                if (!workDir.mkdirs()) throw new IOException("mkdir fail");

                final File srcFile = new File(workDir, "src.apk");
                copyUriToFile(srcApkUri, srcFile);

                damagedApk = new File(workDir, "damaged.apk");
                processApk(srcFile, damagedApk);   

                runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (waitDlg != null) waitDlg.dismiss();
							saveApk();   
						}
					});
            } catch (Exception e) {
                Log.e("Damage", "error", e);
                toast("一键损坏应用包：\n处理失败：" + e.getMessage());
                runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (waitDlg != null) waitDlg.dismiss();
						}
					});
            }
        }
    }

    private void processApk(File srcApk, File outApk) throws IOException {
        ZipFile zin = new ZipFile(srcApk);
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outApk));
        byte[] buf = new byte[8192];
        Enumeration<? extends ZipEntry> en = zin.entries();
        while (en.hasMoreElements()) {
            ZipEntry entry = en.nextElement();
            String name = entry.getName();
            if (entry.isDirectory()) {
                zout.putNextEntry(new ZipEntry(name));
                zout.closeEntry();
                continue;
            }
            InputStream is = zin.getInputStream(entry);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int n;
            while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
            is.close();
            byte[] data = baos.toByteArray();

            if (name.endsWith(".dex") || name.endsWith(".so")) {
                int cut = data.length / 10;
                if (cut == 0) cut = 1;
                byte[] nee = new byte[data.length - cut];
                System.arraycopy(data, 0, nee, 0, nee.length);
                data = nee;
            }

            ZipEntry ne = new ZipEntry(name);
            zout.putNextEntry(ne);
            zout.write(data);
            zout.closeEntry();
        }
        zin.close();
        zout.close();
    }

    private void copyToUri(File src, Uri uri) {
        FileInputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = getContentResolver().openOutputStream(uri);
            if (out == null) throw new IOException("openOutputStream null");
            byte[] b = new byte[8192];
            int n;
            while ((n = in.read(b)) != -1) out.write(b, 0, n);
            toast("一键损坏应用包：\n保存成功，你还需要手动签名，才能使用，推荐使用MT管理器。");
        } catch (IOException e) {
            toast("一键损坏应用包：\n保存失败：" + e.getMessage());
        } finally {
            try { if (in != null) in.close(); } catch (IOException ignore) {}
            try { if (out != null) out.close(); } catch (IOException ignore) {}
        }
    }

    private void copyUriToFile(Uri uri, File dst) throws IOException {
        InputStream in = getContentResolver().openInputStream(uri);
        FileOutputStream out = new FileOutputStream(dst);
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) != -1) out.write(b, 0, n);
        in.close(); out.close();
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(DamagePackage.this, msg, Toast.LENGTH_SHORT).show();
				}
			});
    }
}

