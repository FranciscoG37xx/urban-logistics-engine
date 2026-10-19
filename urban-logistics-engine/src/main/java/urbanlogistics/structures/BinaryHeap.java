/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Heap binário em array. A raiz é o maior elemento segundo o comparador.
 * Para obter um Min-Heap usa-se {@link #minHeap(Comparator)}.
 */
public class BinaryHeap<T> {

    private static final int CAPACIDADE_INICIAL = 16;

    private Object[] dados;
    private int tamanho;
    private final Comparator<? super T> comparador;

    public BinaryHeap(Comparator<? super T> comparador) {
        this.comparador = Objects.requireNonNull(comparador, "O comparador é obrigatório.");
        this.dados = new Object[CAPACIDADE_INICIAL];
    }

    public static <T> BinaryHeap<T> minHeap(Comparator<? super T> comparador) {
        return new BinaryHeap<T>(comparador.reversed());
    }

    /** O(log n) no pior caso, Ω(1) no melhor; O(1) amortizado na redimensão. */
    public void inserir(T elemento) {
        Objects.requireNonNull(elemento, "Não é possível inserir null.");
        if (tamanho == dados.length) {
            dados = Arrays.copyOf(dados, dados.length * 2);
        }
        dados[tamanho] = elemento;
        tamanho++;
        subir(tamanho - 1);
    }

    /** Remove e devolve a raiz. O(log n). */
    public T remover() {
        if (tamanho == 0) {
            throw new NoSuchElementException("O heap está vazio.");
        }
        T raiz = obter(0);
        tamanho--;
        dados[0] = dados[tamanho];
        dados[tamanho] = null;
        if (tamanho > 0) {
            descer(0);
        }
        return raiz;
    }

    /** Remove um elemento qualquer (usa equals). O(n) pela pesquisa + O(log n). */
    public boolean remover(T elemento) {
        int i = indiceDe(elemento);
        if (i < 0) {
            return false;
        }
        tamanho--;
        if (i == tamanho) {
            dados[i] = null;
            return true;
        }
        dados[i] = dados[tamanho];
        dados[tamanho] = null;
        subir(i);
        descer(i);
        return true;
    }

    /** Devolve a raiz sem a remover. Θ(1). */
    public T espreitar() {
        if (tamanho == 0) {
            throw new NoSuchElementException("O heap está vazio.");
        }
        return obter(0);
    }

    public int tamanho() { return tamanho; }

    public boolean estaVazio() { return tamanho == 0; }

    /** Cópia dos elementos, sem ordem garantida (para listar e gravar). O(n). */
    public List<T> elementos() {
        List<T> copia = new ArrayList<>(tamanho);
        for (int i = 0; i < tamanho; i++) {
            copia.add(obter(i));
        }
        return copia;
    }

    private int indiceDe(T elemento) {
        for (int i = 0; i < tamanho; i++) {
            if (dados[i].equals(elemento)) {
                return i;
            }
        }
        return -1;
    }

    private void subir(int i) {
        while (i > 0) {
            int pai = (i - 1) / 2;
            if (comparar(i, pai) <= 0) {
                break;
            }
            trocar(i, pai);
            i = pai;
        }
    }

    private void descer(int i) {
        while (true) {
            int esq = 2 * i + 1;
            if (esq >= tamanho) {
                break;
            }
            int maior = esq;
            int dir = esq + 1;
            if (dir < tamanho && comparar(dir, esq) > 0) {
                maior = dir;
            }
            if (comparar(maior, i) <= 0) {
                break;
            }
            trocar(i, maior);
            i = maior;
        }
    }

    private int comparar(int i, int j) {
        return comparador.compare(obter(i), obter(j));
    }

    private void trocar(int i, int j) {
        Object tmp = dados[i];
        dados[i] = dados[j];
        dados[j] = tmp;
    }

    @SuppressWarnings("unchecked")
    private T obter(int i) {
        return (T) dados[i];
    }
}
