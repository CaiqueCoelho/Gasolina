package caiquecoelho.com.gasolina;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import caiquecoelho.com.gasolina.model.Carro;

public class DetalhesAbastecimentoActivity extends AppCompatActivity {

    private TextView txtPosto;
    private TextView txtData;
    private TextView txtPreco;
    private TextView txtQtde;
    private TextView txtLitros;
    private TextView txtTipo;
    private TextView txtTotal;
    private TextView txtKms;
    private TextView txtKmsPerLiter;
    private Button btnEdit;
    private String idFuelling = null;
    private int position = -1;

    public static final String TAG = "ImmersiveModeFragment";
    public static Activity details;

    public int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_abastecimento);

        fullScreen();
        details = this;

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

        setTitle("Detalhes Abastecimento");

        txtPosto = (TextView) findViewById(R.id.txtPosto);
        txtData = (TextView) findViewById(R.id.txtData);
        txtPreco = (TextView) findViewById(R.id.txtPreco);
        txtQtde = (TextView) findViewById(R.id.txtQtde);
        txtLitros = (TextView) findViewById(R.id.txtLitros);
        txtTipo = (TextView) findViewById(R.id.txtTipo);
        txtTotal = (TextView) findViewById(R.id.textViewTotal);
        txtKms = (TextView) findViewById(R.id.txtKms);
        txtKmsPerLiter = (TextView) findViewById(R.id.txtKmsPerLiter);
        btnEdit = (Button) findViewById(R.id.btnEdit);

        Bundle extras = getIntent().getExtras();
        txtPosto.setText(extras.getString("posto"));
        txtData.setText(extras.getString("data"));
        txtPreco.setText(extras.getString("preco"));
        if(!extras.getString("kms").isEmpty()){
            txtKms.setText(extras.getString("kms") + " quilômetros");
        }
        Double qtdDouble = Double.parseDouble(extras.getString("quantidade"));
//        if(qtdDouble / 100.0 > 1) {
//            qtdDouble = (Double.parseDouble(extras.getString("quantidade")) / 100.0);
//        }
        txtQtde.setText(qtdDouble.toString().replace(".", ",") + " " + extras.getString("real"));
        if(extras.getString("real").equals("litros")){
            txtTotal.setText("Total Abastecido em Reais");
        }

        Double litros = Double.parseDouble(extras.getString("litros"));
//        if(Double.parseDouble(extras.getString("litros")) / 100 > 1) {
//            litros = (Double.parseDouble(extras.getString("quantidade")) / 100.0);
//        }
        txtLitros.setText(litros.toString());
        txtTipo.setText(extras.getString("tipo"));

        Double kmsForLiter = null;
        try{
            kmsForLiter = (Double.parseDouble(extras.getString("lastKms")) - Double.parseDouble(extras.getString("kms"))) / Double.parseDouble(extras.getString("lastLitrosAbastecidos"));
            // Format the string to have only two decimal places
            String formattedKmsForLiter = String.format("%.2f", kmsForLiter);
            txtKmsPerLiter.setText(formattedKmsForLiter + " quilômetros por litro");
        } catch(Exception error){
            Log.i("kmsForLiter", error.getMessage());
        }

        idFuelling = extras.getString("idFuelling");
        position = extras.getInt("position", -1);

        Double finalQtdDouble = qtdDouble;
        btnEdit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                Intent intentEdit = new Intent(DetalhesAbastecimentoActivity.this, AbastecerActivity.class);
                intentEdit.putExtra("posto", extras.getString("posto"));
                intentEdit.putExtra("kms", extras.getString("kms"));
                intentEdit.putExtra("preco", extras.getString("preco"));
                intentEdit.putExtra("quantidade", finalQtdDouble.toString());
                intentEdit.putExtra("tipo", extras.getString("tipo"));
                intentEdit.putExtra("real", extras.getString("real"));
                Log.i("carro-extra", extras.getString("carro"));
                intentEdit.putExtra("carro", extras.getString("carro"));
                intentEdit.putExtra("edit", "1");
                intentEdit.putExtra("date", extras.getString("data"));
                intentEdit.putExtra("position", position);
                intentEdit.putExtra("timestamp", extras.getString("timestamp"));
                intentEdit.putExtra("user_id", extras.getString("user_id"));
                intentEdit.putExtra("idFuelling", idFuelling);
                DetalhesAbastecimentoActivity.this.finish();
                startActivity(intentEdit);
                finish();
            }
        }
        );
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
