package model;

public class SplineParameters {
    private int K;   // число опорных точек
    private int N;  //число отрезков для каждого участка B-сплайна
    private int M;  //число образующих
    private int M1;  //число отрезков по окружностям между соседними образующими

    public SplineParameters(int K, int N, int M, int M1) {
        this.K = K;
        this.N = N;
        this.M = M;
        this.M1 = M1;
    }

    public int getK() {
        return K;
    }

    public void setK(int K) {
        this.K = K;
    }

    public int getN() {
        return N;
    }

    public void setN(int N) {
        this.N = N;
    }

    public int getM() {
        return M;
    }

    public void setM(int M) {
        this.M = M;
    }

    public int getM1() {
        return M1;
    }

    public void setM1(int M1) {
        this.M1 = M1;
    }
}
