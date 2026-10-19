/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import urbanlogistics.model.Operacao;
import urbanlogistics.model.TipoOperacao;
import urbanlogistics.structures.DoublyLinkedList;
import urbanlogistics.structures.DoublyLinkedList.No;

public class HistoricoOperacoes {

    // Registo completo (só cresce) e cursor de navegação.
    private final DoublyLinkedList<Operacao> registo = new DoublyLinkedList<>();
    private No<Operacao> navegacao;

    // Linha do tempo das atribuições para Undo/Redo.
    // ultimaAplicada == null significa que nenhuma está aplicada.
    private final DoublyLinkedList<Operacao> atribuicoes = new DoublyLinkedList<>();
    private No<Operacao> ultimaAplicada;

        /**
     * Regista uma operação. Uma atribuição ou um cancelamento descartam as
     * atribuições desfeitas, porque deixam de poder ser refeitas.
     */
    public void registar(Operacao operacao) {
        Objects.requireNonNull(operacao, "A operação é obrigatória.");
        navegacao = registo.adicionarNoFim(operacao);
        if (operacao.getTipo() != TipoOperacao.ENTRADA) {
            atribuicoes.truncarApos(ultimaAplicada);
        }
        if (operacao.getTipo() == TipoOperacao.ATRIBUICAO) {
            ultimaAplicada = atribuicoes.adicionarNoFim(operacao);
        }
    }

    // ---- Navegação (só leitura) ----

    public Optional<Operacao> atual() {
        return navegacao == null ? Optional.empty() : Optional.of(navegacao.getElemento());
    }

    /** Recua uma operação. Devolve false se já estava no início. */
    public boolean recuar() {
        if (navegacao == null || navegacao.getAnterior() == null) {
            return false;
        }
        navegacao = navegacao.getAnterior();
        return true;
    }

    /** Avança uma operação. Devolve false se já estava no fim. */
    public boolean avancar() {
        if (navegacao == null || navegacao.getSeguinte() == null) {
            return false;
        }
        navegacao = navegacao.getSeguinte();
        return true;
    }

    public void irParaInicio() { navegacao = registo.getCabeca(); }

    public void irParaFim() { navegacao = registo.getCauda(); }

    public List<Operacao> listar() {
        List<Operacao> copia = new ArrayList<>(registo.tamanho());
        for (Operacao o : registo) {
            copia.add(o);
        }
        return copia;
    }

    // ---- Undo / Redo (só atribuições) ----

    public boolean podeDesfazer() { return ultimaAplicada != null; }

    public boolean podeRefazer() {
        return proximaARefazer() != null;
    }

    /** Devolve a atribuição a reverter; o chamador aplica o efeito inverso. */
    public Operacao desfazer() {
        if (ultimaAplicada == null) {
            throw new IllegalStateException("Não há atribuições para desfazer.");
        }
        Operacao operacao = ultimaAplicada.getElemento();
        ultimaAplicada = ultimaAplicada.getAnterior();
        return operacao;
    }

    /** Devolve a atribuição a reaplicar; o chamador aplica o efeito. */
    public Operacao refazer() {
        No<Operacao> proxima = proximaARefazer();
        if (proxima == null) {
            throw new IllegalStateException("Não há atribuições para refazer.");
        }
        ultimaAplicada = proxima;
        return proxima.getElemento();
    }

    private No<Operacao> proximaARefazer() {
        return ultimaAplicada == null ? atribuicoes.getCabeca() : ultimaAplicada.getSeguinte();
    }
}
