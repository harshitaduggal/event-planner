import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class EventPlannerUI extends JFrame {
    private JTextField titleField, dateField, locationField, typeField, searchField;
    private JButton addButton, refreshButton, deleteButton;
    private JTable table;
    private DefaultTableModel tableModel;

    private final String URL = "jdbc:mysql://localhost:3306/event_planner";
    private final String USER = "root";
    private final String PASS = "harshita406";

    public EventPlannerUI() {
        setTitle("Event Planner");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        applyWindowsLookAndFeel();

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Event"));

        titleField = new JTextField();
        dateField = new JTextField("YYYY-MM-DD");
        locationField = new JTextField();
        typeField = new JTextField();

        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Date:"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Location:"));
        formPanel.add(locationField);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeField);

        addButton = new JButton("➕ Add");
        refreshButton = new JButton("🔄 Refresh");
        formPanel.add(addButton);
        formPanel.add(refreshButton);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Date", "Location", "Type"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchField.setBackground(new Color(230, 255, 230));
        searchField.setToolTipText("Search by title or date");
        bottomPanel.add(new JLabel("Search: "));
        bottomPanel.add(searchField);

        deleteButton = new JButton("❌ Delete Selected");
        bottomPanel.add(deleteButton);

        add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addEvent());
        refreshButton.addActionListener(e -> fetchEvents());
        deleteButton.addActionListener(e -> deleteEvent());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        showReminderPopup();
        fetchEvents();
        setVisible(true);
    }

    private void applyWindowsLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void addEvent() {
        String title = titleField.getText().trim();
        String date = dateField.getText().trim();
        String location = locationField.getText().trim();
        String type = typeField.getText().trim();

        if (title.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Date are required!");
            return;
        }

        String sql = "INSERT INTO events (title, event_date, location, type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setString(3, location);
            ps.setString(4, type);
            ps.executeUpdate();
            clearForm();
            fetchEvents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteEvent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this event?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM events WHERE event_id = ?";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                fetchEvents();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void fetchEvents() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM events ORDER BY event_date";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("event_id"),
                    rs.getString("title"),
                    rs.getDate("event_date"),
                    rs.getString("location"),
                    rs.getString("type")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void search() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        String text = searchField.getText();
        if (text.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void showReminderPopup() {
        String today = LocalDate.now().toString();
        String sql = "SELECT * FROM events WHERE event_date = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(today));
            ResultSet rs = ps.executeQuery();
            StringBuilder reminders = new StringBuilder();
            while (rs.next()) {
                reminders.append("\n- ").append(rs.getString("title")).append(" at ").append(rs.getString("location"));
            }
            if (reminders.length() > 0) {
                JOptionPane.showMessageDialog(this, "📅 You have events today:" + reminders);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking reminders: " + ex.getMessage());
        }
    }

    private void clearForm() {
        titleField.setText("");
        dateField.setText("YYYY-MM-DD");
        locationField.setText("");
        typeField.setText("");
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
        SwingUtilities.invokeLater(EventPlannerUI::new);
    }
}