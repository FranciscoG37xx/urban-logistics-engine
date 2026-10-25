/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.service;

import urbanlogistics.algorithms.IntervalScheduling;
import urbanlogistics.algorithms.KnapsackPacking;
import urbanlogistics.algorithms.PackingStrategy;
import urbanlogistics.model.DeliveryWindow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import urbanlogistics.model.Encomenda;
import urbanlogistics.model.Operacao;
import urbanlogistics.model.TipoOperacao;
import urbanlogistics.model.Veiculo;
import urbanlogistics.structures.BinaryHeap;
import urbanlogistics.algorithms.GreedyPacking;

/**
 * Coordena a fila de prioridades (heap), os veículos e o histórico.
 * Os métodos validam tudo antes de alterar o estado, para que uma
 * falha nunca deixe o sistema a meio de uma operação.
 */
public class GestorEncomendas {

    private final Comparator<Encomenda> comparador;
    private final BinaryHeap<Encomenda> fila;
    private final Map<String, Encomenda> pendentes = new HashMap<>();
    private final Set<String> idsUsados = new HashSet<>();
    private final Map<String, Veiculo> veiculos = new LinkedHashMap<>();
    private final HistoricoOperacoes historico = new HistoricoOperacoes();
        private final List<DeliveryWindow> janelas = new ArrayList<>();

    public GestorEncomendas() {
        // Maior pontuação primeiro; em empate, menor identificador primeiro.
        this.comparador = Comparator.comparingDouble(Encomenda::pontuacaoBase)
                .thenComparing(Encomenda::getId, Comparator.reverseOrder());
        this.fila = new BinaryHeap<>(comparador);
    }

    // ---- Veículos ----

    public void registarVeiculo(Veiculo veiculo) {
        Objects.requireNonNull(veiculo, "O veículo é obrigatório.");
        if (veiculos.containsKey(veiculo.getId())) {
            throw new IllegalArgumentException("Já existe o veículo " + veiculo.getId() + ".");
        }
        veiculos.put(veiculo.getId(), veiculo);
    }

    public Collection<Veiculo> getVeiculos() {
        return Collections.unmodifiableCollection(veiculos.values());
    }

    // ---- Entrada, atribuição e cancelamento ----

    /** Insere a encomenda na fila e regista a ENTRADA. O(log n). */
    public void receberEncomenda(Encomenda encomenda, long instante) {
        Objects.requireNonNull(encomenda, "A encomenda é obrigatória.");
        Operacao operacao = new Operacao(TipoOperacao.ENTRADA, encomenda, null, instante);
        if (!idsUsados.add(encomenda.getId())) {
            throw new IllegalArgumentException("O identificador " + encomenda.getId()
                    + " já foi utilizado.");
        }
        fila.inserir(encomenda);
        pendentes.put(encomenda.getId(), encomenda);
        historico.registar(operacao);
    }

    /** Atribui a encomenda mais prioritária ao veículo. O(log n). */
    public Encomenda atribuirProxima(String idVeiculo, long instante) {
        Veiculo veiculo = obterVeiculo(idVeiculo);
        if (fila.estaVazio()) {
            throw new IllegalStateException("Não há encomendas pendentes.");
        }
        Encomenda topo = fila.espreitar();
        validarCabe(veiculo, topo);
        Operacao operacao = new Operacao(TipoOperacao.ATRIBUICAO, topo, veiculo.getId(), instante);
        fila.remover();
        efetivarAtribuicao(topo, veiculo, operacao);
        return topo;
    }

    /** Atribui uma encomenda concreta ao veículo (usado pelo greedy). O(n). */
    public void atribuir(String idEncomenda, String idVeiculo, long instante) {
        Veiculo veiculo = obterVeiculo(idVeiculo);
        Encomenda encomenda = obterPendente(idEncomenda);
        validarCabe(veiculo, encomenda);
        Operacao operacao = new Operacao(TipoOperacao.ATRIBUICAO, encomenda, veiculo.getId(), instante);
        fila.remover(encomenda);
        efetivarAtribuicao(encomenda, veiculo, operacao);
    }

    /** Cancela uma encomenda ainda pendente. O(n). */
    public void cancelar(String idEncomenda, long instante) {
        Encomenda encomenda = obterPendente(idEncomenda);
        Operacao operacao = new Operacao(TipoOperacao.CANCELAMENTO, encomenda, null, instante);
        fila.remover(encomenda);
        pendentes.remove(idEncomenda);
        janelas.removeIf(j -> j.getIdEncomenda().equals(idEncomenda));
        historico.registar(operacao);
    }
    
