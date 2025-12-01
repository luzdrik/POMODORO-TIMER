package aplicacao;

import java.time.LocalDate;

public class StreakManager {

    private int streak;
    private LocalDate ultimoDia;

    public StreakManager() {
        this.streak = 0;
        this.ultimoDia = null;
    }

    public StreakManager(int streak, String ultimoDia) {
        this.streak = streak;
        this.ultimoDia = ultimoDia == null ? null : LocalDate.parse(ultimoDia);
    }

    public int getStreak() {
        return streak;
    }

    public LocalDate getLastDay() {
        return ultimoDia;
    }

    public void registerCycleToday() {
        LocalDate hoje = LocalDate.now();

        if (ultimoDia == null) {
            streak = 1;
        } else if (ultimoDia.equals(hoje)) {
            return; // já contou
        } else if (ultimoDia.plusDays(1).equals(hoje)) {
            streak++;
        } else {
            streak = 1;
        }

        ultimoDia = hoje;
    }
}