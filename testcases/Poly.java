class Poly {
    public static void main(String[] args) {
        Animal a;
        int[] nums;
        int i;
        boolean ok = true;
        a = new Dog();
        nums = new int[5];
        i = 0;
        while (i < nums.length && !(i > 3)) {
            nums[i] = a.Legs() + i;
            i = i + 1;
        }
        if (ok || a == null)
            Xinu.print("done");
        else
            Xinu.println();
        Xinu.printint(nums[2]);
    }
}

class Animal {
    int legs;
    public int Legs() { legs = 4; return legs; }
    public int Noise() { return 0; }
}

class Dog extends Animal {
    int age;
    public int Legs() { age = 3; return legs + age; }
    public int Bark() { return age; }
}
