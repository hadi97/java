import java.util.*;
public class state {
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in); 
		int a1 , a2; 
		System.out.print("Write first number : ");
		a1 = input.nextInt(); 
		System.out.println();
		System.out.println("Write second number : ");
		a2 = input.nextInt(); 
		do {
			if(a1>a2)
				a1=a1-a2;
			else
				a2=a2-a1;
		}while(a1!=a2);
		System.out.println("Answer is : "+a1);
	}
}