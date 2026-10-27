
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.math.BigDecimal;

public class FacultyManagement extends JFrame {
    private static final String URL =
        "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    private static final String USER = "faculty_app";
    private static final String PASSWORD = "FacultyApp123";

    private JTextField id, name, dept, designation, salary;
    private DefaultTableModel model;
    private JTable table;

    public FacultyManagement() {
        setTitle("Faculty Management System");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Faculty Details"));

        id = new JTextField();
        name = new JTextField();
        dept = new JTextField();
        designation = new JTextField();
        salary = new JTextField();

        form.add(new JLabel("Faculty ID:"));
        form.add(id);
        form.add(new JLabel("Name:"));
        form.add(name);
        form.add(new JLabel("Department:"));
        form.add(dept);
        form.add(new JLabel("Designation:"));
        form.add(designation);
        form.add(new JLabel("Salary:"));
        form.add(salary);

        JButton insert = new JButton("Insert");
        JButton search = new JButton("Search by ID");
        JButton update = new JButton("Update");
        JButton byDept = new JButton("Display by Department");
        JButton all = new JButton("Display All");
        JButton clear = new JButton("Clear");

        JPanel buttons = new JPanel(new GridLayout(2, 3, 8, 8));
        buttons.add(insert);
        buttons.add(search);
        buttons.add(update);
        buttons.add(byDept);
        buttons.add(all);
        buttons.add(clear);

        model = new DefaultTableModel(
            new String[]{"ID", "Name", "Department", "Designation", "Salary"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        insert.addActionListener(e -> insertFaculty());
        search.addActionListener(e -> searchFaculty());
        update.addActionListener(e -> updateFaculty());
        byDept.addActionListener(e -> displayByDepartment());
        all.addActionListener(e -> displayAll());
        clear.addActionListener(e -> clearFields());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.getSelectedRow();
                id.setText(model.getValueAt(r, 0).toString());
                name.setText(model.getValueAt(r, 1).toString());
                dept.setText(model.getValueAt(r, 2).toString());
                designation.setText(model.getValueAt(r, 3).toString());
                salary.setText(model.getValueAt(r, 4).toString());
            }
        });

        displayAll();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void insertFaculty() {
        String sql = "INSERT INTO faculty VALUES (?, ?, ?, ?, ?)";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (id.getText().trim().isEmpty() ||
                name.getText().trim().isEmpty() ||
                dept.getText().trim().isEmpty() ||
                designation.getText().trim().isEmpty() ||
                salary.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill in all fields.");
                return;
            }

            ps.setInt(1, Integer.parseInt(id.getText().trim()));
            ps.setString(2, name.getText().trim());
            ps.setString(3, dept.getText().trim());
            ps.setString(4, designation.getText().trim());
            ps.setBigDecimal(5, new BigDecimal(salary.getText().trim()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Faculty inserted.");
            displayAll();
        } catch (NumberFormatException ex) {
            error("Enter a valid ID and salary.");
        } catch (SQLException ex) {
            error(ex.getMessage());
        }
    }

    private void searchFaculty() {
        String sql = "SELECT * FROM faculty WHERE faculty_id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(id.getText().trim()));
            try (ResultSet rs = ps.executeQuery()) {
                model.setRowCount(0);
                if (rs.next()) {
                    showFaculty(rs);
                } else {
                    JOptionPane.showMessageDialog(this, "Faculty not found.");
                }
            }
        } catch (NumberFormatException ex) {
            error("Enter a valid faculty ID.");
        } catch (SQLException ex) {
            error(ex.getMessage());
        }
    }

    private void updateFaculty() {
        String sql = "UPDATE faculty SET designation = ?, salary = ? WHERE faculty_id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, designation.getText().trim());
            ps.setBigDecimal(2, new BigDecimal(salary.getText().trim()));
            ps.setInt(3, Integer.parseInt(id.getText().trim()));

            int count = ps.executeUpdate();
            if (count > 0) {
                JOptionPane.showMessageDialog(this, "Faculty updated.");
                displayAll();
            } else {
                JOptionPane.showMessageDialog(this, "Faculty not found.");
            }
        } catch (NumberFormatException ex) {
            error("Enter a valid ID and salary.");
        } catch (SQLException ex) {
            error(ex.getMessage());
        }
    }

    private void displayByDepartment() {
        String sql = "SELECT * FROM faculty WHERE department = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dept.getText().trim());
            try (ResultSet rs = ps.executeQuery()) {
                model.setRowCount(0);
                while (rs.next()) {
                    showFaculty(rs);
                }
                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "No faculty in that department.");
                }
            }
        } catch (SQLException ex) {
            error(ex.getMessage());
        }
    }

    private void displayAll() {
        String sql = "SELECT * FROM faculty ORDER BY faculty_id";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            model.setRowCount(0);
            while (rs.next()) {
                showFaculty(rs);
            }
        } catch (SQLException ex) {
            error(ex.getMessage());
        }
    }

    private void showFaculty(ResultSet rs) throws SQLException {
        model.addRow(new Object[]{
            rs.getInt("faculty_id"), rs.getString("name"),
            rs.getString("department"), rs.getString("designation"),
            rs.getBigDecimal("salary")
        });
    }

    private void clearFields() {
        id.setText("");
        name.setText("");
        dept.setText("");
        designation.setText("");
        salary.setText("");
        table.clearSelection();
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
            JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new FacultyManagement().setVisible(true));
    }
}
