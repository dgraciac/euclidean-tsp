package com.davidgracia.euclideantsp.test;

public class Permutations<T> {

    private final T[] elements;

    public Permutations(T[] elements) {
        this.elements = elements;
    }

    public void calculate() {

        int[] indexes = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            indexes[i] = 0;
        }

        printArray(elements);

        int i = 0;
        while (i < elements.length) {
            if (indexes[i] < i) {
                swap(elements, i % 2 == 0 ? 0 : indexes[i], i);
                printArray(elements);
                indexes[i]++;
                i = 0;
            } else {
                indexes[i] = 0;
                i++;
            }
        }
    }

    private void swap(T[] input, int a, int b) {
        T tmp = input[a];
        input[a] = input[b];
        input[b] = tmp;
    }

    private void printArray(T[] input) {
        for (T t : input) {
            System.out.println(t);
        }
    }
}
