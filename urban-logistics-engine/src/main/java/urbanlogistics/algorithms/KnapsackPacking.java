/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.algorithms;

/**
 *
 * @author franc
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import urbanlogistics.model.Encomenda;
import urbanlogistics.model.Veiculo;

/**
 * Mochila 0/1 com duas restrições (peso e volume), resolvida por programação dinâmica.
 * A capacidade livre de cada dimensão é dividida em "resolucao" unidades e os tamanhos
 * das encomendas são arredondados por excesso, por isso a solução nunca excede a capacidade
 * real (ver docs/decisoes.md).
 */
public final class KnapsackPacking {

    public static final int DEFAULT_RESOLUTION = 100;

    private KnapsackPacking() { }

    public static List<Encomenda> selecionar(Collection<Encomenda> candidatas,
                                             Veiculo veiculo, long instante) {
        return selecionar(candidatas, veiculo, instante, DEFAULT_RESOLUTION);
    }

    /**
     * Escolhe o subconjunto de maior valor total (prioridade no instante dado).
     * Tempo O(n · R²) e espaço O(n · R²) para reconstruir a solução, com R = resolucao.
     * Não altera o veículo nem as encomendas.
     */
    public static List<Encomenda> selecionar(Collection<Encomenda> candidatas,
                                             Veiculo veiculo, long instante, int resolucao) {
        Objects.requireNonNull(candidatas, "As candidatas são obrigatórias.");
        Objects.requireNonNull(veiculo, "O veículo é obrigatório.");
        if (resolucao < 1) {
            throw new IllegalArgumentException("A resolução tem de ser pelo menos 1.");
        }

        List<Encomenda> escolhidas = new ArrayList<>();
        double pesoLivre = veiculo.getCapacidadePeso() - veiculo.pesoCarregado();
        double volumeLivre = veiculo.getCapacidadeVolume() - veiculo.volumeCarregado();
        if (pesoLivre <= 0 || volumeLivre <= 0) {
            return escolhidas;
        }
        double unidadePeso = pesoLivre / resolucao;
        double unidadeVolume = volumeLivre / resolucao;

        // 1. Custos inteiros (arredondados por excesso); descarta as que nem sozinhas cabem.
        int max = candidatas.size();
        Encomenda[] itens = new Encomenda[max];
        int[] custoPeso = new int[max];
        int[] custoVolume = new int[max];
        double[] valor = new double[max];
        int n = 0;
        for (Encomenda e : candidatas) {
            int cp = custo(e.getPeso(), unidadePeso);
            int cv = custo(e.getVolume(), unidadeVolume);
            if (cp <= resolucao && cv <= resolucao) {
                itens[n] = e;
                custoPeso[n] = cp;
                custoVolume[n] = cv;
                valor[n] = e.calcularPrioridade(instante);
                n++;
            }
        }
        if (n == 0) {
            return escolhidas;
        }

        // 2. melhor[p][v] = maior valor com no máximo p unidades de peso e v de volume.
        //    tomada[i][p][v] regista se o item i melhorou essa célula (para reconstruir).
        double[][] melhor = new double[resolucao + 1][resolucao + 1];
        boolean[][][] tomada = new boolean[n][resolucao + 1][resolucao + 1];
        for (int i = 0; i < n; i++) {
            // Percorre do fim para o início para cada item ser usado no máximo uma vez.
            for (int p = resolucao; p >= custoPeso[i]; p--) {
                for (int v = resolucao; v >= custoVolume[i]; v--) {
                    double candidato = melhor[p - custoPeso[i]][v - custoVolume[i]] + valor[i];
                    if (candidato > melhor[p][v]) {
                        melhor[p][v] = candidato;
                        tomada[i][p][v] = true;
                    }
                }
            }
        }

        // 3. Reconstrução: do último item para o primeiro.
        List<Encomenda> tomadas = new ArrayList<>();
        int p = resolucao;
        int v = resolucao;
        for (int i = n - 1; i >= 0; i--) {
            if (tomada[i][p][v]) {
                tomadas.add(itens[i]);
                p -= custoPeso[i];
                v -= custoVolume[i];
            }
        }
        Collections.reverse(tomadas);

        // 4. Confirmação final com a mesma expressão do Veiculo.cabe(), para evitar
        //    diferenças de arredondamento em casos no limite.
        double pesoUsado = veiculo.pesoCarregado();
        double volumeUsado = veiculo.volumeCarregado();
        for (Encomenda e : tomadas) {
            if (pesoUsado + e.getPeso() <= veiculo.getCapacidadePeso()
                    && volumeUsado + e.getVolume() <= veiculo.getCapacidadeVolume()) {
                escolhidas.add(e);
                pesoUsado += e.getPeso();
                volumeUsado += e.getVolume();
            }
        }
        return escolhidas;
    }

    private static int custo(double tamanho, double unidade) {
        double c = Math.ceil(tamanho / unidade);
        return c >= Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, (int) c);
    }
}
