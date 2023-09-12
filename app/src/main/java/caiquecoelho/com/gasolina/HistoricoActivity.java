package caiquecoelho.com.gasolina;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HistoricoActivity extends AppCompatActivity {

    private ListView listView;
    private TextView txtNextRefuelling;
    private TextView textViewNextRefuelling;

    private SQLiteDatabase bancoDeDados;
    private ArrayList<Integer> listaIds;
    private ArrayList<String> listaPostos;
    private ArrayList<String> listaPrecos;
    private ArrayList<String> listaCarros;
    private ArrayList<String> listaDatas;
    private ArrayList<String> listaQuantidades;
    private ArrayList<String> listaReal;
    private ArrayList<String> listaTipos;
    private ArrayList<String> listaLitros;
    private ArrayList<String> listaKms;

    private ArrayList<String> listTimestamp;
    private ArrayList<String> listaAbastecimentos;

    private ArrayAdapter<String> arrayAdapter;

    public static final String TAG = "ImmersiveModeFragment";

    public int flag = 1;
    private String update = "0";
    private int positionUpdate = -1;

    public static Activity history;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        fullScreen();

        history = this;

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            positionUpdate = extras.getInt("position", -1);
            update = extras.getString("update", "0");
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

        setTitle("Histórico Abastecimentos");

        listView = (ListView) findViewById(R.id.listView);
        txtNextRefuelling = (TextView) findViewById(R.id.txtNextRefuelling);
        textViewNextRefuelling = (TextView) findViewById(R.id.textViewNextRefuelling);

        try{
            bancoDeDados = openOrCreateDatabase("app_gasolina", MODE_PRIVATE, null);
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Erro ", "ao abrir o bando de dados"+e.toString());
        }

        carregandoAbastecimentos();

        /*
        if(update.equals("1")){
            Intent intentDetalhes = HistoricoActivity.this.getIntent(positionUpdate);
            startActivity(intentDetalhes);
        }
        */

        listView.setClickable(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Log.i("carro-extra", listaCarros.get(position));
                    Intent intentDetalhes = HistoricoActivity.this.getIntent(position);
                    startActivity(intentDetalhes);
                }catch (Exception e){
                    Log.i("Erro ", "ao abir detlahes:" +e.getMessage());
                }
            }
        });
    }

    private Intent getIntent(int position) {
        String idAbastecimento = listaIds.get(position).toString();
        String carro = listaCarros.get(position);
        String posto = listaPostos.get(position);
        String data = listaDatas.get(position);
        String preco = listaPrecos.get(position);
        String tipo = listaTipos.get(position);
        String quantidadeAbastecida = listaQuantidades.get(position);
        String litrosAbatecido = listaLitros.get(position);
        String kms = listaKms.get(position);
        String timestamp = listTimestamp.get(position);
        String real = listaReal.get(position);
        String lastKms = null;
        String lastLitrosAbastecidos = null;
        try{
            lastKms = listaKms.get(position -1);
        } catch (Exception error) {
            Log.e("Error geting next kms", error.getMessage());
        }

        try{
            lastLitrosAbastecidos = listaLitros.get(position -1);
        } catch (Exception error) {
            Log.e("Error geting next litrs", error.getMessage());
        }

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            userId = extras.getString("user_id", null);
        }

        Intent intentDetalhes = new Intent(HistoricoActivity.this, DetalhesAbastecimentoActivity.class);
        intentDetalhes.putExtra("posto", posto);
        intentDetalhes.putExtra("data", data);
        intentDetalhes.putExtra("preco", preco);
        intentDetalhes.putExtra("tipo", tipo);
        intentDetalhes.putExtra("quantidade", quantidadeAbastecida);
        intentDetalhes.putExtra("litros", litrosAbatecido);
        intentDetalhes.putExtra("kms", kms);
        intentDetalhes.putExtra("real", real);
        intentDetalhes.putExtra("idFuelling", idAbastecimento);
        intentDetalhes.putExtra("position", position);
        Log.i("Historico-Timestamp", timestamp);
        intentDetalhes.putExtra("timestamp", timestamp);
        intentDetalhes.putExtra("user_id", userId);
        intentDetalhes.putExtra("carro", carro);
        try{
            intentDetalhes.putExtra("lastKms", lastKms);
        } catch (Exception error) {
            Log.e("Error geting next kms", error.getMessage());
        }

        try{
            intentDetalhes.putExtra("lastLitrosAbastecidos", lastLitrosAbastecidos);
        } catch (Exception error) {
            Log.e("Error geting next litrs", error.getMessage());
        }
        return intentDetalhes;
    }

    public void recuperandoAbastecimentos(){

        try{
            Cursor cursor = bancoDeDados.rawQuery("SELECT * FROM abastecimentos ORDER BY id DESC", null);
            Log.i("getColumnNames", ""+cursor.getColumnNames().length);
            Log.i("getExtras", cursor.getExtras().toString());

            //recuperando os ids das colunas
            int idColunaid = cursor.getColumnIndex("id");
            int idColunaPosto = cursor.getColumnIndex("posto");
            int idColunaPreco = cursor.getColumnIndex("preco");
            int idColunaCarro = cursor.getColumnIndex("carro");
            int idColunaData = cursor.getColumnIndex("data");
            int idColunaQtde = cursor.getColumnIndex("quantidade");
            int idColunaReal = cursor.getColumnIndex("real");
            int idColunaTipo = cursor.getColumnIndex("tipo");
            int idColunaLitros = cursor.getColumnIndex("qtdAbastecida");
            int idKms = cursor.getColumnIndex("kms");
            int idTimestamp = cursor.getColumnIndex("timestamp");

            //Criando adaptadores e listas
            listaIds = new ArrayList<Integer>();
            listaPostos = new ArrayList<String>();
            listaPrecos = new ArrayList<String>();
            listaCarros = new ArrayList<String>();
            listaDatas = new ArrayList<String>();
            listaQuantidades = new ArrayList<String>();
            listaReal = new ArrayList<String>();
            listaTipos = new ArrayList<String>();
            listaLitros = new ArrayList<String>();
            listaKms = new ArrayList<String>();
            listTimestamp = new ArrayList<String>();

            listaAbastecimentos = new ArrayList<String>();

            //Listando Abastecimentos
            cursor.moveToFirst();
            while(cursor != null){
                int id = cursor.getInt(idColunaid);
                listaIds.add(id);

                String posto = cursor.getString(idColunaPosto);
                listaPostos.add(posto);

                String preco = cursor.getString(idColunaPreco);
                preco.replace(".", ",");
                listaPrecos.add(preco);

                String carro = cursor.getString(idColunaCarro);
                listaCarros.add(carro);

                String data = cursor.getString(idColunaData);
                listaDatas.add(data);

                Double qtdDouble = null;
                String quantidade = cursor.getString(idColunaQtde);
//                if(Double.parseDouble(quantidade) / 100.0 > 1) {
//                    qtdDouble = (Double.parseDouble(quantidade) / 100.0);
//                } else {
//                    qtdDouble = Double.parseDouble(quantidade);
//                }
                qtdDouble = Double.parseDouble(quantidade);
                listaQuantidades.add(quantidade);

                String real = cursor.getString(idColunaReal);
                listaReal.add(real);

                String tipo = cursor.getString(idColunaTipo);
                listaTipos.add(tipo);

                String litros = cursor.getString(idColunaLitros);
                listaLitros.add(litros);

                Log.i("idColunaLitros", "" + idColunaLitros);
                Log.i("idKms", ""+idKms);
                String kms = cursor.getString(idKms);
                Log.i("kms", ""+kms);
                listaKms.add(kms);

                String timestampGet = cursor.getString(idTimestamp);
                listTimestamp.add(timestampGet);
                Log.i("listTimestamp", timestampGet);

                String abastecimento = "Carro: " +carro+ ", Abastecido: " +qtdDouble.toString().replace(".", ",")+ " " +real;
                listaAbastecimentos.add(abastecimento);

                cursor.moveToNext();

                try {
                    String pattern = "dd-MM-yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                    Date date1 = sdf.parse(listaDatas.get(0));
                    Date date2 = sdf.parse(listaDatas.get(1));
                    long diffInMillis = date2.getTime() - date1.getTime();
                    int diffInDays = (int) TimeUnit.MILLISECONDS.toDays(diffInMillis);
                    // txtNextRefuelling.setText(diffInDays + " dias");

                    Calendar currentDate = Calendar.getInstance();
                    Calendar currentDateBase = Calendar.getInstance();
                    currentDate.add(Calendar.DAY_OF_YEAR, diffInDays + 1 );
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    String currentDateFormat = dateFormat.format(currentDate.getTime());

                    txtNextRefuelling.setText(currentDateFormat);

                    boolean isAfter = currentDateBase.after(currentDate);

                    boolean isSameDate = currentDate.get(Calendar.YEAR) == currentDateBase.get(Calendar.YEAR) &&
                            currentDate.get(Calendar.MONTH) == currentDateBase.get(Calendar.MONTH) &&
                            currentDate.get(Calendar.DAY_OF_MONTH) == currentDateBase.get(Calendar.DAY_OF_MONTH);

                    if(isSameDate){
                        txtNextRefuelling.setBackgroundColor(Color.parseColor("#ffffbb33"));
                        textViewNextRefuelling.setBackgroundColor(Color.parseColor("#ffffbb33"));
                    } else if (isAfter) {
                        txtNextRefuelling.setBackgroundColor(Color.RED);
                        textViewNextRefuelling.setBackgroundColor(Color.RED);
                    }
                } catch (Exception e){
                    Log.e("Predict Next Refuelling", e.getMessage());
                }
            }

        }catch(Exception e){
            e.printStackTrace();
            Log.e("Erro ", "ao listar abastecimentos: " +e.getMessage());
            if(listaAbastecimentos == null){
                listaAbastecimentos = new ArrayList<String>();
                listaAbastecimentos.add("Nenhum abastecimento registrado");
            }
        }

    }

    public void carregandoAbastecimentos(){

        recuperandoAbastecimentos();

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listaAbastecimentos
        ){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.BLACK);

                // Generate ListView Item using TextView
                return view;
            }
        };

        listView.setAdapter(arrayAdapter);

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
