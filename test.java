public class test {
	public static void main (String[] args) {
		int[] array = new int[5];
		array[0] = 2;
		array[1] = 6;
		array[2] = 2;
		array[3] = 11;
		array[4] = 11;   
		System.out.println("max = " + max(array));
	}
	public static int max (int[] x) throws Shit{
		int counter = 0; 
		int max = x[0]; 

		for (int temp : x) 
			if(temp > max) 
				max = temp;	
		for (int temp2 : x) 
			if(temp2 == max) 
				counter++;
		if (counter>1) 
			throw new Shit(max); 
		return max; 
	}
	class Shit extends Exception { 
		public Shit (int max) { 
			super("There are two elements with same value (max).");
		}
	}

}