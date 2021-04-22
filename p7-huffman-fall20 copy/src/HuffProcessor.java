import java.util.PriorityQueue;

/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private final int myDebugLevel;
	
	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;
	
	public HuffProcessor() {
		this(0);
	}
	
	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){

		int[] counts = readForCounts(in);
		HuffNode root = makeTreeFromCounts(counts);
		String[] codings = makeCodingsFromTree(root);

		out.writeBits(BITS_PER_INT, HUFF_TREE);
		writeHeader(root, out);

		in.reset();
		writeCompressedBits(codings, in, out);
		out.close();
	}

	/**
	 * Creates an array of integers with the index value representing the
	 * 8-bit chunk of data and the value stored in the array representing
	 * the frequency of that chunk of data in the file.
	 *
	 * @param in
	 * 			Buffered bit stream of the file to be compressed.
	 * @return integer array storing the frequency of characters in the file.
	 */
	private int[] readForCounts(BitInputStream in){
		int[] freq = new int[ALPH_SIZE+1];
		while (true) {
			int bits = in.readBits(BITS_PER_WORD);
			if (bits == -1) break;
			freq[bits]+=1;
		}
		freq[PSEUDO_EOF] = 1;
		return freq;
	}

	/**
	 * Creates the Huffman tree that will be used to create encodings by
	 * first adding the characters and frequencies in a priority queue,
	 * then combining minimal nodes in the priority queue until one tree
	 * is created with characters in the leaf nodes.
	 *
	 * @param freq	integer array storing frequency of characters in the file.
	 * @return	HuffNode tree that is used to create encodings.
	 */
	private HuffNode makeTreeFromCounts(int[] freq){
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();
		for(int i=0; i<freq.length; i++){
			if(freq[i]>0){
				pq.add(new HuffNode(i, freq[i], null, null));
			}
		}
		while(pq.size()>1){
			HuffNode left = pq.remove();
			HuffNode right = pq.remove();
			HuffNode t = new HuffNode(left.myValue+right.myValue, left.myWeight+right.myWeight, left, right);
			pq.add(t);
		}
		HuffNode root = pq.remove();
		return root;
	}

	/**
	 * Creates and returns an array of Strings whose index represents the 8-bit
	 * chunk of data and value represents the Huffman encoding of the chunk.
	 *
	 * @param root Huffman tree used to create the encodings.
	 * @return	String array of Huffman encodings for the data.
	 */
	private String[] makeCodingsFromTree(HuffNode root){
		String[] encodings = new String[ALPH_SIZE+1];
		codingHelper(root, "", encodings);
		return encodings;
	}

	/**
	 * Helper method for makeCodingsFromTree().
	 * Traverses through the HuffNode to generate paths to each leaf node
	 * of the tree and adds the path to the leaf nodes to the String array
	 * of encodings.
	 *
	 * @param root	Huffman tree that is being traversed through.
	 * @param path	The path from the root to the current node (0-left, 1-right).
	 * @param encodings	String array of Huffman encodings for the data.
	 */
	private void codingHelper(HuffNode root, String path, String[] encodings){
		if(root==null) return;
		if(root.myLeft==null && root.myRight==null){
			encodings[root.myValue] = path;
			return;
		}
		else{
			codingHelper(root.myLeft, path+"0", encodings);
			codingHelper(root.myRight, path+"1", encodings);
		}
	}

	/**
	 * Writes the header of the compressed file, which represents a pre-order
	 * traversal of the Huffman tree that will be needed to decompress the file.
	 *
	 * @param root	Huffman tree that is being traversed through.
	 * @param out	Buffered bit stream writing to the output file.
	 */
	private void writeHeader(HuffNode root, BitOutputStream out){
		if(root==null) return;
		if(root.myLeft==null && root.myRight==null){
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD+1, root.myValue);
		}else{
			out.writeBits(1, 0);
			writeHeader(root.myLeft, out);
			writeHeader(root.myRight, out);
		}
	}

	/**
	 * Write a compressed version of the given file using the encodings created
	 * from the Huffman tree.
	 *
	 * @param encodings	String array of Huffman encodings for the data.
	 * @param in	Buffered bit stream writing to the output file.
	 * @param out	Buffered bit stream writing to the output file.
	 */
	private void writeCompressedBits(String[] encodings, BitInputStream in, BitOutputStream out){
		while (true) {
			int bits = in.readBits(BITS_PER_WORD);
			if (bits == -1) break;
			String code = encodings[bits];
			out.writeBits(code.length(), Integer.parseInt(code, 2));
		}
		String code = encodings[PSEUDO_EOF];
		out.writeBits(code.length(), Integer.parseInt(code,2));
	}

	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){
		int magic = in.readBits(BITS_PER_INT);
		if (magic != HUFF_TREE) {
			throw new HuffException("invalid magic number "+magic);
		}
		HuffNode root = readTree(in);
		HuffNode current = root;
		while (true){
			int bits = in.readBits(1);
			if (bits == -1) {
				throw new HuffException("bad input, no PSEUDO_EOF");
			}else{
				if(bits == 0) current = current.myLeft;
				else current = current.myRight;
				if(current.myLeft==null && current.myRight==null){
					if(current.myValue==PSEUDO_EOF){
						break;
					} else{
						out.writeBits(BITS_PER_WORD, current.myValue);
						current = root;
					}
				}
			}
		}
		out.close();
	}

	/**
	 * Creates a Huffman tree from the pre-order traversal stored in
	 * the header of the file.
	 *
	 * @param in	Buffered bit stream of the file to be decompressed.
	 * @return	The Huffman tree that is created from the header.
	 */
	private HuffNode readTree(BitInputStream in){
		int bit = in.readBits(1);
		if(bit==-1){
			throw new HuffException("Invalid Bit");
		}
		if(bit==0){
			HuffNode left = readTree(in);
			HuffNode right = readTree(in);
			return new HuffNode(0, 0, left, right);
		}else{
			int value = in.readBits(BITS_PER_WORD+1);
			return new HuffNode(value, 0, null, null);
		}
	}
}