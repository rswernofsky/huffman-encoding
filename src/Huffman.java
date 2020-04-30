import java.util.ArrayList;

import tester.*;

// a huffman encoding class
class Huffman {

  // the list of nodes
  ArrayList<ATree> forest;

  // constructor:
  // takes in array of letters and frequencies and creates a Huffman encryption
  // key for those letters
  // error if not supplied letters and frequencies arrays of equal length
  Huffman(ArrayList<String> letters, ArrayList<Integer> frequencies) {
    if ((letters.size() < 2) || (letters.size() != frequencies.size())) {
      throw new IllegalArgumentException("Lengths of letters and frequencies are wrong.");
    }
    this.forest = new ArrayList<ATree>();
    this.createSortedLeaves(letters, frequencies);
    
    // TERMINATES: because combineLeavesAndSort will reduce the number of trees
    // in the forest by 1 every time it is called
    while (this.forest.size() > 1) {
      this.combineLeavesAndSort();
    }
  }

  // create a list of leaves in this huffman encoding algorithm according to the
  // given letters
  // and corresponding frequencies
  // EFFECT: adds the nodes to the forest in this huffman class
  void createSortedLeaves(ArrayList<String> letters, ArrayList<Integer> frequencies) {
    for (int i = 0; i < letters.size(); i += 1) {
      this.forest.add(new Leaf(letters.get(i), frequencies.get(i)));
    }
    this.insertionSort();
  }

  // ASSUME: forest has at least 2 ATrees within this huffman class
  // EFFECT: combine the first two ATrees within this huffman's forest
  void combineLeavesAndSort() {
    Tree newTree = new Tree(this.forest.remove(0), this.forest.remove(0));
    this.insert(newTree, this.forest, new SmallerFrequency(newTree));
  }

  // EFFECT: inserts the given tree into the given array at the spot where the 
  // given predicate changes from true to false
  void insert(ATree tree, ArrayList<ATree> arr, IPred<ATree> p) {
    int index = 0;

    while (index < arr.size() && p.apply(arr.get(index))) {
      index += 1;
    }

    arr.add(index, tree);
  }

  // EFFECT: sorts this huffman class' forest using insertion
  void insertionSort() {
    ArrayList<ATree> result = new ArrayList<>();

    for (ATree t : this.forest) {
      this.insert(t, result, new SmallerOrEqFrequency(t));
    }

    this.forest = result;
  }

  // Encodes the given string with this Huffman encoder's encryption pattern
  // returns the encoded string as an array list of booleans
  ArrayList<Boolean> encode(String str) {
    ArrayList<Boolean> result = new ArrayList<>();

    for (int i = 0; i < str.length(); i += 1) {
      String ch = str.substring(i, i + 1);

      ArrayList<Boolean> codedChar = this.forest.get(0).findCharInTree(ch, new ArrayList<ATree>(),
          new ArrayList<Boolean>());
      result.addAll(codedChar);
    }

    return result;
  }

  // Decodes the given list of booleans into the encoded String it represents
  // returns the decoded String
  String decode(ArrayList<Boolean> code) {
    if (code.isEmpty()) {
      return "";
    }
    ATree n = this.forest.get(0);
    return n.getChar(code, "", n);
  }

}

interface IFunc<A, R> {
  // apply this function to the given argument
  R apply(A arg);
}

interface IPred<T> extends IFunc<T, Boolean> {
}

//does the given ATree have a smaller or equal frequency than the stored ATree?
class SmallerOrEqFrequency implements IPred<ATree> {

  ATree t;

  SmallerOrEqFrequency(ATree t) {
    this.t = t;
  }

  // does the given ATree have a smaller or equal frequency than the stored ATree?
  public Boolean apply(ATree other) {
    return other.getFrequency() <= this.t.getFrequency();
  }
}

//does the given ATree have a smaller frequency than the stored ATree?
class SmallerFrequency implements IPred<ATree> {

