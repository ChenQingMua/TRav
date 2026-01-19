package im.in.github;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class Application extends Application {

    private static Application sApp;
    private final long INTERVAL = 50L;   
    private volatile boolean mQuit = false;

    public Application() { sApp = this; }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new CheckWorker(), "vpn-watch").start();
    }

    private final class CheckWorker implements Runnable {
        public void run() {
            while (!mQuit) {
                try {
                    if (detectBadEnv()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
								public void run() { toastThenKill(); }
							});
                        return;         
                    }
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException ignore) { return; }
            }
        }
    }

    private boolean detectBadEnv() {
        /* 1. VPN */
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            android.net.Network net = cm.getActiveNetwork();
            if (net != null) {
                NetworkCapabilities cap = cm.getNetworkCapabilities(net);
                if (cap != null && cap.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    return true;
            }
        } catch (Throwable ignore) {}

        String host = System.getProperty("http.proxyHost");
        if (host != null && host.trim().length() > 0) return true;

        try {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("AndroidCAStore");
            ks.load(null, null);
            java.util.Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                java.security.cert.Certificate cert = ks.getCertificate(alias);
                if (cert instanceof java.security.cert.X509Certificate) {
                    String name = ((java.security.cert.X509Certificate) cert)
						.getSubjectDN().getName().toLowerCase(java.util.Locale.US);
                    if (name.contains("charles")  ||
                        name.contains("fiddler")  ||
                        name.contains("burp")     ||
                        name.contains("mitm")     ||
                        name.contains("proxyman") ||
                        name.contains("whistle")) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignore) {}

        return false;   
    }

    private void toastThenKill() {
        Toast.makeText(sApp, "环境异常", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
				public void run() { System.exit(0); }
			},50);
    }
}

