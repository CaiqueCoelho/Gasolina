package caiquecoelho.com.gasolina;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoricoActivity extends AppCompatActivity {

    private ListView listView;

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
    private ArrayList<String> listaAbastecimentos;

    private ArrayAdapter<String> arrayAdapter;

    public static final String TAG = "ImmersiveModeFragment";

    public int flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

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

        setTitle("Histórico Abastecimentos");

        listView = (ListView) findViewById(R.id.listView);

        try{
            bancoDeDados = openOrCreateDatabase("app_gasolina", MODE_PRIVATE, null);
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Erro ", "ao abrir o bando de dados"+e.toString());
        }

        carregandoAbastecimentos();

        listView.setClickable(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String posto = listaPostos.get(position);
                    String data = listaDatas.get(position);
                    String preco = listaPrecos.get(position);
                    String tipo = listaTipos.get(position);
                    String quantidadeAbastecida = listaQuantidades.get(position);
                    String litrosAbatecido = listaLitros.get(position);
                    String real = listaReal.get(position);

                    Intent intentDetalhes = new Intent(HistoricoActivity.this, DetalhesAbastecimentoActivity.class);
                    intentDetalhes.putExtra("posto", posto);
                    intentDetalhes.putExtra("data", data);
                    intentDetalhes.putExtra("preco", preco);
                    intentDetalhes.putExtra("tipo", tipo);
                    intentDetalhes.putExtra("quantidade", quantidadeAbastecida);
                    intentDetalhes.putExtra("litros", litrosAbatecido);
                    intentDetalhes.putExtra("real", real);

                    startActivity(intentDetalhes);
                }catch (Exception e){
                    Log.i("Erro ", "ao abir detlahes:" +e.toString());
                }
            }
        });
    }

    public void recuperandoAbastecimentos(){

        try{
            Cursor cursor = bancoDeDados.rawQuery("SELECT * FROM abastecimentos ORDER BY id DESC", null);

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

                String quantidade = cursor.getString(idColunaQtde);
                quantidade.replace(".", ",");
                listaQuantidades.add(quantidade);

                String real = cursor.getString(idColunaReal);
                listaReal.add(real);

                String tipo = cursor.getString(idColunaTipo);
                listaTipos.add(tipo);

                String litros = cursor.getString(idColunaLitros);
                listaLitros.add(litros);


                String abastecimento = "Carro: " +carro+ ", Abastecido: " +quantidade+ " " +real;
                listaAbastecimentos.add(abastecimento);

                cursor.moveToNext();
            }

        }catch(Exception e){
            e.printStackTrace();
            Log.i("Erro ", "ao listar abastecimentos: " +e.toString());
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
