package caiquecoelho.com.gasolina;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import caiquecoelho.com.gasolina.R;
import caiquecoelho.com.gasolina.model.Carro;

public class CarroActivity extends AppCompatActivity {

    private EditText edtMarca;
    private EditText edtApelido;
    private Button btnSalvar;
    private SQLiteDatabase bancoDeDados;
    private TextView txtErroMarca;

    public static final String TAG = "ImmersiveModeFragment";

    public int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carro);

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

        setTitle("Cadastrar Carro");

        criandoOuAbrindoBancoDeDados();

        edtMarca = (EditText) findViewById(R.id.edtMarca);
        edtApelido = (EditText) findViewById(R.id.edtApelido);
        btnSalvar = (Button) findViewById(R.id.btnSalvar);
        txtErroMarca = (TextView) findViewById(R.id.txtErroMarca);
        Bundle extras = getIntent().getExtras();

        final String posto = extras.getString("posto");
        final String preco = extras.getString("preco");
        final String quantidade = extras.getString("quantidade");
        final String tipo = extras.getString("tipo");
        final String real = extras.getString("real");
        final String kms = extras.getString("kms");
        final String position = extras.getString("position");
        final String date = extras.getString("date");
        final String timestamp = extras.getString("timestamp");

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtMarca.getText().toString().isEmpty()){
                    txtErroMarca.setText("Digite a marca e o modelo");
                }
                else {
                    Carro carro = new Carro();
                    carro.setMarca(edtMarca.getText().toString());
                    if (!edtApelido.getText().toString().isEmpty()) {
                        carro.setApelido(edtApelido.getText().toString());
                    }
                    salvando(carro);
                    Intent intentAbastecimento = new Intent(CarroActivity.this, AbastecerActivity.class);
                    intentAbastecimento.putExtra("posto", posto);
                    intentAbastecimento.putExtra("preco", preco);
                    intentAbastecimento.putExtra("quantidade", quantidade);
                    intentAbastecimento.putExtra("tipo", tipo);
                    intentAbastecimento.putExtra("real", real);
                    intentAbastecimento.putExtra("kms", kms);
                    intentAbastecimento.putExtra("position", position);
                    intentAbastecimento.putExtra("date", date);
                    intentAbastecimento.putExtra("timestamp", timestamp);
                    if (extras.getString("edit") != null) {
                        Log.i("update data 1", extras.getString("edit"));
                        intentAbastecimento.putExtra("edit", extras.getString("edit"));
                    }
                    if (extras.getString("idFuelling") != null) {
                        Log.i("idFuelling", extras.getString("idFuelling"));
                        intentAbastecimento.putExtra("idFuelling", extras.getString("idFuelling"));
                    }
                    // intentAbastecimento.putExtra("car_activity", 1);

                    Bundle extras = getIntent().getExtras();
                    if(extras != null && extras.getString("user_id") != null) {
                        intentAbastecimento.putExtra("user_id", extras.getString("user_id"));
                    }
                    intentAbastecimento.putExtra("carro", carro.getApelido());
                    AbastecerActivity.activityAbastecer.finish();
                    startActivity(intentAbastecimento);
                    finish();
                }
            }
        });
    }

    public void criandoOuAbrindoBancoDeDados(){

        try {
            bancoDeDados = openOrCreateDatabase("app_gasolina", MODE_PRIVATE, null);

            bancoDeDados.execSQL("CREATE TABLE IF NOT EXISTS carros(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "marca VARCHAR, apelido VARCHAR)");
        }catch (Exception e){
            e.printStackTrace();
            Log.i("Erro ", "ao criar tabela carros: "+e.toString());
        }

    }

    public void salvando(Carro carro) {

        try {
            bancoDeDados.execSQL("INSERT INTO carros(marca, apelido) VALUES ('" + carro.getMarca() + "', '" + carro.getApelido() + "')");

            Toast.makeText(CarroActivity.this, "Carro salvo com sucesso!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Erro ", "ao salvar carro:" + e.toString());
        }
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