  ATree t;

  SmallerFrequency(ATree t) {
    this.t = t;
  }

  // does the given ATree have a smaller frequency than the stored ATree?
  public Boolean apply(ATree other) {
    return other.frequency < this.t.frequency;
  }
}

// an abstract tree inside a Huffman's forest
abstract class ATree {
  int frequency;

  // get the character at the end of the given path
  abstract String getChar(ArrayList<Boolean> code, String sofar, ATree root);

  // locate a given character (length-1 string) in this ATree
  // and return the path taken to find it
  abstract ArrayList<Boolean> findCharInTree(String ch, ArrayList<ATree> remaining,
      ArrayList<Boolean> code);

  // gets the frequency of this ATree
  int getFrequency() {
    return this.frequency;
  }
}

// A node with a ATree branching to its left and right, but stores no "value" of its own
class Tree extends ATree {
  ATree left;
  ATree right;

  // constructor
  Tree(ATree left, ATree right) {
    this.left = left;
    this.right = right;
    this.frequency = this.left.getFrequency() + this.right.getFrequency();
  }

  // gets the character (length-1 string) at the end of the given path by passing
  // the rest of the
  // path to its left or right ATree
  public String getChar(ArrayList<Boolean> code, String sofar, ATree root) {
    if (code.isEmpty()) {
      return sofar + "?";
    }
    Boolean goRight = code.remove(0);
    if (goRight) {
      return this.right.getChar(code, sofar, root);
    } else {
      return this.left.getChar(code, sofar, root);
    }
  }

  // locates the given character to be encoded in its trees by first searching
  // left then searching
  // right; returns the encoded path to the character
  public ArrayList<Boolean> findCharInTree(String ch, ArrayList<ATree> remaining,
      ArrayList<Boolean> code) {
    remaining.add(this.right);
    code.add(false);
    return this.left.findCharInTree(ch, remaining, code);
  }
}

// A character (length-1 string) stored in an ATree; found at the end of a branch
class Leaf extends ATree {
  String character;

  // constructor
  Leaf(String character, int frequency) {
    this.character = character;
    this.frequency = frequency;
  }

  // adds this leaf's character to the decoded String and returns top of the Tree
  // to search for the next character if there is anything left to be decoded
  public String getChar(ArrayList<Boolean> code, String sofar, ATree root) {
    if (!code.isEmpty()) {
      return root.getChar(code, sofar + this.character, root);
    }
    return sofar + this.character;
  }

  // Returns the path taken to locate this leaf if it contains the matching
  // character
  // or searches the closest un-searched branch for the given character if not;
  // error if all leaves have been searched and the character was not found
  public ArrayList<Boolean> findCharInTree(String ch, ArrayList<ATree> remaining,
      ArrayList<Boolean> code) {
    if (ch.equals(this.character)) {
      return code;
    } 
    if (remaining.isEmpty()) {
      throw new IllegalArgumentException(
          "Tried to encode " + ch + " but that is not part of the language.");
    }
    ATree prevTree = remaining.remove(remaining.size() - 1);

    boolean foundLeft = false;
    int index = code.size() - 1;

    while (!foundLeft) {
      if (!code.get(index)) {
        foundLeft = true;
        code.add(true);
      }
      code.remove(index);
      index -= 1;
    }
    return prevTree.findCharInTree(ch, remaining, code);
    
  }
}

// Examples and tests (woo-woo!)
class ExamplesHuffman {

  Huffman huff1;

  // examples to be initialized in each test
  void initData() {
    ArrayList<String> letters1 = new ArrayList<String>();
    letters1.add("a");
    letters1.add("b");
    letters1.add("c");
    letters1.add("d");
    letters1.add("e");
    letters1.add("f");
    ArrayList<Integer> frequencies1 = new ArrayList<Integer>();
    frequencies1.add(12);
    frequencies1.add(45);
    frequencies1.add(5);
    frequencies1.add(13);
    frequencies1.add(9);
    frequencies1.add(16);

    this.huff1 = new Huffman(letters1, frequencies1);
  }

