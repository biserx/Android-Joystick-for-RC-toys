package rc.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import rc.devices.R;
import rc.devices.RCBoatClient;
import rc.joystick.JoystickActivity;

public class MainActivity extends Activity {

    public static String communicationModule = "commModule";
    Button btnRCBoat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRCBoat = (Button) findViewById(R.id.btnRCBoat);
        btnRCBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, JoystickActivity.class);
                intent.putExtra(communicationModule, "rc.devices.RCBoatClient");
                startActivity(intent);
            }
        });


    }


}
