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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import urbanlogistics.model.DeliveryWindow;

/**
 * Seleção de intervalos: o maior número de janelas sem sobreposição que um veículo
 * consegue cumprir. Estratégia gulosa: ordenar pelo fim e aceitar a primeira que começa
 * depois (ou quando) a última aceite termina.
 */
public final class IntervalScheduling {

    private IntervalScheduling() { }

    /** O(n log n) no tempo (a ordenação domina), O(n) no espaço. */
    public static List<DeliveryWindow> selecionar(Collection<DeliveryWindow> janelas) {
        Objects.requireNonNull(janelas, "As janelas são obrigatórias.");
        List<DeliveryWindow> ordenadas = new ArrayList<>(janelas);
        ordenadas.sort(Comparator.comparingLong(DeliveryWindow::getFim)
                .thenComparingLong(DeliveryWindow::getInicio)
                .thenComparing(DeliveryWindow::getId));

        List<DeliveryWindow> escolhidas = new ArrayList<>();
        long ultimoFim = Long.MIN_VALUE;
        for (DeliveryWindow janela : ordenadas) {
            // Intervalos semiabertos: começar quando a anterior termina é compatível.
            if (janela.getInicio() >= ultimoFim) {
                escolhidas.add(janela);
                ultimoFim = janela.getFim();
            }
        }
        return escolhidas;
    }
}
