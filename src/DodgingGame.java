import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class DodgingGame extends JFrame {
    private static int WINDOW_WIDTH;
    private static int WINDOW_HEIGHT;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;

    private Image playerLeftImage;
    private Image playerRightImage;
    private Image currentPlayerImage;
    private Image backgroundImage;
    private int playerX, playerY;
    private boolean movingLeft;

    public DodgingGame() {
        setTitle("Dodging Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Load images with error handling
        try {
            playerLeftImage = ImageIO.read(new File("src/Amongus1 (1).png")).getScaledInstance(PLAYER_WIDTH, PLAYER_HEIGHT, Image.SCALE_SMOOTH);
            playerRightImage = ImageIO.read(new File("src/AmongusRight-removebg-preview (1).png")).getScaledInstance(PLAYER_WIDTH, PLAYER_HEIGHT, Image.SCALE_SMOOTH);
            backgroundImage = ImageIO.read(new File("src/beach-aerial-view_1308-27375.png"));
            WINDOW_WIDTH = backgroundImage.getWidth(null);
            WINDOW_HEIGHT = backgroundImage.getHeight(null);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading images: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Set size of the JFrame to match the background image dimensions plus the insets (window decorations)
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null); // Center the frame

        currentPlayerImage = playerRightImage; // Start with the right image
        playerX = WINDOW_WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = WINDOW_HEIGHT - PLAYER_HEIGHT - 10;
        movingLeft = false;

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (e.getX() < playerX) {
                        movingLeft = true;
                        currentPlayerImage = playerLeftImage;
                    } else {
                        movingLeft = false;
                        currentPlayerImage = playerRightImage;
                    }
                }
            }
        });

        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                gamePanel.repaint();
            }
        });
        timer.start();

        setVisible(true);

        // Adjust size to include window decorations
        Insets insets = getInsets();
        setSize(WINDOW_WIDTH + insets.left + insets.right, WINDOW_HEIGHT + insets.top + insets.bottom);
    }

    private void updateGame() {
        // Update player position based on direction
        if (movingLeft) {
            playerX -= 5;
        } else {
            playerX += 5;
        }

        // Prevent the player from moving out of the window
        if (playerX < 0) {
            playerX = 0;
        }
        if (playerX > WINDOW_WIDTH - PLAYER_WIDTH) {
            playerX = WINDOW_WIDTH - PLAYER_WIDTH;
        }
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, this);
            g.drawImage(currentPlayerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DodgingGame();
            }
        });
    }
}
