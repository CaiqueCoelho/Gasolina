package caiquecoelho.com.gasolina;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import caiquecoelho.com.gasolina.helper.DatabaseHelper;
import caiquecoelho.com.gasolina.model.Abastecimento;

public class AbastecerActivity extends AppCompatActivity {

    private EditText edtPosto;
    private EditText edtPreco;
    private EditText edtKms;
    private EditText edtQuantidade;
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
    private NumberFormat currencyFormat;

    private SQLiteDatabase bancoDeDados;
    private ArrayAdapter<String> adapterListaCarro;
    private ArrayList<String> listaCarros;
    private ArrayList<Integer> listaIds;
    private String idFuelling;
    private String update = "0";
    private int position = -1;
    private String date = null;

    public static Activity activityAbastecer;

    public static final String TAG = "ImmersiveModeFragment";

    public int flag = 0;
    private String userId;
    private DatabaseReference myRef;

    private Date currentDate;
    private String timestamp;

    AutoCompleteTextView autoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abastecer);

        autoCompleteTextView = findViewById(R.id.edtPosto);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        // Get all posto entries
        List<String> postos = databaseHelper.getAllPostos();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, postos);

        // Specify the layout to use when the list of choices appears
        autoCompleteTextView.setAdapter(adapter);

        // Set the minimum number of characters, after which the dropdown will start showing suggestions
        autoCompleteTextView.setThreshold(0);

        edtQuantidade = findViewById(R.id.edtQuantidadeNew);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        edtQuantidade.addTextChangedListener(currencyTextWatcher);

        fullScreen();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            userId = extras.getString("user_id", null);
        }

        Log.i("Login-auth-abastecer", Objects.requireNonNullElse(userId, "userId is null"));

        try{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference();
        } catch(Exception error){
            Log.e("BreakAbastecer", error.getMessage());
        }

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

        Log.i("BreakAbastecer", "Before criandoOuAbrindoBancoDeDadosCarro");
        criandoOuAbrindoBancoDeDadosCarro();
        Log.i("BreakAbastecer", "Before criandoOuAbrindoBancoDeDados");
        criandoOuAbrindoBancoDeDados();
        Log.i("BreakAbastecer", "After criandoOuAbrindoBancoDeDados");

        edtPosto = (EditText) findViewById(R.id.edtPosto);
        edtPreco = (EditText) findViewById(R.id.edtPreco);
        edtKms = (EditText) findViewById(R.id.edtKMs);
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

        SimpleMaskFormatter smfPreco = new SimpleMaskFormatter("N,NN");
        MaskTextWatcher mtwPreco = new MaskTextWatcher(edtPreco, smfPreco);
        edtPreco.addTextChangedListener(mtwPreco);

        Log.i("BreakAbastecer", "Before carregandoListaCarros");
        carregandoListaCarros();
        Log.i("BreakAbastecer", "After carregandoListaCarros");

        if(extras != null) {
            if(extras.getString("kms") != null){
                edtKms.setText(extras.getString("kms"));
            }
            if(extras.getString("posto") != null){
                edtPosto.setText(extras.getString("posto"));
            }
            if(extras.getString("preco") != null){
                edtPreco.setText(extras.getString("preco"));
            }
            /*
            if(extras.getInt("car_activity") == 1){
                Log.i("car_activity", "1");
                if(extras.getString("quantidade") != null){
                    edtQuantidade.setText(extras.getString("quantidade"));
                }
            }
            else if(extras.getString("quantidade") != null){
                Log.i("car_activity", "0");
                edtQuantidade.setText(extras.getString("quantidade"));
            }*/
            if(extras.getString("quantidade") != null){
                edtQuantidade.setText(extras.getString("quantidade"));
            }

            if(extras.getString("tipo") != null){
                Log.i("tipo", extras.getString("tipo"));
                if (extras.getString("tipo").equals("Álcool") || extras.getString("tipo").equals("alcool")) {
                    radioAlcool.setChecked(true);
                } else if (extras.getString("tipo").equals("Gasolina") || extras.getString("tipo").equals("gasolina")) {
                    Log.i("tipo gasolina", extras.getString("tipo"));
                    radioGasolina.setChecked(true);
                }
            }
            if(extras.getString("real") != null){
                if (extras.getString("real").equals("litro")) {
                    toggleReal.setChecked(true);
                }
            }

            if (extras.getString("idFuelling") != null) {
                idFuelling = extras.getString("idFuelling");
                Log.i("idFuelling", idFuelling);
            }
            if (extras.getString("edit") != null) {
                update = extras.getString("edit");
                Log.i("update data", update);
            }

            if (extras.getString("timestamp") != null) {
                timestamp = extras.getString("timestamp");
                Log.i("timestamp", timestamp);
            }

            Log.i("BreakAbastecer", "Before get carro");
            if(extras.getString("carro") != null){
                Log.i("selectedCar", extras.getString("carro"));
                Log.i("carro-extra", listaCarros.toString());
                int position = listaCarros.indexOf(extras.getString("carro"));
                if (position != -1) {
                    spinnerCarro.setSelection(position);
                }
            } else {
                spinnerCarro.setSelection(listaCarros.size() - 1);
            }
            position = extras.getInt("position", -1);
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = dateFormat.format(currentDate.getTime());
            date = extras.getString("date", formattedDate);
        }

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Calendar currentDateCalendarInstance = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    String formattedDate = dateFormat.format(currentDateCalendarInstance.getTime());
                    currentDate = new Date();
                    if(!Objects.equals(update, "1")) {
                        timestamp = String.valueOf(currentDate.getTime());
                    } else {
                        timestamp = extras.getString("timestamp");
                        Log.i("BreakAbastecer", Objects.requireNonNullElse(timestamp, "timestamp is null"));
                        if(timestamp == null){
                            timestamp = String.valueOf(currentDate.getTime());
                        }
                        Log.i("UPDATE-Abastecer", String.valueOf(timestamp));
                    }

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
                        String quantidadeSemFormatacao = stringQuantidade.replace(".", "").replace(",", ".").replace("R$", "").replace(" ", "").replace("\\s", "");
                        quantidadeSemFormatacao = quantidadeSemFormatacao.replaceAll("\\s", "");
                        Log.i("quantidadeSemFormatacao", quantidadeSemFormatacao);

                        double doublePreco = Double.parseDouble(precoSemFormatacao);
                        double doubleQuantidade = Double.parseDouble(quantidadeSemFormatacao);

                        double quantidadeLitroAbastecida = doubleQuantidade / doublePreco;
                        double reaisAbastecido = doubleQuantidade * doublePreco;
                        String kms = edtKms.getText().toString();

                        Abastecimento abastecimento = new Abastecimento();
                        if(!autoCompleteTextView.getText().toString().isEmpty()) {
                            abastecimento.setPosto(autoCompleteTextView.getText().toString());
                        }else{
                            abastecimento.setPosto("Não Informado");
                        }
                        abastecimento.setPreco(doublePreco);
                        abastecimento.setCarro(listaCarros.get(spinnerCarro.getSelectedItemPosition()));
                        Log.i("SelectedCar", abastecimento.getCarro());
                        abastecimento.setData(formattedDate);
                        abastecimento.setQuantidade(doubleQuantidade);
                        abastecimento.setKms(kms);
                        abastecimento.setTimestamp(timestamp);
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
                    Log.e("Erro ao salvar:", ""+e.getMessage());
                }
            }
        });

        btnNovoCarro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AbastecerActivity.this, CarroActivity.class);
                if(!autoCompleteTextView.getText().toString().isEmpty()) {
                    intent.putExtra("posto", autoCompleteTextView.getText().toString());
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
                Log.i("BreakAbastecer", "Before get user_ui");
                if(extras != null && extras.getString("user_id") != null) {
                    intent.putExtra("user_id", extras.getString("user_id"));
                }
                if (extras.getString("edit") != null) {
                    intent.putExtra("edit", extras.getString("edit"));
                }
                Log.i("BreakAbastecer", "After get user_ui");
                intent.putExtra("position", position);
                intent.putExtra("date", date);
                intent.putExtra("timestamp", timestamp);
                intent.putExtra("idFuelling", idFuelling);
                intent.putExtra("kms", edtKms.getText().toString());
                startActivity(intent);
            }
        });
    }

    private final TextWatcher currencyTextWatcher = new TextWatcher() {
        private boolean isFormatting;
        private boolean isDeleting;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (isFormatting || isDeleting) {
                return;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isFormatting || isDeleting) {
                return;
            }

            isFormatting = true;
            String formattedValue = formatCurrency(s.toString());
            edtQuantidade.setText(formattedValue);
            edtQuantidade.setSelection(formattedValue.length());
            isFormatting = false;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting || isDeleting) {
                return;
            }
        }

        private String formatCurrency(String value) {
            String cleanValue = value.replaceAll("[^0-9]", "");

            try {
                double parsed = Double.parseDouble(cleanValue);
                String formatted = currencyFormat.format((parsed / 100));
                return formatted;
            } catch (NumberFormatException e) {
                return "";
            }
        }
    };

    public void criandoOuAbrindoBancoDeDadosCarro(){

        try {
            bancoDeDados = openOrCreateDatabase("app_gasolina", MODE_PRIVATE, null);

            bancoDeDados.execSQL("CREATE TABLE IF NOT EXISTS carros(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "marca VARCHAR, apelido VARCHAR)");
        }catch (Exception e){
            e.printStackTrace();
            Log.i("Erro ", "ao criar tabela carros: "+e.toString());
        }

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
                    "qtdAbastecida DOUBLE," +
                    "timestamp VARCHAR," +
                    "kms VARCHAR)");

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void salvandoNoBancoDeDados(Abastecimento abastecimento){

        if(!Objects.equals(update, "1")){
            Log.i("Create Abastecimento", "" + update);
            Log.i("Create Abastecimento", "" + Boolean.getBoolean(update));
            Log.i("Create timestamp", "" + abastecimento.getTimestamp());

            try{

                bancoDeDados.execSQL("INSERT INTO abastecimentos(posto, preco, carro, data, quantidade, real, tipo, qtdAbastecida, timestamp, kms) " +
                        "VALUES ('" +abastecimento.getPosto()+ "', '" +abastecimento.getPreco()+"', '"+abastecimento.getCarro()+"" +
                        "', '"+abastecimento.getData()+"', '"+abastecimento.getQuantidade()+"', '"+abastecimento.getReal()+"'," +
                        " '"+abastecimento.getTipo()+"', '"+abastecimento.getQtdLitroAbastecida()+"', '"+abastecimento.getTimestamp()+"', '"+abastecimento.getKms()+"')");

                if(userId != null){
                    myRef.child("users/").child(userId).child(timestamp).setValue(abastecimento);
                }
                Toast.makeText(AbastecerActivity.this, "Abastecimento criado com sucesso!", Toast.LENGTH_LONG).show();

            }catch(Exception e){
                e.printStackTrace();
                Log.e("Erro ", " ao salvar carro: "+e.getMessage());
            }
        } else{
            Log.i("Update Abastecimento", "doing update");
            Log.i("Update timestamp", "" + abastecimento.getTimestamp());
            Log.i("Update carro", "" + abastecimento.getCarro());

            try{

                String updateQuery = "UPDATE abastecimentos SET posto = '" + abastecimento.getPosto() + "', " +
                        "preco = '" + abastecimento.getPreco() + "', " +
                        "carro = '" + abastecimento.getCarro() + "', " +
                        "data = '" + date + "', " +
                        "quantidade = '" + abastecimento.getQuantidade() + "', " +
                        "real = '" + abastecimento.getReal() + "', " +
                        "tipo = '" + abastecimento.getTipo() + "', " +
                        "qtdAbastecida = '" + abastecimento.getQtdLitroAbastecida() + "', " +
                        "timestamp = '" + abastecimento.getTimestamp() + "', " +
                        "kms = '" + abastecimento.getKms() + "'" +
                        "WHERE id = " + idFuelling;

                bancoDeDados.execSQL(updateQuery);

                Log.i("UpdateAbatecer", Objects.requireNonNullElse(userId, "user null"));

                if(userId != null){
                    myRef.child("users/").child(userId).child(timestamp).setValue(abastecimento);
                }

                Toast.makeText(AbastecerActivity.this, "Abastecimento atualizado com sucesso!", Toast.LENGTH_LONG).show();

                HistoricoActivity.history.finish();
                DetalhesAbastecimentoActivity.details.finish();
                Intent history = new Intent(AbastecerActivity.this, HistoricoActivity.class);
                history.putExtra("update", "1");
                history.putExtra("position", position);
                startActivity(history);

            }catch(Exception e){
                e.printStackTrace();
                Log.e("Erro ", " ao salvar carro: "+e.getMessage());
            }
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
                listaCarros.add("Não informado");
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
