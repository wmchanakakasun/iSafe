package apps.gligerglg.isafe;

import android.arch.persistence.room.Room;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.List;

public class DriverHistory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DriverHistoryAdapter adapter;
    private TripDB tripDB;
    private List<Trip> trips;
    private TextView txt_totalDistance, txt_totalTime, txt_totalScore;
    private double distance=0, time = 0;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_history);

        Init();
    }

    private void Init() {
        txt_totalDistance = findViewById(R.id.txt_history_distance);
        txt_totalTime = findViewById(R.id.txt_history_duration);
        txt_totalScore = findViewById(R.id.txt_history_points);

        tripDB = Room.databaseBuilder(getApplicationContext(),TripDB.class,"TripDB").fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        trips = tripDB.tripDao().getAllTrips();
        for(Trip trip : trips){
            distance+=trip.getTotalDistance();
            time += trip.getTotalDuration();
            score += trip.getTotal_score();
        }

        txt_totalDistance.setText("" + MapController.generateSimpleDistanceString(distance));
        txt_totalTime.setText("" + MapController.generateSimpleTimeString(time));
        txt_totalScore.setText(score + " Points");

        recyclerView = findViewById(R.id.history_view);
        layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DriverHistoryAdapter(trips);
        recyclerView.setAdapter(adapter);

    }

}
