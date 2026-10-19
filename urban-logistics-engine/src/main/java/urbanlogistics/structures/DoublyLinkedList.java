/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package urbanlogistics.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Lista duplamente ligada com referências à cabeça e à cauda. */
public class DoublyLinkedList<T> implements Iterable<T> {

    public static class No<T> {
        private final T elemento;
        private No<T> anterior;
        private No<T> seguinte;

        private No(T elemento) { this.elemento = elemento; }

        public T getElemento() { return elemento; }
        public No<T> getAnterior() { return anterior; }
        public No<T> getSeguinte() { return seguinte; }
    }

    private No<T> cabeca;
    private No<T> cauda;
    private int tamanho;

    /** Θ(1). */
    public No<T> adicionarNoInicio(T elemento) {
        No<T> no = new No<>(Objects.requireNonNull(elemento));
        if (cabeca == null) {
            cabeca = cauda = no;
        } else {
            no.seguinte = cabeca;
            cabeca.anterior = no;
            cabeca = no;
        }
        tamanho++;
        return no;
    }

    /** Θ(1). */
    public No<T> adicionarNoFim(T elemento) {
        No<T> no = new No<>(Objects.requireNonNull(elemento));
        if (cauda == null) {
            cabeca = cauda = no;
        } else {
            no.anterior = cauda;
            cauda.seguinte = no;
            cauda = no;
        }
        tamanho++;
        return no;
    }

    /** Θ(1). */
    public T removerPrimeiro() {
        if (cabeca == null) {
            throw new NoSuchElementException("A lista está vazia.");
        }
        return remover(cabeca);
    }

    /** Θ(1). */
    public T removerUltimo() {
        if (cauda == null) {
            throw new NoSuchElementException("A lista está vazia.");
        }
        return remover(cauda);
    }

    /** Remove um nó que pertença a esta lista, sem pesquisa. Θ(1). */
    public T remover(No<T> no) {
        Objects.requireNonNull(no);
        if (no.anterior != null) {
            no.anterior.seguinte = no.seguinte;
        } else {
            cabeca = no.seguinte;
        }
        if (no.seguinte != null) {
            no.seguinte.anterior = no.anterior;
        } else {
            cauda = no.anterior;
        }
        no.anterior = null;
        no.seguinte = null;
        tamanho--;
        return no.elemento;
    }

    /** Remove todos os nós depois de 'no' (ou todos, se for null). O(k), k = nós removidos. */
    public void truncarApos(No<T> no) {
        if (no == null) {
            limpar();
            return;
        }
        int removidos = 0;
        for (No<T> n = no.seguinte; n != null; n = n.seguinte) {
            removidos++;
        }
        no.seguinte = null;
        cauda = no;
        tamanho -= removidos;
    }

    public void limpar() {
        cabeca = null;
        cauda = null;
        tamanho = 0;
    }

    public No<T> getCabeca() { return cabeca; }
    public No<T> getCauda() { return cauda; }
    public int tamanho() { return tamanho; }
    public boolean estaVazia() { return tamanho == 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private No<T> atual = cabeca;

            @Override
            public boolean hasNext() { return atual != null; }

            @Override
            public T next() {
                if (atual == null) {
                    throw new NoSuchElementException();
                }
                T e = atual.elemento;
                atual = atual.seguinte;
                return e;
            }
        };
    }
}
