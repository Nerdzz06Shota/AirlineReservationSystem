import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;

/**
 * Airline Reservation System with bookings saved to a text file for persistence.
 */
public class AirlineReservationSystem extends JFrame {

    private java.util.List<Flight> flights = new ArrayList<>();
    private java.util.List<Booking> bookings = new ArrayList<>();
    private JComboBox<String> cbFrom, cbTo, cbFlightSelect;
    private JSpinner spPassengers;
    private DefaultTableModel bookingTableModel;
    private JTable bookingTable;
    private JTextField tfName, tfPassport;
    private JComboBox<String> cbSeat;

    private static final String BOOKINGS_FILE = "bookings.txt";

    public AirlineReservationSystem() {
        setTitle("Airline Reservation System");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initializeFlights();
        loadBookingsFromFile();

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel panelSearch = createFlightSearchPanel();
        JPanel panelView = createViewBookingsPanel();
        JPanel panelCancel = createCancelBookingPanel();

        tabbedPane.addTab("Search & Book Flight", panelSearch);
        tabbedPane.addTab("View Bookings", panelView);
        tabbedPane.addTab("Cancel Booking", panelCancel);

        add(tabbedPane);
    }

    private void initializeFlights() {
        flights.add(new Flight("AI101", "New York", "London", "09:00 AM", 180));
        flights.add(new Flight("BA202", "London", "Paris", "12:00 PM", 100));
        flights.add(new Flight("DL303", "Paris", "Rome", "03:00 PM", 120));
        flights.add(new Flight("AF404", "Rome", "Berlin", "06:00 PM", 150));
        flights.add(new Flight("LH505", "Berlin", "New York", "09:00 PM", 200));
        flights.add(new Flight("UA606", "New York", "Paris", "08:00 AM", 190));
        flights.add(new Flight("AA707", "London", "Rome", "01:00 PM", 160));
    }

