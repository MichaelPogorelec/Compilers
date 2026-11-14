class BinaryTree {
    public static void main(String[] a) {
        Xinu.printint(new BT().Start());
    }
}
class BT {
    public int Start() {
        Tree root; boolean ntb; int nti;
        root = new Tree();
        ntb = root.Init(16);
        ntb = root.Insert(8);
        ntb = root.Insert(24);
        nti = root.GetKey();
        Xinu.printint(nti);
        if (root.Search(24)) Xinu.printint(1); else Xinu.printint(0);
        return 0;
    }
}
class Tree {
    Tree left; Tree right; int key; boolean has_left; boolean has_right; Tree my_null;
    public boolean Init(int v_key) { key = v_key; has_left = false; has_right = false; return true; }
    public boolean SetRight(Tree rn) { right = rn; return true; }
    public boolean SetLeft(Tree ln) { left = ln; return true; }
    public Tree GetRight() { return right; }
    public Tree GetLeft() { return left; }
    public int GetKey() { return key; }
    public boolean Compare(int num1, int num2) {
        boolean ntb; int nti;
        ntb = false; nti = num2 + 1;
        if (num1 < num2) ntb = false; else if (!(num1 < nti)) ntb = false; else ntb = true;
        return ntb;
    }
    public boolean Insert(int v_key) {
        Tree new_node; boolean ntb; Tree current_node; boolean cont; int key_aux;
        new_node = new Tree();
        ntb = new_node.Init(v_key);
        current_node = this;
        cont = true;
        while (cont) {
            key_aux = current_node.GetKey();
            if (v_key < key_aux) {
                if (current_node.GetHas_Left()) current_node = current_node.GetLeft();
                else { cont = false; ntb = current_node.SetHas_Left(true); ntb = current_node.SetLeft(new_node); }
            } else {
                if (current_node.GetHas_Right()) current_node = current_node.GetRight();
                else { cont = false; ntb = current_node.SetHas_Right(true); ntb = current_node.SetRight(new_node); }
            }
        }
        return true;
    }
    public boolean Search(int v_key) {
        Tree current_node; boolean cont; boolean ifound; int key_aux;
        current_node = this; cont = true; ifound = false;
        while (cont) {
            key_aux = current_node.GetKey();
            if (v_key < key_aux) { if (current_node.GetHas_Left()) current_node = current_node.GetLeft(); else cont = false; }
            else { if (this.Compare(v_key, key_aux)) { ifound = true; cont = false; } else if (current_node.GetHas_Right()) current_node = current_node.GetRight(); else cont = false; }
        }
        return ifound;
    }
    public boolean GetHas_Right() { return has_right; }
    public boolean GetHas_Left() { return has_left; }
    public boolean SetHas_Left(boolean val) { has_left = val; return true; }
    public boolean SetHas_Right(boolean val) { has_right = val; return true; }
}