  // simultaneously testing the Huffman constructor, createSortedLeaves,
  // combineLeavesAndSort
  // methods inside the Huffman class
  void testHuffmanConstructor(Tester t) {
    ArrayList<String> testLetters = new ArrayList<>();
    testLetters.add("a");
    testLetters.add("b");
    testLetters.add("c");
    testLetters.add("d");
    ArrayList<Integer> testFrequencies = new ArrayList<>();
    testFrequencies.add(5);
    testFrequencies.add(9);
    testFrequencies.add(2);
    testFrequencies.add(9);

    Huffman testHuff = new Huffman(testLetters, testFrequencies);
    t.checkExpect(testHuff.forest.get(0).getFrequency(), 25);
    t.checkExpect(((Tree) testHuff.forest.get(0)).left.getFrequency(), 9);
    t.checkExpect(((Tree) testHuff.forest.get(0)).right.getFrequency(), 16);

    testFrequencies.remove(0); // remove the first element to make the length shorter
    t.checkConstructorException(
        new IllegalArgumentException("Lengths of letters and frequencies are " + "wrong."),
        "Huffman", testLetters, testFrequencies);

    t.checkConstructorException(
        new IllegalArgumentException("Lengths of letters and frequencies are " + "wrong."),
        "Huffman", new ArrayList<>(), new ArrayList<>());
  }

  // test SmallerFrequency function object
  void testSmallerFrequency(Tester t) {
    SmallerFrequency sf = new SmallerFrequency(new Leaf("d", 16));

    t.checkExpect(sf.apply(new Leaf("d", 15)), true);
    t.checkExpect(sf.apply(new Leaf("d", 16)), false);
    t.checkExpect(sf.apply(new Leaf("d", 17)), false);
  }

  // test SmallerOrEqFrequency function object
  void testSmallerOrEqFrequency(Tester t) {
    SmallerOrEqFrequency sef = new SmallerOrEqFrequency(new Leaf("d", 16));

    t.checkExpect(sef.apply(new Leaf("d", 15)), true);
    t.checkExpect(sef.apply(new Leaf("d", 16)), true);
    t.checkExpect(sef.apply(new Leaf("d", 17)), false);
  }

  // test insert in Huffman class
  void testInsert(Tester t) {
    this.initData();

    t.checkExpect(this.huff1.forest.get(0).getFrequency(), 100);
    Leaf r = new Leaf("r", 6);
    this.huff1.insert(r, this.huff1.forest, new SmallerFrequency(r));
    t.checkExpect(this.huff1.forest.get(0).getFrequency(), 6);

    Leaf s = new Leaf("s", 9);
    this.huff1.insert(s, this.huff1.forest, new SmallerFrequency(s));
    t.checkExpect(this.huff1.forest.get(0).getFrequency(), 6);
    t.checkExpect(this.huff1.forest.get(1).getFrequency(), 9);

    Leaf l = new Leaf("l", 9);
    // insert as deeply as possible
    this.huff1.insert(l, this.huff1.forest, new SmallerOrEqFrequency(l));
    t.checkExpect(this.huff1.forest.get(2).getFrequency(), 9);
    t.checkExpect(((Leaf) this.huff1.forest.get(2)).character, "l");
  }

