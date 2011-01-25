import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

public class SearchEngine {
    
    private String vector;
    
    public SearchEngine(File vector) {
        try {
            BufferedReader  r = new BufferedReader(new FileReader(vector));
            this.vector = r.readLine();
        } catch (IOException e) {
            System.out.println("File not found");
        }
    }
    
    public void search(String s){
        
        
        
        
    }
}
