package aplicacao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class JSONManager {

    private static final String FILE = "data.json";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static DataModel carregarTudo() {

        File file = new File(FILE);

        if (!file.exists()) {
            DataModel novo = criarDataModelPadrao();
            salvarTudo(novo);
            return novo;
        }

        try (FileReader reader = new FileReader(FILE, StandardCharsets.UTF_8)) {

            DataModel data = gson.fromJson(reader, DataModel.class);

            if (data == null) return criarDataModelPadrao();
            if (data.resumo == null) data.resumo = new Resumo();
            if (data.tasks == null) data.tasks = new ArrayList<>();
            if (data.tasksConcluidas == null) data.tasksConcluidas = new ArrayList<>();

            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return criarDataModelPadrao();
        }
    }

    public static void salvarTudo(DataModel data) {
        try (FileWriter writer = new FileWriter(FILE, StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static DataModel criarDataModelPadrao() {
        DataModel novo = new DataModel();
        novo.resumo = new Resumo();
        novo.tasks = new ArrayList<>();
        novo.tasksConcluidas = new ArrayList<>();
        return novo;
    }
}
