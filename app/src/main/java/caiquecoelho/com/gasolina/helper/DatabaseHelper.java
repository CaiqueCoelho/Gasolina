package caiquecoelho.com.gasolina.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import caiquecoelho.com.gasolina.model.Abastecimento;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "app_gasolina";

    // Table name
    private static final String TABLE_ABASTECIMENTOS = "abastecimentos";

    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_POSTO = "posto";
    private static final String KEY_PRECO = "preco";
    private static final String KEY_CARRO = "carro";
    private static final String KEY_DATA = "data";
    private static final String KEY_QUANTIDADE = "quantidade";
    private static final String KEY_REAL = "real";
    private static final String KEY_TIPO = "tipo";
    private static final String KEY_QTD_ABASTECIDA = "qtdAbastecida";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_KMS = "kms";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ABASTECIMENTOS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ABASTECIMENTOS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_POSTO + " VARCHAR,"
                + KEY_PRECO + " DOUBLE,"
                + KEY_CARRO + " VARCHAR,"
                + KEY_DATA + " VARCHAR,"
                + KEY_QUANTIDADE + " DOUBLE,"
                + KEY_REAL + " VARCHAR,"
                + KEY_TIPO + " VARCHAR,"
                + KEY_QTD_ABASTECIDA + " DOUBLE,"
                + KEY_TIMESTAMP + " VARCHAR,"
                + KEY_KMS + " VARCHAR" + ")";
        db.execSQL(CREATE_ABASTECIMENTOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ABASTECIMENTOS);

        // Create tables again
        onCreate(db);
    }

    // Method to get all 'posto' entries
    public List<String> getAllPostos() {
        List<String> postos = new ArrayList<>();
        String selectQuery = "SELECT DISTINCT posto FROM abastecimentos";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                postos.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return postos;
    }

    // Code to get all abastecimentos
//    public List<Abastecimento> getAllAbastecimentos() {
//        List<Abastecimento> abastecimentoList = new ArrayList<>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_ABASTECIMENTOS;
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // Looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                Abastecimento abastecimento = new Abastecimento();
//                abastecimento.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
//                abastecimento.setPosto(cursor.getString(cursor.getColumnIndex(KEY_POSTO)));
//                abastecimento.setPreco(cursor.getDouble(cursor.getColumnIndex(KEY_PRECO)));
//                abastecimento.setCarro(cursor.getString(cursor.getColumnIndex(KEY_CARRO)));
//                abastecimento.setData(cursor.getString(cursor.getColumnIndex(KEY_DATA)));
//                abastecimento.setQuantidade(cursor.getDouble(cursor.getColumnIndex(KEY_QUANTIDADE)));
//                abastecimento.setReal(cursor.getString(cursor.getColumnIndex(KEY_REAL)));
//                abastecimento.setTipo(cursor.getString(cursor.getColumnIndex(KEY_TIPO)));
//                abastecimento.setQtdAbastecida(cursor.getDouble(cursor.getColumnIndex(KEY_QTD_ABASTECIDA)));
//                abastecimento.setTimestamp(cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP)));
//                abastecimento.setKms(cursor.getString(cursor.getColumnIndex(KEY_KMS)));
//                // Adding abastecimento to list
//                abastecimentoList.add(abastecimento);
//            } while (cursor.moveToNext());
//        }
//
//        // Close the cursor and db when done
//        cursor.close();
//        db.close();
//
//        // Return abastecimento list
//        return abastecimentoList;
//    }
}

//class Abastecimento {
//    private int id;
//    private String posto;
//    private double preco;
//    private String carro;
//    private String data;
//    private double quantidade;
//    private String real;
//    private String tipo;
//    private double qtdAb
//}
