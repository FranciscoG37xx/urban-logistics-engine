/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.model;

/**
 *
 * @author franc
 */
import java.util.Objects;

/**
 * Encomenda a processar. A prioridade depende da urgência, da taxa paga
 * e do tempo de espera (ver docs/decisoes.md).
 */
public class Encomenda {

    public static final int URGENCIA_MIN = 1;
    public static final int URGENCIA_MAX = 5;

    // "Taxas de câmbio" entre fatores: 1 nível de urgência vale 10 € de taxa
    // e 200 minutos de espera.
    private static final double PESO_URGENCIA = 20.0;
    private static final double PESO_TAXA = 2.0;
    private static final double PESO_ESPERA = 0.1;

    private final String id;
    private final double peso;        // kg
    private final double volume;      // m3
    private final long tempoChegada;  // minutos desde o início da simulação
    private final double taxa;        // euros
    private final int urgencia;       // URGENCIA_MIN..URGENCIA_MAX

    public Encomenda(String id, double peso, double volume,
                     long tempoChegada, double taxa, int urgencia) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador é obrigatório.");
        }
        if (peso <= 0 || volume <= 0) {
            throw new IllegalArgumentException("Peso e volume têm de ser positivos.");
        }
        if (tempoChegada < 0) {
            throw new IllegalArgumentException("O tempo de chegada não pode ser negativo.");
        }
        if (taxa < 0) {
            throw new IllegalArgumentException("A taxa não pode ser negativa.");
        }
        if (urgencia < URGENCIA_MIN || urgencia > URGENCIA_MAX) {
            throw new IllegalArgumentException("A urgência tem de estar entre "
                    + URGENCIA_MIN + " e " + URGENCIA_MAX + ".");
        }
        this.id = id.trim();
        this.peso = peso;
        this.volume = volume;
        this.tempoChegada = tempoChegada;
        this.taxa = taxa;
        this.urgencia = urgencia;
    }

    /**
     * Prioridade no instante dado: sobe linearmente com o tempo de espera.
     */
    public double calcularPrioridade(long tempoAtual) {
        if (tempoAtual < tempoChegada) {
            throw new IllegalArgumentException("O instante atual é anterior à chegada.");
        }
        return PESO_URGENCIA * urgencia
                + PESO_TAXA * taxa
                + PESO_ESPERA * (tempoAtual - tempoChegada);
    }

    /**
     * Chave de ordenação fixa, equivalente a calcularPrioridade() a menos de
     * uma constante comum a todas as encomendas. É a que o heap vai usar.
     */
    public double pontuacaoBase() {
        return PESO_URGENCIA * urgencia
                + PESO_TAXA * taxa
                - PESO_ESPERA * tempoChegada;
    }

    public String getId() { return id; }
    public double getPeso() { return peso; }
    public double getVolume() { return volume; }
    public long getTempoChegada() { return tempoChegada; }
    public double getTaxa() { return taxa; }
    public int getUrgencia() { return urgencia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Encomenda)) return false;
        return id.equals(((Encomenda) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Encomenda[%s | %.1f kg | %.2f m3 | chegada=%d | taxa=%.2f € | urgência=%d]",
                id, peso, volume, tempoChegada, taxa, urgencia);
    }
}
