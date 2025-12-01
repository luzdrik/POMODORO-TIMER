package aplicacao;

import java.time.LocalDate;

public class Resumo {

    private int totalFocoMinutosHoje;
    private int ciclosHoje;
    private int pomodorosHoje;
    private double progressoDiario;

    private LocalDate ultimoDia = LocalDate.now();
    private int streak = 1;

    public void registrarFoco(int minutos) {
        verificarViradaDia();
        totalFocoMinutosHoje += minutos;
    }

    public void registrarCiclo() {
        verificarViradaDia();
        ciclosHoje++;
    }

    public void registrarPomodoro() {
        verificarViradaDia();
        pomodorosHoje++;
    }

    public void verificarViradaDia() {
        LocalDate hoje = LocalDate.now();

        if (!hoje.equals(ultimoDia)) {

            totalFocoMinutosHoje = 0;
            ciclosHoje = 0;
            pomodorosHoje = 0;
            progressoDiario = 0;

            if (ultimoDia.plusDays(1).equals(hoje)) {
                streak++;
            } else {
                streak = 1;
            }

            ultimoDia = hoje;
        }
    }

    public int getTotalFoco() { return totalFocoMinutosHoje; }
    public int getCiclosHoje() { return ciclosHoje; }
    public int getPomodorosHoje() { return pomodorosHoje; }
    public int getStreak() { return streak; }
    public double getProgressoDiario() { return progressoDiario; }

    public void setProgressoDiario(double progressoDiario) {
        this.progressoDiario = progressoDiario;
    }
}