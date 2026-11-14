class Thr {
    public static void main(String[] a) {
        Worker w;
        int d;
        w = new Worker();
        d = w.setN(5);
        d = new Worker().setN(1);
        Xinu.println(w.get());
        Xinu.println(new Worker().n);
    }
}
class Worker extends Thread {
    int n;
    int[] data;
    Worker other;
    public int setN(int x) { n = x; data = new int[3]; data[0] = x; return 0; }
    public int get() { return n + data[0] + data.length; }
    public void run() {
        int t;
        if (other == null && n > 2 || !(n < 0)) Xinu.println(n);
        t = new Worker().get();
        other = this;
        n = other.n;
        Xinu.print("done");
    }
}
