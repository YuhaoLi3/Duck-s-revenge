import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DodgingGame extends JFrame {
    private static int WINDOW_WIDTH;
    private static int WINDOW_HEIGHT;
    private static final int PLAYER_WIDTH = 80;
    private static final int PLAYER_HEIGHT = 80;

    private BufferedImage[] walkLeftImages;
    private BufferedImage[] walkRightImages;
    private BufferedImage playerLeftImage;
    private BufferedImage playerRightImage;
    private BufferedImage currentPlayerImage;
    private BufferedImage backgroundImage;
    private BufferedImage bird;
    private int playerX, playerY;
    private boolean movingLeft;
    private int walkIndex;
    private int walkFrameDelay = 10;
    private int walkFrameCount = 0;

    public DodgingGame() {
        setTitle("Dodging Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        try {
            walkLeftImages = new BufferedImage[12];
            walkRightImages = new BufferedImage[12];
            for (int i = 1; i <= 12; i++) {
                BufferedImage img = ImageIO.read(new File("src/AmoungusAnimation/walk" + i + ".png"));
                walkRightImages[i-1] = resizeImage(img, PLAYER_WIDTH, PLAYER_HEIGHT);
                walkLeftImages[i-1] = flipImageHorizontally(walkRightImages[i-1]);
            }
            playerLeftImage = resizeImage(ImageIO.read(new File("src/Amongus1 (1).png")), PLAYER_WIDTH, PLAYER_HEIGHT);
            playerRightImage = resizeImage(ImageIO.read(new File("src/AmongusRight-removebg-preview (1).png")), PLAYER_WIDTH, PLAYER_HEIGHT);
            backgroundImage = ImageIO.read(new File("src/duck-hunt-extreme-wide-shot-u6m5195gtxd0akw6 (1).png"));
            bird = ImageIO.read(new File("src/image-removebg-preview.png"));
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

        currentPlayerImage = playerRightImage;
        playerX = WINDOW_WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = WINDOW_HEIGHT - PLAYER_HEIGHT - 10;
        movingLeft = false;
        walkIndex = 0;

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (e.getX() < playerX) {
                        movingLeft = true;
                    } else {
                        movingLeft = false;
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

        // Prevent the player from moving out of the window and update the current image
        if (playerX < 0) {
            playerX = 0;
            currentPlayerImage = playerLeftImage;
        } else if (playerX > WINDOW_WIDTH - PLAYER_WIDTH) {
            playerX = WINDOW_WIDTH - PLAYER_WIDTH;
            currentPlayerImage = playerRightImage;
        } else {
            walkFrameCount++;
            if (walkFrameCount >= walkFrameDelay) {
                walkFrameCount = 0;
                walkIndex = (walkIndex + 1) % walkLeftImages.length;
                if (movingLeft) {
                    currentPlayerImage = walkLeftImages[walkIndex];
                } else {
                    currentPlayerImage = walkRightImages[walkIndex];
                }
            }
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    private BufferedImage flipImageHorizontally(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
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
