/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.model;

/**
 *
 * @author franc
 */

/** Janela temporal [inicio, fim) em que uma encomenda pode ser entregue. */
public class DeliveryWindow {

    private final String id;
    private final String idEncomenda;
    private final long inicio;  // minutos desde o início da simulação
    private final long fim;

    public DeliveryWindow(String id, String idEncomenda, long inicio, long fim) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador da janela é obrigatório.");
        }
        if (idEncomenda == null || idEncomenda.isBlank()) {
            throw new IllegalArgumentException("O identificador da encomenda é obrigatório.");
        }
        if (inicio < 0) {
            throw new IllegalArgumentException("O início não pode ser negativo.");
        }
        if (fim <= inicio) {
            throw new IllegalArgumentException("O fim tem de ser posterior ao início.");
        }
        this.id = id.trim();
        this.idEncomenda = idEncomenda.trim();
        this.inicio = inicio;
        this.fim = fim;
    }

    public String getId() { return id; }
    public String getIdEncomenda() { return idEncomenda; }
    public long getInicio() { return inicio; }
    public long getFim() { return fim; }

    @Override
    public String toString() {
        return String.format("Janela[%s | encomenda %s | [%d, %d)]", id, idEncomenda, inicio, fim);
    }
}