  // test insertionSort in Huffman class
  void testInsersionSort(Tester t) {
    this.initData();

    t.checkExpect(this.huff1.forest.get(0).getFrequency(), 100);
    this.huff1.forest.add(new Leaf("r", 6));
    this.huff1.forest.add(new Leaf("s", 9));
    this.huff1.forest.add(new Leaf("l", 6));
    t.checkExpect(this.huff1.forest.get(0).getFrequency(), 100);
    t.checkExpect(this.huff1.forest.get(1).getFrequency(), 6);
    t.checkExpect(this.huff1.forest.get(2).getFrequency(), 9);
    t.checkExpect(this.huff1.forest.get(3).getFrequency(), 6);
    this.huff1.insertionSort();
    t.checkExpect(this.huff1.forest.get(0).getFrequency(), 6);
    t.checkExpect(this.huff1.forest.get(1).getFrequency(), 6);
    t.checkExpect(((Leaf) this.huff1.forest.get(1)).character, "l");
    t.checkExpect(this.huff1.forest.get(2).getFrequency(), 9);
    t.checkExpect(this.huff1.forest.get(3).getFrequency(), 100);
  }

  // test combineLeavesAndSort in Huffman class
  void testCombineLeavesAndSort(Tester t) {
    this.initData();

    // setup (same as test above)
    t.checkExpect(this.huff1.forest.get(0).getFrequency(), 100);
    this.huff1.forest.add(new Leaf("r", 6));
    this.huff1.forest.add(new Leaf("s", 9));
    this.huff1.forest.add(new Leaf("l", 6));
    this.huff1.insertionSort();

    this.huff1.combineLeavesAndSort();

    t.checkExpect(this.huff1.forest.get(0).getFrequency(), 9);
    t.checkExpect(this.huff1.forest.get(1).getFrequency(), 12);
  }

  // test decode in Huffman class
  void testDecode(Tester t) {
    this.initData();
    ArrayList<Boolean> test = new ArrayList<>();
    test.add(true);
    test.add(false);
    test.add(true);
    test.add(true);
    test.add(false);
    test.add(false);
    test.add(true);
    test.add(false);
    test.add(true);
    t.checkExpect(huff1.decode(test), "dad");

    ArrayList<Boolean> test2 = new ArrayList<>();
    test2.add(true);
    test2.add(false);
    test2.add(true);
    test2.add(true);
    test2.add(false);
    test2.add(false);
    test2.add(true);
    test2.add(false);
    t.checkExpect(huff1.decode(test2), "da?");

    ArrayList<Boolean> test3 = new ArrayList<>();
    t.checkExpect(huff1.decode(test3), "");
  }

  // test encode in Huffman class
  void testEncode(Tester t) {
    this.initData();
    ArrayList<Boolean> test = new ArrayList<>();
    test.add(true);
    test.add(false);
    test.add(true);
    test.add(true);
    test.add(false);
    test.add(false);
    test.add(true);
    test.add(false);
    test.add(true);
    t.checkExpect(huff1.encode("dad"), test);
    t.checkExpect(huff1.decode(test), "dad");

    ArrayList<Boolean> a = new ArrayList<>();
    a.add(true);
    a.add(false);
    a.add(false);
    t.checkExpect(huff1.encode("a"), a);

    ArrayList<Boolean> b = new ArrayList<>();
    b.add(false);
    t.checkExpect(huff1.encode("b"), b);

    ArrayList<Boolean> c = new ArrayList<>();
    c.add(true);
    c.add(true);
    c.add(false);
    c.add(false);
    t.checkExpect(huff1.encode("c"), c);

    ArrayList<Boolean> d = new ArrayList<>();
    d.add(true);
    d.add(false);
    d.add(true);
    t.checkExpect(huff1.encode("d"), d);

    ArrayList<Boolean> e = new ArrayList<>();
    e.add(true);
    e.add(true);
    e.add(false);
    e.add(true);
    t.checkExpect(huff1.encode("e"), e);

    ArrayList<Boolean> f = new ArrayList<>();
    f.add(true);
    f.add(true);
    f.add(true);
    t.checkExpect(huff1.encode("f"), f);

    t.checkException(
        new IllegalArgumentException(
            "Tried to encode r but that is not part of the " + "language."),
        this.huff1, "encode", "r");
  }
}
