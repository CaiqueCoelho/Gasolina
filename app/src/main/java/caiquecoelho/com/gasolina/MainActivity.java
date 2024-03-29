package caiquecoelho.com.gasolina;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import java.util.Timer;
import java.util.TimerTask;

import caiquecoelho.com.gasolina.helper.ImmersiveModeFragment;

public class MainActivity extends AppCompatActivity {

    private EditText edtGasolina;
    private EditText edtAlcool;
    private Button btnCalcular;
    private ImageView btnAbastecer;
    private TextView btnHistorico;
    private TextView txtErroGasolina;
    private TextView txtErroAlcool;
    private String userId;

    public static final String FRAGTAG = "ImmersiveModeFragment";
    public static final String TAG = "ImmersiveModeFragment";

    public int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fullScreen();

        final View contentView = findViewById(R.id.rootView);
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                contentView.getWindowVisibleDisplayFrame(r);
                int screenHeight = contentView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                Log.d(TAG, "keypadHeight = " + keypadHeight);

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    flag = 1;
                }
                else {
                    fullScreen();
                }
            }
        });

        edtGasolina = (EditText) findViewById(R.id.edtGasolina);
        edtAlcool = (EditText) findViewById(R.id.edtAlcool);
        btnAbastecer = (ImageView) findViewById(R.id.btnAbastecer);
        btnHistorico = (TextView) findViewById(R.id.btnHistorico);
        btnCalcular = (Button) findViewById(R.id.btnCalcular);
        txtErroGasolina = (TextView) findViewById(R.id.txtErroGasolina);
        txtErroAlcool = (TextView) findViewById(R.id.txtErroAlcool);

        SimpleMaskFormatter smfGasolina = new SimpleMaskFormatter("N,NN");
        MaskTextWatcher mtwGasolina = new MaskTextWatcher(edtGasolina, smfGasolina);
        edtGasolina.addTextChangedListener(mtwGasolina);

        SimpleMaskFormatter smfAlcool = new SimpleMaskFormatter("N,NN");
        MaskTextWatcher mtwAlcool = new MaskTextWatcher(edtAlcool , smfAlcool);
        edtAlcool.addTextChangedListener(mtwAlcool);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            userId = extras.getString("user_id");
        }

        btnCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String stringValorGasolina = edtGasolina.getText().toString();
                String stringValorGasolinaSemFormatacao = stringValorGasolina.replace(",", ".");
                String stringValorAlcool = edtAlcool.getText().toString();
                String stringValorAlcoolSemFormatacao = stringValorAlcool.replace(",", ".");

                txtErroAlcool.setText("");
                txtErroGasolina.setText("");

                if(stringValorAlcool.isEmpty() || stringValorGasolina.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Faltou digitar o valor do álcool ou da gasolina", Toast.LENGTH_LONG).show();

                    if (stringValorAlcool.isEmpty()) {
                        txtErroAlcool.setText("Digite o valor");
                    }

                    if (stringValorGasolina.isEmpty()) {
                        txtErroGasolina.setText("Digite o valor");
                    }
                }
                else
                {

                    double doubleValorGasolina = Double.parseDouble(stringValorGasolinaSemFormatacao);
                    double doubleValorAlcool = Double.parseDouble(stringValorAlcoolSemFormatacao);

                    //alcool/gasolina
                    double resultado = doubleValorAlcool / doubleValorGasolina;
                    //Log.i("Alcool", String.valueOf(doubleValorAlcool));
                    //Log.i("Gasolina", String.valueOf(doubleValorGasolina));
                    //Log.i("Resultado", String.valueOf(resultado));

                    if (resultado > 0.7) {
                        //Gasolina
                        AlertDialog.Builder builderDialog = new AlertDialog.Builder(MainActivity.this);
                        builderDialog.setTitle("Gasolina!!!");
                        builderDialog.setMessage("\nÉ melhor abastecer com gasolina!");

                        builderDialog.setIcon(R.drawable.gasolina);

                        builderDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        builderDialog.create();
                        builderDialog.show();

                    } else {
                        //Alcool
                        AlertDialog.Builder builderDialog = new AlertDialog.Builder(MainActivity.this);
                        builderDialog.setTitle("Álcool!!!");
                        builderDialog.setMessage("\nÉ melhor abastecer com álcool!");

                        builderDialog.setIcon(R.drawable.alcool);

                        builderDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        builderDialog.create();
                        builderDialog.show();

                    }

                }
            }
        });

        btnAbastecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AbastecerActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        btnHistorico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoricoActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

    }

    public void fullScreen() {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
