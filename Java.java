import java.util.*;  
import java.util.stream.Collectors;
class Product{  
    int id;  
    String name;  
	float price;  
    public Product(int id, String name, float price) {  
        this.id = id;  
        this.name = name;  
        this.price = price;  
    }  
}  
public class Java {  
    public static void main(String[] args) {  
        List<Product> productsList = new LinkedList<Product>();  
        //Adding Products  
        productsList.add(new Product(1,"HP Laptop",25000f));  
        productsList.add(new Product(2,"Dell Laptop",30000f));  
        productsList.add(new Product(3,"Lenevo Laptop",28000f));  
        productsList.add(new Product(4,"Sony Laptop",28000f));  
        productsList.add(new Product(5,"Apple Laptop",90000f));
        List <Product> prices = productsList.stream()
        .filter(p-> p.price <= 30000)
        .collect(Collectors.toList());
        Iterator itr = productsList.iterator();
        while(itr.hasNext());
        	System.out.println(itr.next());
        for(Product p : prices)
        	System.out.println(p.name);

    }
} 