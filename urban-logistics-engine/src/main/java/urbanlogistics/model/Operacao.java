/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.model;

import java.util.Objects;

public class Operacao {

    private final TipoOperacao tipo;
    private final Encomenda encomenda;
    private final String idVeiculo;   // só em ATRIBUICAO
    private final long instante;      // minutos desde o início da simulação

    public Operacao(TipoOperacao tipo, Encomenda encomenda, String idVeiculo, long instante) {
        this.tipo = Objects.requireNonNull(tipo, "O tipo é obrigatório.");
        this.encomenda = Objects.requireNonNull(encomenda, "A encomenda é obrigatória.");
        if (instante < 0) {
            throw new IllegalArgumentException("O instante não pode ser negativo.");
        }
        if (tipo == TipoOperacao.ATRIBUICAO && (idVeiculo == null || idVeiculo.isBlank())) {
            throw new IllegalArgumentException("Uma atribuição requer um veículo.");
        }
        this.idVeiculo = idVeiculo;
        this.instante = instante;
    }

    public TipoOperacao getTipo() { return tipo; }
    public Encomenda getEncomenda() { return encomenda; }
    public String getIdVeiculo() { return idVeiculo; }
    public long getInstante() { return instante; }

    @Override
    public String toString() {
        return "[t=" + instante + "] " + tipo + " " + encomenda.getId()
                + (idVeiculo != null ? " -> veículo " + idVeiculo : "");
    }
}
