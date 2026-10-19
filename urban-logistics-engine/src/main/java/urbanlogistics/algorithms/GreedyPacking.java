/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import urbanlogistics.model.Encomenda;
import urbanlogistics.model.Veiculo;

/**
 * Seleção greedy das encomendas a carregar num veículo com limite de peso e volume.
 * Ordena por "valor por recurso consumido" e vai juntando o que ainda cabe.
 * É uma heurística: não garante a solução ótima (ver docs/decisoes.md).
 */
public final class GreedyPacking {

    private GreedyPacking() { }

    /**
     * Escolhe encomendas para a capacidade que ainda sobra no veículo. Não altera nada.
     * O(n log n) no tempo, O(n) no espaço.
     *
     * @param instante instante usado para calcular a prioridade (tem de ser
     *                 posterior à chegada de todas as candidatas)
     * @return as encomendas escolhidas, pela ordem em que foram aceites
     */
    public static List<Encomenda> selecionar(Collection<Encomenda> candidatas,
                                             Veiculo veiculo, long instante) {
        Objects.requireNonNull(candidatas, "As candidatas são obrigatórias.");
        Objects.requireNonNull(veiculo, "O veículo é obrigatório.");

        double pesoUsado = veiculo.pesoCarregado();
        double volumeUsado = veiculo.volumeCarregado();
        double pesoLivre = veiculo.getCapacidadePeso() - pesoUsado;
        double volumeLivre = veiculo.getCapacidadeVolume() - volumeUsado;

        // 1. Descarta as que nem sozinhas cabem e calcula a densidade das restantes.
        List<Candidata> lista = new ArrayList<>();
        for (Encomenda e : candidatas) {
            if (cabe(e, pesoUsado, volumeUsado, veiculo)) {
                double valor = e.calcularPrioridade(instante);
                double custo = Math.max(e.getPeso() / pesoLivre, e.getVolume() / volumeLivre);
                lista.add(new Candidata(e, valor, valor / custo));
            }
        }

        // 2. Da maior para a menor densidade.
        lista.sort(GreedyPacking::comparar);

        // 3. Aceita enquanto cabe.
        List<Encomenda> escolhidas = new ArrayList<>();
        for (Candidata c : lista) {
            if (cabe(c.encomenda, pesoUsado, volumeUsado, veiculo)) {
                escolhidas.add(c.encomenda);
                pesoUsado += c.encomenda.getPeso();
                volumeUsado += c.encomenda.getVolume();
            }
        }
        return escolhidas;
    }

    // Mesma expressão que Veiculo.cabe(), para não haver diferenças de arredondamento.
    private static boolean cabe(Encomenda e, double pesoUsado, double volumeUsado, Veiculo v) {
        return pesoUsado + e.getPeso() <= v.getCapacidadePeso()
                && volumeUsado + e.getVolume() <= v.getCapacidadeVolume();
    }

    // Maior densidade primeiro; empate: maior valor; depois menor id.
    private static int comparar(Candidata a, Candidata b) {
        int c = Double.compare(b.densidade, a.densidade);
        if (c != 0) {
            return c;
        }
        c = Double.compare(b.valor, a.valor);
        if (c != 0) {
            return c;
        }
        return a.encomenda.getId().compareTo(b.encomenda.getId());
    }

    private static final class Candidata {
        private final Encomenda encomenda;
        private final double valor;
        private final double densidade;

        private Candidata(Encomenda encomenda, double valor, double densidade) {
            this.encomenda = encomenda;
            this.valor = valor;
            this.densidade = densidade;
        }
    }
}
