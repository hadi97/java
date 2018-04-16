import java.util.*; 
import java.lang.Comparable; 
public class prog6 {
	public static void main (String[] args) {
		Map <NrTelefoniczny,Wpis> map = new TreeMap <NrTelefoniczny,Wpis>(); 
		Wpis osoba1 = new Osoba("Michal","zenski","Lodz",new NrTelefoniczny(222,111));
		Wpis Firma1 = new Firma("Samsung","Lodz",new NrTelefoniczny(222,111)); 
		map.put(osoba1.numer,osoba1); 
		//Iterator itr = map.itrator(); 

	}

} 
class NrTelefoniczny implements Comparaple<NrTelefoniczny> { 
	int nrKierunkowy; 
	int nrTelefonu;
	public NrTelefoniczny (int n1 , int n2) {
		this.nrKierunkowy=n1;
		this.nrTelefonu=n2; 
	}
	//@Override
	public int compareto(NrTelefoniczny id) {
		if (this.nrKierunkowy != id.nrKierunkowy) 
			return this.nrKierunkowy-id.nrKierunkowy;
		if (this.nrTelefonu != id.nrTelefonu) 
			return this.nrTelefonu-id.nrTelefonu;
	}
}  
abstract class Wpis {
	public final NrTelefoniczny ID; 
	public Wpis(NrTelefoniczny id) {
		this.ID=id; 
	}
	abstract void opis();

} 
class Osoba extends Wpis {
	private String imie; 
	private String nazwisko; 
	private String adres; 
	private NrTelefoniczny numer; 
	public Osoba (String im , String nazw , String adres , NrTelefoniczny nrTelef) {
		this.imie = im; 
		this.nazwisko = nazw; 
		this.adres = adres; 
		this.numer.nrKierunkowy = nrTelef.nrKierunkowy;
		this.numer.nrTelefonu=nrTelef.nrTelefonu;
	} 
	void opis() {
		System.out.println("Imie : "+imie); 
		System.out.println("Nazwisko : "+nazwisko); 
		System.out.println("Adres : "+adres); 
		System.out.println("NrKierunkowy : "+numer.nrKierunkowy);
		System.out.println("NrTelefonu : "+numer.nrTelefonu); 
	}
} 
class Firma extends Wpis {
	private String imie; 
	private String adres; 
	private NrTelefoniczny numer; 
	
	public Firma (String im , String adres , NrTelefoniczny num) {
		this.imie = im; 
		this.adres = adres; 
		this.numer.nrKierunkowy= num.nrKierunkowy; 
		this.numer.nrTelefonu=num.nrTelefonu;
	}
	void opis () {
		System.out.println("Imie : "+imie);  
		System.out.println("Adres : "+adres); 
		System.out.println("NrKierunkowy : "+numer.nrKierunkowy);
		System.out.println("NrTelefonu : "+numer.nrTelefonu);
	}
}