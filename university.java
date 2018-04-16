import java.util.Set;
import java.util.HashSet;
public class university {

    public static void main(String[] args) { 
        Course course1 = new Course("NFM",201805);
        Course course2 = new Course("LKJ",201903);
        Course course3 = new Course("WER",201810);
        Course course4 = new Course("WAT",201809);
        
        Set <Student> students = new HashSet <Student> (); 
        students.add(new Student("Michal","Lodz","michal@gmail.com"));
        students.add(new Student("Sebestian","Warsaw","sebestian@gmail.com"));
        students.add(new Student("Jacek","Wroclaw","jacek@gmail.com"));
        students.add(new Student("Dominik","Katowice","dominik@gmail.com"));    
        //printStudent(students); //prints info about every student.
        for(Student x : students){ //registering all the students to course1 
            x.register(course1);
        }     
        printCourse(course1); // prints the Course ID, start data and its students list.
        
        for(Student x : students){ // checking with adding Jacek again to the same course. 
            if(x.getName().equals("Jacek"))
                x.register(course1);
        }  
    }
    public static void printCourse(Course course) {
            System.out.println("Course ID is "+course.getID()+" "+ course.getData());
            if (course.students.isEmpty()) 
                System.out.println("This course has no students in it");
            else 
                for(Student x : course.students) //loops through the student's list of the course.  
                    System.out.println(x.getName());
                
    }
    public static void printStudent(Set <Student> students) {
        students.forEach((x) -> { 
            System.out.println(x.getName()+" "+x.getCity()+" " + x.getEmail());
        });
    }
}
//--- Course class
class Course {
   final private String ID; 
   final int startData; 
   Set <Student> students = new HashSet <Student>(); //list of students registered to this course 
   public Course (String id, int sd) {
       this.ID = id; 
       this.startData=sd;  
   } 
   public String getID() {
       return ID;
   }        
   public int getData() {
       return startData;
   }
} 
//--Student Class
class Student {
    final private String name;      
    final private String city; 
    final private String email; 
    Set <Course> courses  = new HashSet<Course>(); //the courses that student is registered to.  
    
    public Student (String na , String ci , String em) { 
       this.name = na; 
       this.city = ci; 
       this.email = em;    
    }
    //Student can join course only once, but he can join different courses
    public void register(Course cour) {
        if (courses.isEmpty()) { //if student has empty courses list , immediately joining the course. 
            cour.students.add(this);//adding this studens to course's students list. 
            courses.add(cour); //adding this course to student's courses list.
        }
        else { //if he hasn't empty courses list , then checking the courses 
            for (Course c : courses) {
                if(cour.getID().equals(c.getID())) // comparing the course ID to the IDs of the courses list. 
                    System.out.println("Student " +  name +" is already registered to this course");             
                else {
                    cour.students.add(this);
                    courses.add(cour);
                }    
            }     
        }
    }
    public String getName() {
        return name; 
    }
    public String getCity() {
        return city; 
    }
    public String getEmail() {
        return email; 
    }
}