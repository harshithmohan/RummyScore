package com.harshith.score;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    EditText sc[] = new EditText[5];
    EditText names[] = new EditText[5];
    TextView tv[] = new TextView[5], turnCount;
    LinearLayout ll[] = new LinearLayout[5], mainLL, scLL, archiveLL, aLL;
    LinearLayout dl[] = new LinearLayout[5];
    TextView rs[] = new TextView[5];
    int turn=1,players,maxScore;
    boolean newGame=true;
    Intent starterIntent;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        starterIntent = getIntent();
        for(int i=0; i<5; i++) {
            String scID = "sc"+(i+1);
            String tvID = "tv"+(i+1);
            String nameID = "name"+(i+1);
            String layoutID = "ll"+(i+1);
            String dealerID = "dl"+(i+1);
            String rID = "rs"+(i+1);
            sc[i]=findViewById(getResources().getIdentifier(scID, "id", getPackageName()));
            tv[i]=findViewById(getResources().getIdentifier(tvID,"id", getPackageName()));
            names[i]=findViewById(getResources().getIdentifier(nameID, "id", getPackageName()));
            ll[i]=findViewById(getResources().getIdentifier(layoutID, "id", getPackageName()));
            dl[i]=findViewById(getResources().getIdentifier(dealerID, "id", getPackageName()));
            rs[i]=findViewById(getResources().getIdentifier(rID, "id", getPackageName()));
        }
        mainLL = findViewById(R.id.ll);
        scLL = findViewById(R.id.scLL);
        archiveLL = findViewById(R.id.archiveLL);
        aLL = findViewById(R.id.aLL);
        turnCount = findViewById(R.id.turns);
        loadData();
        if(newGame) {
            setMax();
            setPlayers();
        }
        else
            hidePlayers();
    }

    public void saveData() {
        sharedPreferences = getSharedPreferences("Scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i=0; i<5; i++) {
            int score = Integer.parseInt(tv[i].getText().toString());
            String name = names[i].getText().toString();
            String r = rs[i].getText().toString().substring(4);
            String key="Score "+i;
            editor.putInt(key, score);
            key = "Name "+i;
            editor.putString(key, name);
            key = "Rs "+i;
            editor.putString(key,r);
        }
        editor.putInt("turn", turn);
        editor.putInt("players", players);
        editor.putBoolean("newGame", newGame);
        editor.putInt("maxScore", maxScore);
        editor.commit();
    }

    public void loadData() {
        sharedPreferences = getSharedPreferences("Scores", Context.MODE_PRIVATE);
        for(int i=0; i<5; i++) {
            String key = "Score "+i;
            tv[i].setText(Integer.toString(sharedPreferences.getInt(key, 0)));
            key = "Name "+i;
            names[i].setText(sharedPreferences.getString(key, ""));
            key = "Rs "+i;
            rs[i].setText("Rs: "+sharedPreferences.getString(key, "0"));
        }
        turn = sharedPreferences.getInt("turn", 1);
        turnCount.setText("Turn: "+turn);
        players = sharedPreferences.getInt("players", 5);
        newGame = sharedPreferences.getBoolean("newGame", true);
        maxScore = sharedPreferences.getInt("maxScore", 0);
        setDealer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                reset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void bTotal(View V) {
        int o[] = new int[5], n[] = new int[5];
        for(int i=0; i<5; i++) {
            if(sc[i].getText().toString().length()!=0)
                n[i]=Integer.parseInt(sc[i].getText().toString());
            else
                n[i]=0;
            o[i]=Integer.parseInt(tv[i].getText().toString());
            tv[i].setText(""+(n[i]+o[i]));
            if(n[i]==0) {
                int count = Integer.parseInt(rs[i].getText().toString().substring(4));
                rs[i].setText("Rs: " +(count+1));
            }
        }
        turn++;
        turnCount.setText("Turn: "+turn);
        newGame=false;
        saveArchive();
        for(int i=0; i<5; i++)
            sc[i].setText("");
        setDealer();
        checkPlayers();
        saveData();
    }

    public void checkPlayers() {
        for(int i=players-1; i>=0; i--) {
            int currScore = Integer.parseInt(tv[i].getText().toString());
            if(currScore>=maxScore&&i!=players-1) {
                for(int j=i; j<players-1; j++) {
                    names[j].setText(names[j+1].getText());
                    tv[j].setText(tv[j+1].getText());
                    rs[j].setText(rs[j+1].getText());
                }
                players--;
            }
            else if(currScore>=maxScore) {
                players--;
            }
        }
        if(players==1)
            reset();
        hidePlayers();
    }

    public void reset() {
        sharedPreferences = getSharedPreferences("Scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        finish();
        startActivity(starterIntent);
    }

    public void setPlayers() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final EditText nop = new EditText(this);
        nop.setInputType(InputType.TYPE_CLASS_NUMBER);
        adb.setTitle("Players")
            .setMessage("Enter number of players")
            .setView(nop)
            .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(nop.getText().toString().length()!=0) {
                        players = Integer.parseInt(nop.getText().toString());
                        if(players>5)
                            players=5;
                        hidePlayers();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Enter number of players", Toast.LENGTH_SHORT).show();
                        setPlayers();
                    }
                }
            })
            .show();
    }

    public void setMax() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final EditText max = new EditText(this);
        max.setInputType(InputType.TYPE_CLASS_NUMBER);
        adb.setTitle("Maximum score")
            .setMessage("Enter maximum score for the game")
            .setView(max)
            .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(max.getText().toString().length()!=0) {
                        maxScore = Integer.parseInt(max.getText().toString());
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Enter maximum score", Toast.LENGTH_SHORT).show();
                        setMax();
                    }
                }
            })
            .show();
    }

    public void hidePlayers() {
        if(players<2) {
            Toast.makeText(MainActivity.this, "Minimum 2 players required", Toast.LENGTH_SHORT).show();
            setPlayers();
        }
        else {
            for(int i=4; i>=players; i--) {
                ll[i].setVisibility(View.GONE);
                tv[i].setVisibility(View.GONE);
            }
            mainLL.setWeightSum(players);
            scLL.setWeightSum(players);
            aLL.setWeightSum(players);
        }
    }

    public void setDealer() {
        for(int i=0; i<players; i++) {
            int pos = turn%players;
            if(pos==0)
                pos=players;
            if(i+1==pos)
                dl[i].setVisibility(View.VISIBLE);
            else
                dl[i].setVisibility(View.INVISIBLE);
        }
    }

    public void pack(View V) {
        int pos = Integer.parseInt(V.getTag().toString());
        sc[pos-1].setText("25");
    }

    public void saveArchive() {
        LinearLayout rowLL = new LinearLayout(this);
        rowLL.setWeightSum(players);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        param.width=0;
        for(int i=0; i<players; i++) {
            TextView col = new TextView(this);
            String s;
            if(sc[i].getText().toString().length()!=0) {
                if (sc[i].getText().toString().equals("0"))
                    s = "R";
                else
                    s = sc[i].getText().toString();
            }
            else
                s = "R";
            col.setText(s);
            col.setTextSize(20);
            if(s.equals("R"))
                col.setTypeface(null, Typeface.BOLD);
            col.setGravity(Gravity.CENTER);
            col.setLayoutParams(param);
            rowLL.addView(col);
        }
        archiveLL.addView(rowLL);
    }
}
