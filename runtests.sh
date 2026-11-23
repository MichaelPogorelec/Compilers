PROJ=~brylow/cosc4400/Projects
TESTDIR="$PROJ/tests"
OUTDIR="./difftest"
 
ALL_TESTS=(BinarySearch BinaryTree BubbleSort Factorial LinearSearch
           LinkedList QuickSort ThreadGood TreeVisitor)
 
if [ $# -gt 0 ]; then
    TESTS=("${@%.java}")
else
    TESTS=("${ALL_TESTS[@]}")
fi
 
make || { echo "make failed, aborting." >&2; exit 1; }
 
if [ -t 1 ]; then
    GREEN=$'\e[32m'; RED=$'\e[31m'; RESET=$'\e[0m'
else
    GREEN=""; RED=""; RESET=""
fi
 
pass=0
fail=0
failed=()
 
mkdir -p "$OUTDIR"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
 
for t in "${TESTS[@]}"; do
    src="$TESTDIR/$t.java"
    [ -f "$src" ] || { echo "skipping $t (no $src)" >&2; continue; }
 
    java Assem.Main < "$src" > "$tmp/mine" 2>/dev/null
 
    "$PROJ/mjparser" "$src" \
        | "$PROJ/mjchecker" - \
        | "$PROJ/mjtranslator" - -riscv \
        | "$PROJ/mjassem" - > "$tmp/ref" 2>/dev/null
 
    if diff -u --label "reference/$t" --label "mine/$t" \
            "$tmp/ref" "$tmp/mine" > "$OUTDIR/$t.diff"; then
        echo "${GREEN}PASS${RESET} $t"
        pass=$((pass + 1))
    else
        echo "${RED}FAIL${RESET} $t"
        failed+=("$t")
        fail=$((fail + 1))
    fi
done
 
echo
echo "Passed: $pass   Failed: $fail"
[ $fail -gt 0 ] && echo "Failed tests: ${failed[*]}"
echo "Diffs written to $OUTDIR/"
[ $fail -eq 0 ]
 
