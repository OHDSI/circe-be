import org.ohdsi.circe.CirceNativeLibrary;

public class test_java_integration {
    public static void main(String[] args) {
        System.out.println("Testing Circe Native Library...");
        
        // Test basic functionality
        String testExpression = "{}";
        String options = null;
        
        try {
            String result = CirceNativeLibrary.buildCohortSql(testExpression, options);
            System.out.println("buildCohortSql result: " + result);
            
            String checkResult = CirceNativeLibrary.checkCohortExpression(testExpression);
            System.out.println("checkCohortExpression result: " + checkResult);
            
            System.out.println("✅ Java integration test completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error during Java integration test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