    private JPanel createFlightSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top selection panel
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // From label and combo
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("From:"), gbc);

        Set<String> fromLocations = new TreeSet<>();
        for (Flight f : flights) fromLocations.add(f.getFrom());

        cbFrom = new JComboBox<>(fromLocations.toArray(new String[0]));
        gbc.gridx = 1;
        cbFrom.setPreferredSize(new Dimension(150, 25));
        topPanel.add(cbFrom, gbc);

        // To label and combo
        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(new JLabel("To:"), gbc);

        Set<String> toLocations = new TreeSet<>();
        for (Flight f : flights) toLocations.add(f.getTo());

        cbTo = new JComboBox<>(toLocations.toArray(new String[0]));
        gbc.gridx = 1;
        cbTo.setPreferredSize(new Dimension(150, 25));
        topPanel.add(cbTo, gbc);

        // Passengers
        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(new JLabel("Passengers:"), gbc);
        gbc.gridx = 1;
        spPassengers = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spPassengers.setPreferredSize(new Dimension(150, 25));
        topPanel.add(spPassengers, gbc);

        // Search flights button
        gbc.gridx = 1;
        gbc.gridy = 3;
        JButton btnSearch = new JButton("Search Flights");
        btnSearch.addActionListener(e -> updateFlightList());
        topPanel.add(btnSearch, gbc);

        panel.add(topPanel, BorderLayout.NORTH);

        // Flight selection panel
        JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        middlePanel.setBorder(BorderFactory.createTitledBorder("Available Flights"));

        cbFlightSelect = new JComboBox<>();
        cbFlightSelect.setPreferredSize(new Dimension(350, 25));
        middlePanel.add(cbFlightSelect);

        panel.add(middlePanel, BorderLayout.CENTER);

        // Booking panel
        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setBorder(BorderFactory.createTitledBorder("Booking Details"));
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        bookingPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        tfName = new JTextField();
        tfName.setPreferredSize(new Dimension(200, 25));
        bookingPanel.add(tfName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bookingPanel.add(new JLabel("Passport No:"), gbc);
        gbc.gridx = 1;
        tfPassport = new JTextField();
        tfPassport.setPreferredSize(new Dimension(200, 25));
        bookingPanel.add(tfPassport, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bookingPanel.add(new JLabel("Seat (A-F):"), gbc);
        gbc.gridx = 1;
        cbSeat = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
        bookingPanel.add(cbSeat, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        JButton btnBook = new JButton("Book Flight");
        btnBook.addActionListener(e -> bookFlight());
        bookingPanel.add(btnBook, gbc);

        panel.add(bookingPanel, BorderLayout.SOUTH);

        updateFlightList(); // initial population

        return panel;
    }

    private void updateFlightList() {
        String from = (String) cbFrom.getSelectedItem();
        String to = (String) cbTo.getSelectedItem();

        cbFlightSelect.removeAllItems();
        for (Flight f : flights) {
            if (f.getFrom().equals(from) && f.getTo().equals(to)) {
                cbFlightSelect.addItem(f.getFlightNo() + " - " + f.getFrom() + " to " + f.getTo() + " (" + f.getTime() + ")");
            }
        }
        if (cbFlightSelect.getItemCount() == 0) {
            cbFlightSelect.addItem("No flights found");
        }
    }

    private JPanel createViewBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        bookingTableModel = new DefaultTableModel();
        bookingTableModel.setColumnIdentifiers(new String[]{"Booking ID", "Flight No", "Name", "Passport", "Seat"});

        bookingTable = new JTable(bookingTableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(bookingTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh Bookings");
        btnRefresh.addActionListener(e -> refreshBookingTable());

        panel.add(btnRefresh, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshBookingTable() {
        bookingTableModel.setRowCount(0);
        for (Booking b : bookings) {
            bookingTableModel.addRow(new Object[]{b.getBookingId(), b.getFlight().getFlightNo(),
                    b.getPassengerName(), b.getPassportNo(), b.getSeat()});
        }
    }

    private JPanel createCancelBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField tfBookingId = new JTextField();
        tfBookingId.setPreferredSize(new Dimension(200, 25));

        JButton btnCancel = new JButton("Cancel Booking");
        btnCancel.addActionListener(e -> cancelBooking(tfBookingId.getText()));

        JPanel cancelPanel = new JPanel();
        cancelPanel.add(new JLabel("Booking ID:"));
        cancelPanel.add(tfBookingId);
        cancelPanel.add(btnCancel);

        panel.add(cancelPanel, BorderLayout.NORTH);

        return panel;
    }

    private void bookFlight() {
        String flightInfo = (String) cbFlightSelect.getSelectedItem();
        if (flightInfo == null || flightInfo.equals("No flights found")) {
            JOptionPane.showMessageDialog(this, "Please select a valid flight.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] flightDetails = flightInfo.split(" - ")[0].split(" ");
        Flight selectedFlight = null;
        for (Flight f : flights) {
            if (f.getFlightNo().equals(flightDetails[0])) {
                selectedFlight = f;
                break;
            }
        }

        String name = tfName.getText();
        String passport = tfPassport.getText();
        String seat = (String) cbSeat.getSelectedItem();

        if (name.isEmpty() || passport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Booking booking = new Booking(selectedFlight, name, passport, seat);
        bookings.add(booking);
        saveBookingsToFile();
        JOptionPane.showMessageDialog(this, "Flight booked successfully! Booking ID: " + booking.getBookingId());
        refreshBookingTable();
    }

    private void cancelBooking(String bookingId) {
        if (bookingId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a booking ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean found = false;
        for (Iterator<Booking> iterator = bookings.iterator(); iterator.hasNext(); ) {
            Booking b = iterator.next();
            if (b.getBookingId().equals(bookingId)) {
                iterator.remove();
                found = true;
                saveBookingsToFile();
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
                refreshBookingTable();
                break;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "Booking ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBookingsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Booking b : bookings) {
                // Format: bookingId|flightNo|passengerName|passportNo|seat
                String line = String.join("|",
                        b.getBookingId(),
                        b.getFlight().getFlightNo(),
                        b.getPassengerName().replace("|", ""), // sanitize
                        b.getPassportNo().replace("|", ""),
                        b.getSeat()
                );
                writer.println(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving bookings: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookingsFromFile() {
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int maxId = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 5) continue;
                String bookingId = parts[0];
                String flightNo = parts[1];
                String passengerName = parts[2];
                String passportNo = parts[3];
                String seat = parts[4];

                Flight flightRef = null;
                for (Flight f : flights) {
                    if (f.getFlightNo().equals(flightNo)) {
                        flightRef = f;
                        break;
                    }
                }
                if (flightRef == null) continue;

                Booking b = new Booking(flightRef, passengerName, passportNo, seat, bookingId);
                bookings.add(b);

                // Extract numeric part of booking ID to update counter
                try {
                    int idNum = Integer.parseInt(bookingId.substring(1));
                    if (idNum > maxId) maxId = idNum;
                } catch (NumberFormatException ignored) {
                }
            }
            Booking.setBookingCounter(maxId + 1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String args[]) 
{
        SwingUtilities.invokeLater(() -> {
            AirlineReservationSystem frame = new AirlineReservationSystem();
            frame.setVisible(true);
        });
    }
}

class Flight {
    private String flightNo;
    private String from;
    private String to;
    private String time;
    private int capacity;

    public Flight(String flightNo, String from, String to, String time, int capacity) {
        this.flightNo = flightNo;
        this.from = from;
        this.to = to;
        this.time = time;
        this.capacity = capacity;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getTime() {
        return time;
    }

    public int getCapacity() {
        return capacity;
    }
}

class Booking {
    private static int bookingCounter = 1;
    private String bookingId;
    private Flight flight;
    private String passengerName;
    private String passportNo;
    private String seat;

    public Booking(Flight flight, String passengerName, String passportNo, String seat) {
        this.bookingId = "B" + (bookingCounter++);
        this.flight = flight;
        this.passengerName = passengerName;
        this.passportNo = passportNo;
        this.seat = seat;
    }

    // Constructor with bookingId for loading from file
    public Booking(Flight flight, String passengerName, String passportNo, String seat, String bookingId) {
        this.bookingId = bookingId;
        this.flight = flight;
        this.passengerName = passengerName;
        this.passportNo = passportNo;
        this.seat = seat;
    }

    public static void setBookingCounter(int counter) {
        bookingCounter = counter;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Flight getFlight() {
        return flight;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPassportNo() {
        return passportNo;
    }

    public String getSeat() {
        return seat;
    }
}

