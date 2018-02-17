package caiquecoelho.com.gasolina;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import caiquecoelho.com.gasolina.helper.NumberTextWatcher;
import caiquecoelho.com.gasolina.model.Abastecimento;
import faranjit.currency.edittext.CurrencyEditText;

public class AbastecerActivity extends AppCompatActivity {

    private EditText edtPosto;
    private EditText edtPreco;
    private CurrencyEditText edtQuantidade;
    private RadioButton radioAlcool;
    private RadioButton radioGasolina;
    private RadioGroup radioGroup;
    private ToggleButton toggleReal;
    private TextView txtErroPreco;
    private TextView txtErroQuantidade;
    private TextView txtErroTipo;
    private Spinner spinnerCarro;
    private ImageView btnSalvar;
    private ImageView btnNovoCarro;

    private SQLiteDatabase bancoDeDados;
    private ArrayAdapter<String> adapterListaCarro;
    private ArrayList<String> listaCarros;
    private ArrayList<Integer> listaIds;

    public static Activity activityAbastecer;

    public static final String TAG = "ImmersiveModeFragment";

    public int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abastecer);

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

        activityAbastecer = this;

        setTitle("Registrar Abastecimento");

        criandoOuAbrindoBancoDeDados();

        edtPosto = (EditText) findViewById(R.id.edtPosto);
        edtPreco = (EditText) findViewById(R.id.edtPreco);
        edtQuantidade = (CurrencyEditText) findViewById(R.id.edtQuantidade);
        radioAlcool = (RadioButton) findViewById(R.id.radioAlcool);
        radioGasolina = (RadioButton) findViewById(R.id.radioGasolina);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        toggleReal = (ToggleButton) findViewById(R.id.btnReal);
        txtErroPreco = (TextView) findViewById(R.id.txtErroPreco);
        txtErroQuantidade = (TextView) findViewById(R.id.txtErroQuantidade);
        txtErroTipo = (TextView) findViewById(R.id.txtErroTipo);
        spinnerCarro = (Spinner) findViewById(R.id.carro);
        btnSalvar = (ImageView) findViewById(R.id.btnSalvar);
        btnNovoCarro = (ImageView) findViewById(R.id.btnNovoCarro);

        edtQuantidade.setMonetaryDivider(',');

        SimpleMaskFormatter smfPreco = new SimpleMaskFormatter("N,NN");
        MaskTextWatcher mtwPreco = new MaskTextWatcher(edtPreco , smfPreco);
        edtPreco.addTextChangedListener(mtwPreco);

        Bundle extras = getIntent().getExtras();
        carregandoListaCarros();


        if(extras != null){
            edtPosto.setText(extras.getString("posto"));
            edtPreco.setText(extras.getString("preco"));
            edtQuantidade.setText(extras.getString("quantidade"));
            if(extras.getString("tipo").equals("alcool")){
                radioAlcool.setChecked(true);
            }
            else if(extras.getString("tipo").equals("gasolina")){
                radioGasolina.setChecked(true);
            }
            if(extras.getString("real").equals("litro")){
                toggleReal.setChecked(true);
            }
            spinnerCarro.setSelection(listaCarros.size()-1);
        }

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Calendar currentDate = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = dateFormat.format(currentDate.getTime());

                    String stringPreco = edtPreco.getText().toString();
                    String stringQuantidade = edtQuantidade.getText().toString();
                    boolean gasolinaMarcado = radioGasolina.isChecked();
                    boolean alcoolMarcado = radioAlcool.isChecked();

                    txtErroPreco.setText("");
                    txtErroTipo.setText("");
                    txtErroQuantidade.setText("");

                    if (stringPreco.isEmpty() || stringQuantidade.isEmpty() || (!gasolinaMarcado && !alcoolMarcado)) {

                        if (stringPreco.isEmpty()) {
                            txtErroPreco.setText("Preço obrigatório");
                        }

                        if (stringQuantidade.isEmpty()) {
                            txtErroQuantidade.setText("Quantidade obrigatória");
                        }

                        if (!gasolinaMarcado && !alcoolMarcado) {
                            txtErroTipo.setText("Tipo obrigatório");
                        }

                        Toast.makeText(AbastecerActivity.this, "Algum campo está errado!", Toast.LENGTH_LONG).show();

                    } else {

                        String precoSemFormatacao = stringPreco.replace(",", ".");
                        stringQuantidade = stringQuantidade.replace(".", "");
                        String quantidadeSemFormatacao = stringQuantidade.replace(",", ".");

                        double doublePreco = Double.parseDouble(precoSemFormatacao);
                        double doubleQuantidade = Double.parseDouble(quantidadeSemFormatacao);

                        double quantidadeLitroAbastecida = doubleQuantidade / doublePreco;
                        double reaisAbastecido = doubleQuantidade * doublePreco;

                        Abastecimento abastecimento = new Abastecimento();
                        if(!edtPosto.getText().toString().isEmpty()) {
                            abastecimento.setPosto(edtPosto.getText().toString());
                        }else{
                            abastecimento.setPosto("Não Informado");
                        }
                        abastecimento.setPreco(doublePreco);
                        abastecimento.setCarro(listaCarros.get(spinnerCarro.getSelectedItemPosition()));
                        abastecimento.setData(formattedDate);
                        abastecimento.setQuantidade(doubleQuantidade);
                        if (!toggleReal.isChecked()) {
                            abastecimento.setReal("reais");
                            abastecimento.setQtdLitroAbastecida(quantidadeLitroAbastecida);
                        } else {
                            abastecimento.setReal("litros");
                            abastecimento.setQtdLitroAbastecida(reaisAbastecido);
                        }
                        if (gasolinaMarcado) {
                            abastecimento.setTipo("Gasolina");
                            Log.i("Tipo: ", "Gasolina");
                        } else {
                            abastecimento.setTipo("Álcool");
                            Log.i("Tipo: ", "Álcool");
                        }
                        Log.i("Tipo", abastecimento.getTipo());
                        salvandoNoBancoDeDados(abastecimento);
                        finish();

                    }
                }catch(Exception e){
                    Log.i("Erro ao salvar:", " "+e.toString());
                }
            }
        });

        btnNovoCarro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AbastecerActivity.this, CarroActivity.class);
                if(!edtPosto.getText().toString().isEmpty()) {
                    intent.putExtra("posto", edtPosto.getText().toString());
                }else{
                    intent.putExtra("posto", "");
                }

                if(!edtPreco.getText().toString().isEmpty()){
                    intent.putExtra("preco", edtPreco.getText().toString());
                }
                else{
                    intent.putExtra("preco", "");
                }

                if(!edtQuantidade.getText().toString().isEmpty()){
                    intent.putExtra("quantidade", edtQuantidade.getText().toString());
                }
                else{
                    intent.putExtra("quantidade", "");
                }

                if(radioAlcool.isChecked()){
                    intent.putExtra("tipo", "alcool");
                }
                else if(radioGasolina.isChecked()){
                    intent.putExtra("tipo", "gasolina");
                }
                else{
                    intent.putExtra("tipo", "");
                }

                if(toggleReal.isChecked()){
                    intent.putExtra("real", "litro");
                }
                else{
                    intent.putExtra("real", "real");
                }

                startActivity(intent);
                //finish();
            }
        });
    }

    public void criandoOuAbrindoBancoDeDados(){

        try{
            bancoDeDados = openOrCreateDatabase("app_gasolina", MODE_PRIVATE, null);

            bancoDeDados.execSQL("CREATE TABLE IF NOT EXISTS abastecimentos(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "posto VARCHAR," +
                    "preco DOUBLE," +
                    "carro VARCHAR," +
                    "data VARCHAR," +
                    "quantidade DOUBLE," +
                    "real VARCHAR," +
                    "tipo VARCHAR," +
                    "qtdAbastecida DOUBLE)");

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void salvandoNoBancoDeDados(Abastecimento abastecimento){

        try{

            bancoDeDados.execSQL("INSERT INTO abastecimentos(posto, preco, carro, data, quantidade, real, tipo, qtdAbastecida) " +
                    "VALUES ('" +abastecimento.getPosto()+ "', '" +abastecimento.getPreco()+"', '"+abastecimento.getCarro()+"" +
                    "', '"+abastecimento.getData()+"', '"+abastecimento.getQuantidade()+"', '"+abastecimento.getReal()+"'," +
                    " '"+abastecimento.getTipo()+"', '"+abastecimento.getQtdLitroAbastecida()+"')");

            Toast.makeText(AbastecerActivity.this, "Abastecimento salvo com sucesso!", Toast.LENGTH_LONG).show();

        }catch(Exception e){
            e.printStackTrace();
            Log.i("Erro ", " ao salvar carro: "+e.toString());
        }
    }

    public void recuperandoCarros(){

        try{

            Cursor cursor = bancoDeDados.rawQuery("SELECT * FROM carros ORDER BY id ASC", null);

            //Recupera os ids das colunas
            int idColunaId = cursor.getColumnIndex("id");
            int idColunaMarca = cursor.getColumnIndex("marca");
            int idColunaApelido = cursor.getColumnIndex("apelido");

            //Criando adaptador para lista de carros
            listaIds = new ArrayList<Integer>();
            listaCarros = new ArrayList<String>();

            //Listar carros
            cursor.moveToFirst();
            while(cursor != null){
                String nomeCarro = cursor.getString(idColunaApelido);
                if(nomeCarro.equals("Não informado")){
                    listaCarros.add(cursor.getString(idColunaMarca));
                }else{
                    listaCarros.add(cursor.getString(idColunaApelido));
                }
                //Log.i("Carro: ", "Id: " +cursor.getInt(idColunaId)+ " Marca: "+cursor.getString(idColunaMarca));
                listaIds.add(cursor.getInt(idColunaId));
                cursor.moveToNext();
            }

        }catch(Exception e){
            e.printStackTrace();
            Log.i("Erro ","ao recuperar carros: " + e.toString());
            if(listaCarros == null) {
                listaCarros = new ArrayList<String>();
                listaCarros.add("Nenhum Carro Cadastrado");
            }
        }
    }

    public void carregandoListaCarros(){
        recuperandoCarros();
        //Log.i("ListaCarros: ", listaCarros.get(0));
        adapterListaCarro = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                listaCarros);
        adapterListaCarro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarro.setAdapter(adapterListaCarro);
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
