import java.sql.*;
import java.util.Date;

public class Querier {
    private String url;
    private String user;
    private String password;
    private Connection conn;
    public Querier(String url, String user, String password){
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public void setUpConnection() throws ClassNotFoundException, SQLException{
        Class.forName("org.postgresql.Driver");
        // Connect to the database
        conn = DriverManager.getConnection(url, user, password);
    }
    public void getAllStudents() throws SQLException{
        System.out.println("getAllStudents():");
        Statement stmt = conn.createStatement(); // Execute SQL query
        String SQL = "SELECT * FROM students";
        ResultSet rs = stmt.executeQuery(SQL); // Process the result set
        while(rs.next()){
            int student_id = rs.getInt("student_id");
            String first_name = rs.getString("first_name");
            String last_name = rs.getString("last_name");
            String email = rs.getString("email");
            Date enrollment_date = rs.getDate("enrollment_date");
            System.out.printf("student_id: %d, first_name: %s, last_name: %s, email: %s, enrollment_date: %tF\n", student_id, first_name, last_name, email, enrollment_date);
        }
        System.out.println();
        // Close resources
        rs.close();
        stmt.close();
    }

    public int addStudent(String firstName, String lastName, String email, Date enrollmentDate) throws SQLException {
        System.out.println("addStudent():");
        String insertSQL = "INSERT INTO students (first_name, last_name, email, enrollment_date) VALUES (?, ?, ?, ?) RETURNING student_id";
        PreparedStatement pstmt = conn.prepareStatement(insertSQL);
        pstmt.setString(1, firstName);
        pstmt.setString(2, lastName);
        pstmt.setString(3, email);
        pstmt.setDate(4, new java.sql.Date(enrollmentDate.getTime()));

        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int studentId = rs.getInt(1); // Assuming student_id is the first column returned
        System.out.printf("Added student (%s, %s, %s, %tF) to students with student_id: %d\n\n", firstName, lastName, email, enrollmentDate, studentId);
        return studentId;
    }
    public void updateStudentEmail(int studentId, String newEmail) throws SQLException{
        System.out.println("updateStudentEmail():");
        String SQL = "UPDATE students SET email = ? WHERE student_id = ?";

        PreparedStatement pstmt = conn.prepareStatement(SQL);
        pstmt.setString(1, newEmail);
        pstmt.setInt(2, studentId);
        pstmt.executeUpdate();
        System.out.printf("Updated email for student %d to %s\n\n", studentId, newEmail);
    }
    public void deleteStudent(int studentId) throws SQLException{
        System.out.println("deleteStudent():");
        String SQL = "DELETE FROM students WHERE student_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(SQL);
        pstmt.setInt(1, studentId);
        pstmt.executeUpdate();
        System.out.printf("Deleted student %d\n\n", studentId);
    }

    public Connection getConn() {
        return conn;
    }

    public void end() throws SQLException{
        conn.close();
    }

    public static void main(String[] args) {
        // JDBC & Database credentials
        String url = "jdbc:postgresql://localhost:5432/Assignment3Q1";
        String user = "postgres";
        String password = "password";

        Querier q = new Querier(url, user, password);

        try { // Load PostgreSQL JDBC Driver
            q.setUpConnection();
            if (q.getConn() != null) {
                System.out.println("Connected to PostgreSQL successfully!\n");
                q.getAllStudents();
                int addedIndex = q.addStudent("Steve", "Lawson", "tms@seminary.com", new Date());
                q.getAllStudents();
                q.updateStudentEmail(addedIndex, "sjl@onepassionministries.com");
                q.getAllStudents();
                q.deleteStudent(addedIndex);
                q.getAllStudents();
            } else {
                System.out.println("Failed to establish connection.");
            } // Close the connection (in a real scenario, do this in a finally
            q.end();
        }
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
