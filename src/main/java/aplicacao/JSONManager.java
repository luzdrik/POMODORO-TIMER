package aplicacao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONManager {

    private static final String FILE = "resumo.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Resumo carregarResumo() {
        try (FileReader reader = new FileReader(FILE)) {
            return gson.fromJson(reader, Resumo.class);
        } catch (Exception e) {
            return new Resumo(); // se não existir o arquivo
        }
    }

    public static void salvarResumo(Resumo resumo) {
        try (FileWriter writer = new FileWriter(FILE)) {
            gson.toJson(resumo, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}