package aplicacao;

public class Config {
    // Valores padrão da técnica Pomodoro
    private int ciclos = 4;
    private int foco = 25;
    private int pausaCurta = 5;
    private int pausaLonga = 15;
    private int cicloPausaLonga = 4;

    private boolean autoCiclo = false;
    private boolean sempreVisivel = false;

    public int getCiclos() { return ciclos; }
    public void setCiclos(int ciclos) { this.ciclos = ciclos; }

    public int getFoco() { return foco; }
    public void setFoco(int foco) { this.foco = foco; }

    public int getPausaCurta() { return pausaCurta; }
    public void setPausaCurta(int pausaCurta) { this.pausaCurta = pausaCurta; }

    public int getPausaLonga() { return pausaLonga; }
    public void setPausaLonga(int pausaLonga) { this.pausaLonga = pausaLonga; }

    public int getCicloPausaLonga() { return cicloPausaLonga; }
    public void setCicloPausaLonga(int cicloPausaLonga) { this.cicloPausaLonga = cicloPausaLonga; }

    public boolean isAutoCiclo() { return autoCiclo; }
    public void setAutoCiclo(boolean autoCiclo) { this.autoCiclo = autoCiclo; }

    public boolean isSempreVisivel() { return sempreVisivel; }
    public void setSempreVisivel(boolean sempreVisivel) { this.sempreVisivel = sempreVisivel; }
}