          /** Mostra o que a estratégia escolheria para o veículo, sem alterar o estado. */
    public List<Encomenda> simularSelecao(String idVeiculo, long instante, PackingStrategy estrategia) {
        Objects.requireNonNull(estrategia, "A estratégia é obrigatória.");
        Veiculo veiculo = obterVeiculo(idVeiculo);
        List<Encomenda> candidatas = fila.elementos();
        if (estrategia == PackingStrategy.KNAPSACK) {
            return KnapsackPacking.selecionar(candidatas, veiculo, instante);
        }
        return GreedyPacking.selecionar(candidatas, veiculo, instante);
    }

    /**
     * Carrega o veículo com as encomendas escolhidas pela estratégia. A seleção é feita
     * antes de alterar o estado e cabe sempre no veículo. Cada encomenda fica como uma
     * atribuição separada (o Undo desfaz uma de cada vez).
     */
    public List<Encomenda> carregarVeiculo(String idVeiculo, long instante, PackingStrategy estrategia) {
        List<Encomenda> escolhidas = simularSelecao(idVeiculo, instante, estrategia);
        for (Encomenda e : escolhidas) {
            atribuir(e.getId(), idVeiculo, instante);
        }
        return escolhidas;
    }

    // ---- Janelas de entrega ----

    public void adicionarJanela(DeliveryWindow janela) {
        Objects.requireNonNull(janela, "A janela é obrigatória.");
        if (!idsUsados.contains(janela.getIdEncomenda())) {
            throw new IllegalArgumentException("Encomenda desconhecida: " + janela.getIdEncomenda());
        }
        for (DeliveryWindow existente : janelas) {
            if (existente.getId().equals(janela.getId())) {
                throw new IllegalArgumentException("Já existe a janela " + janela.getId() + ".");
            }
        }
        janelas.add(janela);
    }

    public List<DeliveryWindow> getJanelas() {
        return Collections.unmodifiableList(janelas);
    }

    /** Maior conjunto de janelas sem sobreposição. O(n log n). */
    public List<DeliveryWindow> otimizarJanelas() {
        return IntervalScheduling.selecionar(janelas);
    }

    // ---- Undo / Redo de atribuições ----

    /** Reverte a última atribuição: descarrega o veículo e volta a pôr a encomenda na fila. */
    public Operacao desfazerAtribuicao() {
        Operacao operacao = historico.desfazer();
        Encomenda encomenda = operacao.getEncomenda();
        veiculos.get(operacao.getIdVeiculo()).descarregar(encomenda);
        fila.inserir(encomenda);
        pendentes.put(encomenda.getId(), encomenda);
        return operacao;
    }

    /** Reaplica a última atribuição desfeita. */
    public Operacao refazerAtribuicao() {
        Operacao operacao = historico.refazer();
        Encomenda encomenda = operacao.getEncomenda();
        fila.remover(encomenda);
        pendentes.remove(encomenda.getId());
        veiculos.get(operacao.getIdVeiculo()).carregar(encomenda);
        return operacao;
    }

    public boolean podeDesfazer() { return historico.podeDesfazer(); }

    public boolean podeRefazer() { return historico.podeRefazer(); }

    // ---- Consulta ----

    public Optional<Encomenda> proximaEncomenda() {
        return fila.estaVazio() ? Optional.empty() : Optional.of(fila.espreitar());
    }

    public int numeroPendentes() { return fila.tamanho(); }

    /** Pendentes da mais para a menos prioritária. O(n log n). */
    public List<Encomenda> listarPendentes() {
        List<Encomenda> copia = new ArrayList<>(fila.elementos());
        copia.sort(comparador.reversed());
        return copia;
    }

    public HistoricoOperacoes getHistorico() { return historico; }

    // ---- Auxiliares ----

    private void efetivarAtribuicao(Encomenda encomenda, Veiculo veiculo, Operacao operacao) {
        pendentes.remove(encomenda.getId());
        veiculo.carregar(encomenda);
        historico.registar(operacao);
    }

    private Veiculo obterVeiculo(String idVeiculo) {
        Veiculo veiculo = idVeiculo == null ? null : veiculos.get(idVeiculo.trim());
        if (veiculo == null) {
            throw new IllegalArgumentException("Veículo inexistente: " + idVeiculo);
        }
        return veiculo;
    }

    private Encomenda obterPendente(String idEncomenda) {
        Encomenda encomenda = idEncomenda == null ? null : pendentes.get(idEncomenda.trim());
        if (encomenda == null) {
            throw new IllegalArgumentException("Não existe encomenda pendente com o id: " + idEncomenda);
        }
        return encomenda;
    }

    private void validarCabe(Veiculo veiculo, Encomenda encomenda) {
        if (!veiculo.cabe(encomenda)) {
            throw new IllegalStateException("A encomenda " + encomenda.getId()
                    + " não cabe no veículo " + veiculo.getId() + ".");
        }
    }
}
