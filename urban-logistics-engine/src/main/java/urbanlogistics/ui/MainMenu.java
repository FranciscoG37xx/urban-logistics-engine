/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.ui;

/**
 *
 * @author franc
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import urbanlogistics.algorithms.PackingStrategy;
import urbanlogistics.model.DeliveryWindow;
import urbanlogistics.model.Encomenda;
import urbanlogistics.model.Operacao;
import urbanlogistics.model.Veiculo;
import urbanlogistics.persistence.StateStorage;
import urbanlogistics.service.GestorEncomendas;
import urbanlogistics.service.HistoricoOperacoes;

/** Interface de texto. Não contém lógica de negócio: delega tudo no GestorEncomendas. */
public class MainMenu {

    private final Path ficheiroEstado;
    private final Scanner in = new Scanner(System.in);
    private GestorEncomendas gestor = new GestorEncomendas();
    private long relogio = 0;  // minutos desde o início da simulação; só avança

    public MainMenu(Path ficheiroEstado) {
        this.ficheiroEstado = ficheiroEstado;
    }

    public void run() {
        carregarInicial();
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            try {
                continuar = executar(lerLinha("Opção: "));
            } catch (EndOfInput e) {
                continuar = false;
            } catch (IllegalArgumentException | IllegalStateException
                     | NoSuchElementException | IOException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("=== Logística urbana (relógio: " + relogio + " min) ===");
        System.out.println(" 1. Registar veículo           9. Desfazer última atribuição");
        System.out.println(" 2. Receber encomenda         10. Refazer atribuição");
        System.out.println(" 3. Listar pendentes          11. Adicionar janela de entrega");
        System.out.println(" 4. Atribuir a mais prioritária 12. Otimizar janelas (intervalos)");
        System.out.println(" 5. Carregar veículo (greedy) 13. Histórico (navegar)");
        System.out.println(" 6. Carregar veículo (knapsack) 14. Listar veículos");
        System.out.println(" 7. Comparar greedy e knapsack 15. Avançar relógio");
        System.out.println(" 8. Cancelar encomenda        16. Carregar de ficheiro");
        System.out.println(" 0. Guardar e sair            17. Sair sem guardar");
    }

    private boolean executar(String opcao) throws IOException {
        switch (opcao) {
            case "1": registarVeiculo(); break;
            case "2": receberEncomenda(); break;
            case "3": listarPendentes(); break;
            case "4": atribuirProxima(); break;
            case "5": carregarVeiculo(PackingStrategy.GREEDY); break;
            case "6": carregarVeiculo(PackingStrategy.KNAPSACK); break;
            case "7": compararEstrategias(); break;
            case "8": cancelar(); break;
            case "9": desfazer(); break;
            case "10": refazer(); break;
            case "11": adicionarJanela(); break;
            case "12": otimizarJanelas(); break;
            case "13": historico(); break;
            case "14": listarVeiculos(); break;
            case "15": avancarRelogio(); break;
            case "16": carregarFicheiro(); break;
            case "0":
                StateStorage.guardar(ficheiroEstado, gestor, relogio);
                System.out.println("Estado guardado em " + ficheiroEstado + ".");
                return false;
            case "17":
                System.out.println("A sair sem guardar.");
                return false;
            default:
                System.out.println("Opção inválida.");
        }
        return true;
    }

    // ---- Ações ----

    private void registarVeiculo() {
        String id = lerId("Id do veículo: ");
        double peso = lerDouble("Capacidade de peso (kg): ");
        double volume = lerDouble("Capacidade de volume (m3): ");
        gestor.registarVeiculo(new Veiculo(id, peso, volume));
        System.out.println("Veículo registado.");
    }

    private void receberEncomenda() {
        String id = lerId("Id da encomenda: ");
        double peso = lerDouble("Peso (kg): ");
        double volume = lerDouble("Volume (m3): ");
        double taxa = lerDouble("Taxa paga (€): ");
        int urgencia = (int) lerLong("Urgência (" + Encomenda.URGENCIA_MIN + " a "
                + Encomenda.URGENCIA_MAX + "): ");
        Encomenda e = new Encomenda(id, peso, volume, relogio, taxa, urgencia);
        gestor.receberEncomenda(e, relogio);
        System.out.printf("Encomenda registada (prioridade %.1f).%n", e.calcularPrioridade(relogio));
    }

    private void listarPendentes() {
        List<Encomenda> pendentes = gestor.listarPendentes();
        if (pendentes.isEmpty()) {
            System.out.println("Não há encomendas pendentes.");
            return;
        }
        for (int i = 0; i < pendentes.size(); i++) {
            Encomenda e = pendentes.get(i);
            System.out.printf("%d. %s | prioridade=%.1f%n", i + 1, e, e.calcularPrioridade(relogio));
        }
    }

    private void atribuirProxima() {
        String idVeiculo = lerId("Id do veículo: ");
        Encomenda e = gestor.atribuirProxima(idVeiculo, relogio);
        System.out.println("Atribuída: " + e);
    }

    private void carregarVeiculo(PackingStrategy estrategia) {
        String idVeiculo = lerId("Id do veículo: ");
        List<Encomenda> escolhidas = gestor.carregarVeiculo(idVeiculo, relogio, estrategia);
        System.out.println(estrategia + " carregou: " + resumo(escolhidas));
    }

    /** Mostra o que cada estratégia escolheria, sem alterar nada. */
    private void compararEstrategias() {
        String idVeiculo = lerId("Id do veículo: ");
        for (PackingStrategy estrategia : PackingStrategy.values()) {
            System.out.println(estrategia + ": "
                    + resumo(gestor.simularSelecao(idVeiculo, relogio, estrategia)));
        }
    }

    private void cancelar() {
        gestor.cancelar(lerId("Id da encomenda: "), relogio);
        System.out.println("Encomenda cancelada.");
    }

    private void desfazer() {
        System.out.println("Desfeito: " + gestor.desfazerAtribuicao());
    }

    private void refazer() {
        System.out.println("Refeito: " + gestor.refazerAtribuicao());
    }

    private void adicionarJanela() {
        String id = lerId("Id da janela: ");
        String idEncomenda = lerId("Id da encomenda: ");
        long inicio = lerLong("Início (min): ");
        long fim = lerLong("Fim (min): ");
        gestor.adicionarJanela(new DeliveryWindow(id, idEncomenda, inicio, fim));
        System.out.println("Janela adicionada.");
    }

    private void otimizarJanelas() {
        List<DeliveryWindow> escolhidas = gestor.otimizarJanelas();
        System.out.println(escolhidas.size() + " de " + gestor.getJanelas().size()
                + " janelas cabem sem sobreposição:");
        for (DeliveryWindow j : escolhidas) {
            System.out.println("  " + j);
        }
    }

    private void historico() {
        HistoricoOperacoes h = gestor.getHistorico();
        if (h.listar().isEmpty()) {
            System.out.println("O histórico está vazio.");
            return;
        }
        h.irParaFim();
        while (true) {
            Optional<Operacao> atual = h.atual();
            System.out.println("Operação atual: " + (atual.isPresent() ? atual.get() : "-"));
            String cmd = lerLinha("[a] anterior [d] seguinte [i] início [f] fim [l] listar [q] voltar: ");
            switch (cmd) {
                case "a":
                    if (!h.recuar()) System.out.println("Já está no início.");
                    break;
                case "d":
                    if (!h.avancar()) System.out.println("Já está no fim.");
                    break;
                case "i": h.irParaInicio(); break;
                case "f": h.irParaFim(); break;
                case "l":
                    int numero = 1;
                    for (Operacao o : h.listar()) {
                        System.out.println(numero++ + ". " + o);
                    }
                    break;
                case "q": return;
                default: System.out.println("Comando inválido.");
            }
        }
    }

    private void listarVeiculos() {
        if (gestor.getVeiculos().isEmpty()) {
            System.out.println("Não há veículos registados.");
            return;
        }
        for (Veiculo v : gestor.getVeiculos()) {
            System.out.println(v);
        }
    }

    private void avancarRelogio() {
        long minutos = lerLong("Minutos a avançar: ");
        if (minutos <= 0) {
            throw new IllegalArgumentException("Tem de avançar pelo menos 1 minuto.");
        }
        relogio += minutos;
    }

    private void carregarFicheiro() throws IOException {
        Path caminho = Paths.get(lerLinha("Caminho do ficheiro: "));
        GestorEncomendas novo = new GestorEncomendas();
        long novoRelogio = StateStorage.carregar(caminho, novo);
        // Só substitui o estado atual se o ficheiro foi lido por inteiro.
        gestor = novo;
        relogio = novoRelogio;
        System.out.println("Ficheiro carregado.");
    }

    private void carregarInicial() {
        if (!Files.exists(ficheiroEstado)) {
            return;
        }
        try {
            GestorEncomendas novo = new GestorEncomendas();
            relogio = StateStorage.carregar(ficheiroEstado, novo);
            gestor = novo;
            System.out.println("Estado carregado de " + ficheiroEstado + ".");
        } catch (IOException e) {
            System.out.println("Não foi possível carregar " + ficheiroEstado + " (" + e.getMessage()
                    + "). A começar com o sistema vazio.");
        }
    }

    // ---- Auxiliares ----

    private String resumo(List<Encomenda> escolhidas) {
        if (escolhidas.isEmpty()) {
            return "nenhuma encomenda";
        }
        StringBuilder ids = new StringBuilder();
        double valor = 0;
        double peso = 0;
        double volume = 0;
        for (Encomenda e : escolhidas) {
            if (ids.length() > 0) {
                ids.append(", ");
            }
            ids.append(e.getId());
            valor += e.calcularPrioridade(relogio);
            peso += e.getPeso();
            volume += e.getVolume();
        }
        return String.format("%s | valor total = %.1f | peso = %.1f kg | volume = %.2f m3",
                ids, valor, peso, volume);
    }

    private String lerLinha(String pergunta) {
        System.out.print(pergunta);
        if (!in.hasNextLine()) {
            throw new EndOfInput();
        }
        return in.nextLine().trim();
    }

    private String lerId(String pergunta) {
        String texto = lerLinha(pergunta);
        if (texto.isEmpty() || texto.contains(";")) {
            throw new IllegalArgumentException("Identificador inválido (vazio ou com ';').");
        }
        return texto;
    }

    private double lerDouble(String pergunta) {
        try {
            double valor = Double.parseDouble(lerLinha(pergunta).replace(',', '.'));
            if (!Double.isFinite(valor)) {
                throw new IllegalArgumentException("O valor tem de ser um número finito.");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido.");
        }
    }

    private long lerLong(String pergunta) {
        try {
            return Long.parseLong(lerLinha(pergunta));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número inteiro inválido.");
        }
    }

    /** Sinaliza que o input terminou (por exemplo, Ctrl+D). */
    private static final class EndOfInput extends RuntimeException { }
}
