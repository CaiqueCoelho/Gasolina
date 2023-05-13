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

        final Intent intent = getIntent();
        final String posto = intent.getStringExtra("posto");
        final String preco = intent.getStringExtra("preco");
        final String quantidade = intent.getStringExtra("quantidade");
        final String tipo = intent.getStringExtra("tipo");
        final String real = intent.getStringExtra("real");

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
                    //intentAbastecimento.putExtra("carro", carro);
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

    public void esconderMenuBar(){
        if(flag == 1) {
            // The UI options currently enabled are represented by a bitfield.
            // getSystemUiVisibility() gives us that bitfield.
            int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
            int newUiOptions = uiOptions;
            boolean isImmersiveModeEnabled =
                    ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
            if (isImmersiveModeEnabled) {
                Log.i(TAG, "Turning immersive mode mode off. ");
            } else {
                Log.i(TAG, "Turning immersive mode mode on.");
            }

            // Navigation bar hiding:  Backwards compatible to ICS.
            if (Build.VERSION.SDK_INT >= 14) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            // Status bar hiding: Backwards compatible to Jellybean
            if (Build.VERSION.SDK_INT >= 16) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            // Immersive mode: Backward compatible to KitKat.
            // Note that this flag doesn't do anything by itself, it only augments the behavior
            // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
            // all three flags are being toggled together.
            // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
            // Sticky immersive mode differs in that it makes the navigation and status bars
            // semi-transparent, and the UI flag does not get cleared when the user interacts with
            // the screen.
            if (Build.VERSION.SDK_INT >= 18) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
            flag = 0;
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
