import java.util.*; 
class solution {
	public static void main(String[] args) {
		int ans = fibb(4); 
		System.out.println("Answer is "+ans); 
	}
	static int fibb (int n) {
		int[] array = new int[n+1]; 
		array[0]=0; 
		array[1]=1;	
		for(int i = 2 ; i<=n; i++) 
			array[i]=array[i-1]+array[i-2]; 
		return array[n]; 
	}

}