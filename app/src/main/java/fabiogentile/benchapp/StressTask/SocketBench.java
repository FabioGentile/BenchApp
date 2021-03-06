package fabiogentile.benchapp.StressTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fabiogentile.benchapp.CallbackInterfaces.MainActivityI;
import fabiogentile.benchapp.Util.CpuManager;
import fabiogentile.benchapp.Util.SocketTypeEnum;


public class SocketBench extends AsyncTask<String, Void, Void> {
    private final String TAG = "SocketBench";
    private MainActivityI listener;
    private Object syncToken;
    private String serverIp;
    private int serverPort;
    private int startRate;
    private int endRate;
    private int msPerRate;
    private int payloadSize;
    private boolean waitLcdOff = true;
    private CpuManager cpuManager = CpuManager.getInstance();

    public SocketBench(MainActivityI listener, Object token, SharedPreferences prefs, SocketTypeEnum type) {
        this.listener = listener;
        this.syncToken = token;

        if (type == SocketTypeEnum.WIFI) {
            this.serverIp = prefs.getString("wifi_ip_address", "127.0.0.1");
            this.serverPort = Integer.parseInt(prefs.getString("wifi_server_port", "29000"));
            this.startRate = Integer.parseInt(prefs.getString("wifi_start_rate", "5"));
            this.endRate = Integer.parseInt(prefs.getString("wifi_end_rate", "20"));
            this.msPerRate = Integer.parseInt(prefs.getString("wifi_ms_per_rate", "5000"));
            this.payloadSize = Integer.parseInt(prefs.getString("wifi_payload_size", "256"));

        } else {
            this.serverIp = prefs.getString("threeG_ip_address", "127.0.0.1");
            this.serverPort = Integer.parseInt(prefs.getString("threeG_server_port", "8080"));
            this.startRate = Integer.parseInt(prefs.getString("threeG_start_rate", "5"));
            this.endRate = Integer.parseInt(prefs.getString("threeG_end_rate", "10"));
            this.msPerRate = Integer.parseInt(prefs.getString("threeG_ms_per_rate", "5000"));
            this.payloadSize = Integer.parseInt(prefs.getString("threeG_payload_size", "256"));
        }

        this.waitLcdOff = prefs.getBoolean("general_turn_off_monitor", true);
        this.cpuManager.setPreferences(prefs);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            //Wait for screen to turn off
            if (syncToken != null && waitLcdOff)
                synchronized (syncToken) {
                    try {
                        syncToken.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            String netInterface = params[0];
            cpuManager.marker();


            //USAGE: IP PORT START_RATE END_RATE MS_PER_RATE PAYLOAD_SIZE INTERFACE
            String cmd = "su -c /system/xbin/SocketBench "
                    + this.serverIp + " "
                    + this.serverPort + " "
                    + this.startRate + " "
                    + this.endRate + " "
                    + this.msPerRate + " "
                    + this.payloadSize + " "
                    + netInterface;
            Log.i(TAG, "doInBackground: start script " + cmd);
            Process su = Runtime.getRuntime().exec(cmd);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(su.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.i(TAG, "doInBackground: " + line);
            }

            su.waitFor();
            Log.i(TAG, "doInBackground: script ended");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {
        listener.WiFiTaskCompleted();
    }

}
