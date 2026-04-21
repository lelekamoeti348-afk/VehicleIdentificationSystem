import org.mindrot.jbcrypt.BCrypt;

public class HashGen {
    public static void main(String[] args) {
        String myPassword = "password";  // ← change to your actual password
        String hash = BCrypt.hashpw(myPassword, BCrypt.gensalt());
        System.out.println("Hash: " + hash);
    }
}