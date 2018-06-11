package apps.gligerglg.isafe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.tapadoo.alerter.Alerter;

import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import rx.functions.Action1;

public class MainMenu extends AppCompatActivity {

    private ImageButton btn_navigation, btn_neabyIncident, btn_history, btn_profile, btn_settings, btn_points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Set Permission
        PermissionsManager.init(this);
        PermissionsManager.get()
                .requestLocationPermission()
                .subscribe(new Action1<PermissionsResult>() {
                    @Override
                    public void call(PermissionsResult permissionsResult) {
                        if (permissionsResult.isGranted()) { // always true pre-M
                            // do whatever
                        }
                        if (permissionsResult.hasAskedForPermissions()) { // false if pre-M
                            // do whatever
                        }
                    }
                });


        initializeConponents();
        initializeMethods();

    }

    private void initializeConponents() {
        btn_navigation = findViewById(R.id.btn_mnu_navigate);
        btn_neabyIncident = findViewById(R.id.btn_mnu_incidents);
        btn_history = findViewById(R.id.btn_mnu_history);
        btn_profile = findViewById(R.id.btn_mnu_profile);
        btn_settings = findViewById(R.id.btn_mnu_settings);
        btn_points = findViewById(R.id.btn_mnu_points);
    }

    private void initializeMethods(){
        btn_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainMenu.this,Navigation.class));
            }
        });

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainMenu.this,SettingsActivity.class));

            }
        });

        btn_points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainMenu.this,PointsActivity.class));
            }
        });
    }

}
