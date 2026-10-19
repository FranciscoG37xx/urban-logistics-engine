/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Veículo com capacidade limitada de peso e volume. */
public class Veiculo {

    private final String id;
    private final double capacidadePeso;    // kg
    private final double capacidadeVolume;  // m3
    private final List<Encomenda> carga = new ArrayList<>();

    public Veiculo(String id, double capacidadePeso, double capacidadeVolume) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador é obrigatório.");
        }
        if (capacidadePeso <= 0 || capacidadeVolume <= 0) {
            throw new IllegalArgumentException("As capacidades têm de ser positivas.");
        }
        this.id = id.trim();
        this.capacidadePeso = capacidadePeso;
        this.capacidadeVolume = capacidadeVolume;
    }

    public double pesoCarregado() {
        double total = 0;
        for (Encomenda e : carga) {
            total += e.getPeso();
        }
        return total;
    }

    public double volumeCarregado() {
        double total = 0;
        for (Encomenda e : carga) {
            total += e.getVolume();
        }
        return total;
    }

    /** Indica se a encomenda cabe na capacidade restante (peso e volume). */
    public boolean cabe(Encomenda e) {
        Objects.requireNonNull(e, "A encomenda é obrigatória.");
        return pesoCarregado() + e.getPeso() <= capacidadePeso
                && volumeCarregado() + e.getVolume() <= capacidadeVolume;
    }

    public void carregar(Encomenda e) {
        if (!cabe(e)) {
            throw new IllegalStateException("A encomenda " + e.getId()
                    + " não cabe no veículo " + id + ".");
        }
        carga.add(e);
    }

    public void descarregar(Encomenda e) {
        if (!carga.remove(e)) {
            throw new IllegalArgumentException("A encomenda " + e.getId()
                    + " não está no veículo " + id + ".");
        }
    }

    public String getId() { return id; }
    public double getCapacidadePeso() { return capacidadePeso; }
    public double getCapacidadeVolume() { return capacidadeVolume; }

    public List<Encomenda> getCarga() {
        return Collections.unmodifiableList(carga);
    }

    @Override
    public String toString() {
        return String.format("Veiculo[%s | peso %.1f/%.1f kg | volume %.2f/%.2f m3 | %d encomendas]",
                id, pesoCarregado(), capacidadePeso, volumeCarregado(), capacidadeVolume, carga.size());
    }
}
