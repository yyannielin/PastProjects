public class LinkStrand implements IDnaStrand{

    private class Node {
        String info;
        Node next;
        public Node(String s){
            info = s;
        }
        public Node(String s, Node node){
            info = s;
            next = node;
        }
    }

    private Node myFirst, myLast, myCurrent;
    private long mySize;
    private int myAppends, myIndex, myLocalIndex;

    public LinkStrand() { this(""); }
    public LinkStrand(String s) { initialize(s); }

    /**
     * Initialize by copying DNA data from the string into this strand,
     * replacing any data that was stored. The parameter should contain only
     * valid DNA characters, no error checking is done by the this method.
     *
     * @param source is the string used to initialize this strand
     */

    @Override
    public void initialize(String source) {

        myFirst = new Node(source);
        myLast = myFirst;
        mySize = source.length();
        myAppends = 0;

        myIndex = 0;
        myLocalIndex = 0;
        myCurrent = myFirst;
    }

    /**
     * Return this object, useful to obtain
     * an object without knowing its type, e.g.,
     * calling dna.getInstance() returns an IDnaStrand
     * that will be the same concrete type as dna
     *
     * @param source is data from which object constructed
     * @return an IDnaStrand whose .toString() method will be source
     */

    @Override
    public IDnaStrand getInstance(String source) {
        return new LinkStrand(source);
    }

    /**
     * Returns the number of elements/base-pairs/nucleotides in this strand.
     *
     * @return the number of base-pairs in this strand
     */

    @Override
    public long size() {
        return mySize;
    }

    /**
     * Append dna to the end of this strind.
     *
     * @param dna is the string appended to this strand
     * @return this strand after the data has been added
     */

    @Override
    public IDnaStrand append(String dna) {
        myLast.next = new Node(dna);
        myLast = myLast.next;
        mySize += dna.length();
        myAppends += 1;
        return this;
    }

    @Override
    public String toString() {

        Node temp = myFirst;
        StringBuilder sb = new StringBuilder();

        while (temp != null) {
            sb.append(temp.info);
            temp = temp.next;
        }

        return sb.toString();
    }

    /**
     * Returns an IDnaStrand that is the reverse of this strand, e.g., for
     * "CGAT" returns "TAGC"
     *
     * @return reverse strand
     */

    @Override
    public IDnaStrand reverse() {

        LinkStrand rev_strand = new LinkStrand();

        Node temp = this.myFirst;
        Node rev_temp = null;

        while (temp != null) {

            rev_strand.myFirst = new Node(
                    new StringBuilder(temp.info).reverse().toString(), rev_temp);

            if (rev_temp == null) {
                rev_strand.myLast = rev_strand.myFirst;
            }

            rev_strand.mySize += rev_strand.myFirst.info.length();

            temp = temp.next;
            rev_temp = rev_strand.myFirst;
        }

        return rev_strand;
    }

//        Node temp = myFirst;
//
//        LinkStrand rev_strand = new LinkStrand(
//                new StringBuilder(temp.info).reverse().toString());

//        temp = temp.next;

//        while (temp!=null){

//            Node rev_temp = rev_strand.myFirst;

//            rev_strand.myFirst = new Node(
//                    new StringBuilder(temp.info).reverse().toString(), rev_temp);

//            rev_strand.mySize += temp.info.length();
//            rev_strand.myAppends++;

//            temp = temp.next;
//        }

//        return rev_strand;
//    }

    /**
     * Returns the number of times append has been called.
     *
     * @return number of appends made to LinkStrand
     */

    @Override
    public int getAppendCount() { return myAppends; }

    /**
     * Returns character at a specified index, where 0 <= index < size()
     *
     * @param index specifies which character will be returned
     * @return the character at index
     * @throws IndexOutOfBoundsException if index < 0 or index >= size()
     */

    @Override
    public char charAt(int index) {

        if (index < 0 || index >= size())
            throw new IndexOutOfBoundsException();

        if (index < myIndex) {
            myIndex = 0;
            myLocalIndex = 0;
            myCurrent = myFirst;
        }

        while (myIndex != index) {
            myIndex++;
            myLocalIndex++;
            if (myLocalIndex >= myCurrent.info.length()) {
                myLocalIndex = 0;
                myCurrent = myCurrent.next;
            }
        }

        return myCurrent.info.charAt(myLocalIndex);
    }
}
