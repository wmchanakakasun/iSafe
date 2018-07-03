package apps.gligerglg.isafe;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
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

    private ImageButton btn_navigation, btn_neabyIncident, btn_history, btn_profile, btn_settings, btn_appInfo;
    private TripDB tripDB;
    private ConstraintLayout layout;

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
        layout = findViewById(R.id.layout_home);
        btn_navigation = findViewById(R.id.btn_mnu_navigate);
        btn_neabyIncident = findViewById(R.id.btn_mnu_incidents);
        btn_history = findViewById(R.id.btn_mnu_history);
        btn_profile = findViewById(R.id.btn_mnu_profile);
        btn_settings = findViewById(R.id.btn_mnu_settings);
        btn_appInfo = findViewById(R.id.btn_mnu_appInfo);

        tripDB = Room.databaseBuilder(getApplicationContext(),TripDB.class,"TripDB").fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
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

        btn_neabyIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainMenu.this,NearbyMap.class));
            }
        });

        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tripDB.tripDao().getAllTrips().size()==0)
                    setMessage("No Records Found!");
                else
                    startActivity(new Intent(MainMenu.this,DriverHistory.class));
            }
        });
    }

    private void setMessage(String message){
        Snackbar snackbar = Snackbar.make(layout,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

}
