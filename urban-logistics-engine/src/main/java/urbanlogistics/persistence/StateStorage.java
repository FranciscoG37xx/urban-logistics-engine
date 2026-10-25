/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.persistence;

/**
 *
 * @author franc
 */
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import urbanlogistics.model.DeliveryWindow;
import urbanlogistics.model.Encomenda;
import urbanlogistics.model.Veiculo;
import urbanlogistics.service.GestorEncomendas;

/**
 * Guarda e carrega o estado num ficheiro de texto (um registo por linha, campos separados
 * por ';'). Registos: CLOCK, VEHICLE, ORDER, WINDOW. O histórico não é guardado: ao
 * carregar, o estado é reconstruído através da API pública do gestor.
 */
public final class StateStorage {

    private static final String SEP = ";";

    private StateStorage() { }

    public static void guardar(Path ficheiro, GestorEncomendas gestor, long relogio) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("# Urban logistics state");
        linhas.add(juntar("CLOCK", relogio));
        for (Veiculo v : gestor.getVeiculos()) {
            linhas.add(juntar("VEHICLE", v.getId(), v.getCapacidadePeso(), v.getCapacidadeVolume()));
        }
        for (Encomenda e : gestor.listarPendentes()) {
            linhas.add(linhaEncomenda(e, "PENDING", ""));
        }
        for (Veiculo v : gestor.getVeiculos()) {
            for (Encomenda e : v.getCarga()) {
                linhas.add(linhaEncomenda(e, "ASSIGNED", v.getId()));
            }
        }
        for (DeliveryWindow j : gestor.getJanelas()) {
            linhas.add(juntar("WINDOW", j.getId(), j.getIdEncomenda(), j.getInicio(), j.getFim()));
        }

        // Escreve num ficheiro temporário e só depois substitui, para nunca deixar
        // o ficheiro a meio se algo falhar.
        Path pasta = ficheiro.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        Path temporario = ficheiro.resolveSibling(ficheiro.getFileName() + ".tmp");
        Files.write(temporario, linhas, StandardCharsets.UTF_8);
        Files.move(temporario, ficheiro, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Carrega o ficheiro para um gestor vazio e devolve o relógio da simulação.
     * Em caso de erro lança IOException com o número da linha.
     */
    public static long carregar(Path ficheiro, GestorEncomendas gestor) throws IOException {
        List<String> linhas = Files.readAllLines(ficheiro, StandardCharsets.UTF_8);
        long relogio = 0;
        long chegadaMaxima = 0;
        List<Linha> atribuicoes = new ArrayList<>();
        List<Linha> janelas = new ArrayList<>();

        for (int i = 0; i < linhas.size(); i++) {
            String texto = linhas.get(i).trim();
            if (texto.isEmpty() || texto.startsWith("#")) {
                continue;
            }
            String[] f = texto.split(SEP, -1);
            try {
                switch (f[0]) {
                    case "CLOCK":
                        exigir(f, 2);
                        relogio = Long.parseLong(f[1]);
                        break;
                    case "VEHICLE":
                        exigir(f, 4);
                        gestor.registarVeiculo(new Veiculo(f[1],
                                Double.parseDouble(f[2]), Double.parseDouble(f[3])));
                        break;
                    case "ORDER": {
                        exigir(f, 9);
                        Encomenda e = new Encomenda(f[1], Double.parseDouble(f[2]),
                                Double.parseDouble(f[3]), Long.parseLong(f[4]),
                                Double.parseDouble(f[5]), Integer.parseInt(f[6]));
                        gestor.receberEncomenda(e, e.getTempoChegada());
                        chegadaMaxima = Math.max(chegadaMaxima, e.getTempoChegada());
                        if ("ASSIGNED".equals(f[7])) {
                            atribuicoes.add(new Linha(i + 1, f));
                        } else if (!"PENDING".equals(f[7])) {
                            throw new IllegalArgumentException("Estado desconhecido: " + f[7]);
                        }
                        break;
                    }
                    case "WINDOW":
                        exigir(f, 5);
                        janelas.add(new Linha(i + 1, f));
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de registo desconhecido: " + f[0]);
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                throw new IOException("Linha " + (i + 1) + ": " + ex.getMessage(), ex);
            }
        }

        // O relógio nunca fica antes da chegada de uma encomenda carregada.
        relogio = Math.max(relogio, chegadaMaxima);

        // Atribuições e janelas só se aplicam depois de todas as encomendas existirem.
        for (Linha l : atribuicoes) {
            try {
                gestor.atribuir(l.campos[1], l.campos[8], relogio);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                throw new IOException("Linha " + l.numero + ": " + ex.getMessage(), ex);
            }
        }
        for (Linha l : janelas) {
            try {
                gestor.adicionarJanela(new DeliveryWindow(l.campos[1], l.campos[2],
                        Long.parseLong(l.campos[3]), Long.parseLong(l.campos[4])));
            } catch (IllegalArgumentException | IllegalStateException ex) {
                throw new IOException("Linha " + l.numero + ": " + ex.getMessage(), ex);
            }
        }
        return relogio;
    }

    private static String linhaEncomenda(Encomenda e, String estado, String idVeiculo) throws IOException {
        return juntar("ORDER", e.getId(), e.getPeso(), e.getVolume(), e.getTempoChegada(),
                e.getTaxa(), e.getUrgencia(), estado, idVeiculo);
    }

    private static String juntar(Object... campos) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            String texto = String.valueOf(campos[i]);
            if (texto.contains(SEP) || texto.indexOf('\n') >= 0 || texto.indexOf('\r') >= 0) {
                throw new IOException("O valor \"" + texto
                        + "\" contém caracteres não permitidos (';' ou mudança de linha).");
            }
            if (i > 0) {
                sb.append(SEP);
            }
            sb.append(texto);
        }
        return sb.toString();
    }

    private static void exigir(String[] campos, int esperado) {
        if (campos.length != esperado) {
            throw new IllegalArgumentException("Esperava " + esperado + " campos em "
                    + campos[0] + " mas encontrei " + campos.length + ".");
        }
    }

    private static final class Linha {
        private final int numero;
        private final String[] campos;

        private Linha(int numero, String[] campos) {
            this.numero = numero;
            this.campos = campos;
        }
    }
}
