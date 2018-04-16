
import java.util.*;
import java.io.*;

class WektoryRoznejDlugosciException extends Exception {
	
	public final int dlA, dlB;
	
	public WektoryRoznejDlugosciException(int dlA, int dlB) {
		super("Wektory maj¹ ró¿ne d³ugoœci");
		this.dlA = dlA;
		this.dlB = dlB;
	}
	
}

public class P5 {

	public static void main(String[] args) {
		try {
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader rd = new BufferedReader(isr);
			Vector<Double> C = null;
			boolean w = false;
			do {
				w = false;
				System.out.print("Podaj pierwszy wektor: ");
				Vector<Double> A = readVec(rd.readLine());
				System.out.print("Podaj drugi wektor: ");
				Vector<Double> B = readVec(rd.readLine());
				try {
					C = addVec(A, B);
				} catch(WektoryRoznejDlugosciException e) {
					System.out.println("B³¹d: " + e.getMessage() + " " + e.dlA + " != " + e.dlB);
					System.out.println("Podaj ponownie wektory.");
					w = true;
				}
			} while(w);
			saveVec(C, "suma.txt");
			rd.close();
			isr.close();
		} catch(IOException e) {
			System.out.println("B³¹d IO :( " + e.getLocalizedMessage());
		}
	}
	
	static Vector<Double> readVec(String l) {
		Scanner scan = new Scanner(l);
		Vector<Double> vec = new Vector<Double>();
		while(scan.hasNext()) {
			if(scan.hasNextDouble())
				vec.add(scan.nextDouble());
			else
				scan.next();
		}
		scan.close();
		return vec;
	}
	
	static Vector<Double> addVec(Vector<Double> A, Vector<Double> B) throws WektoryRoznejDlugosciException {
		if(A.size() != B.size())
			throw new WektoryRoznejDlugosciException(A.size(), B.size());
		Vector<Double> C = new Vector<Double>(A.size());
		for(int i = 0; i < A.size(); ++i)
			C.add(A.elementAt(i) + B.elementAt(i));
		return C;
	}
	
	static void saveVec(Vector<Double> v, String f) throws IOException {
		OutputStream out = new FileOutputStream(f);
		Writer wr = new OutputStreamWriter(out);
		PrintWriter pr = new PrintWriter(wr);
		for(int i = 0; i < v.size(); ++i)
			pr.print(v.elementAt(i) + " ");
		pr.close();
		wr.close();
		out.close();
	}

}
