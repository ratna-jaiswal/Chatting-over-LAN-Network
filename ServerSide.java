import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;

public class ServerSide {
    private static JPanel chatPanel;
    private static JTextField inputField;
    private static PrintWriter writer;
    private static JScrollPane scrollPane;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Server Chat 💬");
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Gradient Background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(131, 58, 180), getWidth(), getHeight(), new Color(252, 175, 69));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // Chat Panel inside a Scroll Pane
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(false);

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        inputField.setBackground(Color.WHITE);
        inputField.setBorder(new EmptyBorder(5, 10, 5, 10));
        inputPanel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("➤");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(new Color(88, 86, 214));
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        inputPanel.add(sendButton, BorderLayout.EAST);
        backgroundPanel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(backgroundPanel);
        frame.setVisible(true);

        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            addMessage("📡 Waiting for client...", false);

            Socket socket = serverSocket.accept();
            addMessage("✅ Client connected!", false);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Thread to receive messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        addMessage("👤 " + message, false);
                    }
                } catch (IOException e) {
                    addMessage("⚠ Connection lost.", false);
                }
            }).start();

            // Send messages when Enter is pressed or Button is clicked
            inputField.addActionListener(e -> sendMessage());
            sendButton.addActionListener(e -> sendMessage());

        } catch (IOException e) {
            addMessage("❌ Error: " + e.getMessage(), false);
        }
    }

    private static void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            writer.println(message);
            addMessage("🛑 You: " + message, true);
            inputField.setText("");
        }
    }

    private static void addMessage(String message, boolean isSent) {
        JLabel messageLabel = new JLabel("<html><p style='padding:10px;'>" + message + "</p></html>");
        messageLabel.setOpaque(true);
        messageLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageLabel.setForeground(Color.WHITE);

        if (isSent) {
            messageLabel.setBackground(new Color(0, 122, 255)); // Blue for sent messages
        } else {
            messageLabel.setBackground(new Color(255, 69, 58)); // Red for received messages
        }

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        if (isSent) {
            messagePanel.add(messageLabel, BorderLayout.EAST);
        } else {
            messagePanel.add(messageLabel, BorderLayout.WEST);
        }

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        // Auto-scroll to latest message
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }
}
