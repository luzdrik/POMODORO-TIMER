package aplicacao;

import java.util.ArrayList;
import java.util.List;

public class DataModel {

    public Resumo resumo = new Resumo();
    public Config config = new Config();

    public List<String> tasks = new ArrayList<>();
    public List<String> tasksConcluidas = new ArrayList<>();

    public int streak = 0;
    public String ultimoDiaCompleto = null;

    public DataModel() {}

    public Resumo getResumo() {
        return resumo;
    }

    public void setResumo(Resumo resumo) {
        this.resumo = resumo;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    public List<String> getTasksConcluidas() {
        return tasksConcluidas;
    }

    public void setTasksConcluidas(List<String> tasksConcluidas) {
        this.tasksConcluidas = tasksConcluidas;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public String getUltimoDiaCompleto() {
        return ultimoDiaCompleto;
    }

    public void setUltimoDiaCompleto(String ultimoDiaCompleto) {
        this.ultimoDiaCompleto = ultimoDiaCompleto;
    }
}