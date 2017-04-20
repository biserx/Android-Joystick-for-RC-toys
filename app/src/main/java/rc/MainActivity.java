package rc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashMap;

import rc.devices.R;
import rc.devices.RCAirplane;
import rc.devices.RCBoat;

public class MainActivity extends Activity {

    public static String DEVICE = "device";

    final HashMap<String, Class<?>> devices = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // v Add your implementations here v
        devices.put("RC Boat (ESP8266)", RCBoat.class);
        devices.put("RC Airplane (ESP8266)", RCAirplane.class);
        // ^ Add your implementations here ^
        addButtons();
    }

    private void addButtons() {
        LinearLayout container = (LinearLayout) findViewById(R.id.container);

        for (String deviceName : devices.keySet()) {
            Button btn = new Button(this);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            btn.setText(deviceName);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, JoystickActivity.class);
                    intent.putExtra(DEVICE, devices.get(((Button) view).getText()));
                    startActivity(intent);
                }
            });
            container.addView(btn);
        }
    }


}
