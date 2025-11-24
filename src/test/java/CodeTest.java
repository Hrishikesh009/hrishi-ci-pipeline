import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;

public class CodeTest {

    @Test
    public void testMainOutput() throws Exception {
        // Capture console output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Run main
        Code.main(null);

        // Restore original System.out
        System.setOut(originalOut);

        String output = outContent.toString().trim();

        assertEquals("Hello and welcome from Hrishi's new CI/CD Pipeline!", output);
    }
}
