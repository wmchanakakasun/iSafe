package apps.gligerglg.isafe;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class PointsActivity extends AppCompatActivity {

    private TextView txt_earnedPoints, txt_reducedPoints, txt_totalPoints;
    private FloatingTextButton btn_resetPoints;
    private ScoreDB scoreDB;
    private List<Score> scoreList;
    private int earned=00, reduced=00, total=00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        Init();
        updateUI();

        btn_resetPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(PointsActivity.this,R.style.Theme_AppCompat_Dialog_Alert);
                builder.setTitle("Reset All Points");
                builder.setMessage("Do you want to remove your all point earnings?");
                builder.setCancelable(false);
                builder.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        scoreDB.scoreDao().deleteAll();
                        txt_totalPoints.setText("0");
                        txt_reducedPoints.setText("0");
                        txt_earnedPoints.setText("0" );
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    private void updateUI() {

        scoreList = scoreDB.scoreDao().getAllScore();
        for(Score score : scoreList){
            earned += score.getEarned_score();
            reduced = score.getReduced_score();
        }
        total = earned - reduced;

        txt_totalPoints.setText("" + total);
        txt_reducedPoints.setText("" + reduced);
        txt_earnedPoints.setText("" + earned);
    }

    private void Init() {
        txt_earnedPoints = findViewById(R.id.txt_earnedPoints);
        txt_reducedPoints = findViewById(R.id.txt_reducedPoints);
        txt_totalPoints = findViewById(R.id.txt_totalPoints);
        btn_resetPoints = findViewById(R.id.btn_resetPoints);

        scoreDB = Room.databaseBuilder(getApplicationContext(),ScoreDB.class,"ScoreDB").fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }
}
