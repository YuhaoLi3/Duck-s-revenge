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
import java.util.ArrayList;
import java.util.Iterator;

public class DodgingGame extends JFrame {
    private static int WINDOW_WIDTH;
    private static int WINDOW_HEIGHT;
    private static final int PLAYER_WIDTH = 80;
    private static final int PLAYER_HEIGHT = 80;
    private static final int BIRD_WIDTH = 50;
    private static final int BIRD_HEIGHT = 50;

    private static final int ANIMATION_WIDTH = PLAYER_WIDTH * 2; // 2 times bigger than the player
    private static final int ANIMATION_HEIGHT = PLAYER_HEIGHT * 2; // 2 times bigger than the player
    private static final int ANIMATION_OFFSET = 20; // Offset for the animation position

    private BufferedImage[] walkLeftImages;
    private BufferedImage[] walkRightImages;
    private BufferedImage playerLeftImage;
    private BufferedImage playerRightImage;
    private BufferedImage currentPlayerImage;
    private BufferedImage backgroundImage;

    private BufferedImage[] birdLeftImages;
    private BufferedImage[] birdRightImages;
    private BufferedImage currentBirdImage;

    private BufferedImage[] airPoopImages;
    private BufferedImage[] groundPoopImages;

    private BufferedImage[] predeathImages;
    private BufferedImage[] deathImages;

    private int birdX, birdY;
    private int birdIndex;
    private int birdFrameDelay = 10;
    private int birdFrameCount = 0;

    private int playerX, playerY;
    private boolean movingLeft;
    private int walkIndex;
    private int walkFrameDelay = 10;
    private int walkFrameCount = 0;

    private double angle = 0; // Angle for oscillation

    private ArrayList<Poop> poops = new ArrayList<>();
    private int poopDropDelay = 100;
    private int poopDropCount = 0;

    private boolean isDead = false;
    private boolean isPredeath = false;
    private boolean isDeath = false;
    private boolean playerVisible = true; // New flag to manage player visibility
    private int predeathIndex = 0;
    private int deathIndex = 0;
    private int predeathFrameDelay = 10;
    private int deathFrameDelay = 5;
    private int predeathFrameCount = 0;
    private int deathFrameCount = 0;
    private int predeathX, predeathY;

    public DodgingGame() {
        setTitle("Dodging Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Load images with error handling
        try {
            walkLeftImages = new BufferedImage[12];
            walkRightImages = new BufferedImage[12];
            for (int i = 1; i <= 12; i++) {
                BufferedImage img = ImageIO.read(new File("src/AmoungusAnimation/walk" + i + ".png"));
                walkRightImages[i - 1] = resizeImage(img, PLAYER_WIDTH, PLAYER_HEIGHT);
                walkLeftImages[i - 1] = flipImageHorizontally(walkRightImages[i - 1]);
            }
            playerLeftImage = resizeImage(ImageIO.read(new File("src/Amongus1 (1).png")), PLAYER_WIDTH, PLAYER_HEIGHT);
            playerRightImage = resizeImage(ImageIO.read(new File("src/AmongusRight-removebg-preview (1).png")), PLAYER_WIDTH, PLAYER_HEIGHT);
            backgroundImage = ImageIO.read(new File("src/duck-hunt-extreme-wide-shot-u6m5195gtxd0akw6 (1).png"));
            WINDOW_WIDTH = backgroundImage.getWidth(null);
            WINDOW_HEIGHT = backgroundImage.getHeight(null);

            birdLeftImages = new BufferedImage[3];
            birdRightImages = new BufferedImage[3];
            for (int i = 1; i <= 3; i++) {
                BufferedImage img = ImageIO.read(new File("src/Bird/bird" + i + ".png"));
                birdRightImages[i - 1] = resizeImage(img, BIRD_WIDTH, BIRD_HEIGHT);
                birdLeftImages[i - 1] = flipImageHorizontally(birdRightImages[i - 1]);
            }
            currentBirdImage = birdRightImages[0];

            airPoopImages = new BufferedImage[4];
            for (int i = 1; i <= 4; i++) {
                airPoopImages[i - 1] = resizeImage(ImageIO.read(new File("src/Airpoop/air" + i + ".png")), Poop.POOP_WIDTH, Poop.POOP_HEIGHT);
            }

            groundPoopImages = new BufferedImage[8];
            for (int i = 1; i <= 8; i++) {
                groundPoopImages[i - 1] = resizeImage(ImageIO.read(new File("src/GroundPoop/ground" + i + ".png")), Poop.POOP_WIDTH, Poop.POOP_HEIGHT);
            }

            predeathImages = new BufferedImage[5];
            for (int i = 1; i <= 5; i++) {
                predeathImages[i - 1] = resizeImage(ImageIO.read(new File("src/Predeath/predeath" + i + ".png")), ANIMATION_WIDTH, ANIMATION_HEIGHT);
            }

            deathImages = new BufferedImage[17];
            for (int i = 1; i <= 17; i++) {
                deathImages[i - 1] = resizeImage(ImageIO.read(new File("src/Death/death" + i + ".png")), ANIMATION_WIDTH, ANIMATION_HEIGHT);
            }

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
        birdX = playerX;
        birdY = 100; // Fixed position of the bird at the top of the screen
        movingLeft = false;
        walkIndex = 0;
        birdIndex = 0;

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

        // Adjust size to include window decorations
        Insets insets = getInsets();
        setSize(WINDOW_WIDTH + insets.left + insets.right, WINDOW_HEIGHT + insets.top + insets.bottom);
    }

    private void updateGame() {
        if (isDead) {
            if (isPredeath) {
                predeathFrameCount++;
                if (predeathFrameCount >= predeathFrameDelay) {
                    predeathFrameCount = 0;
                    predeathIndex++;
                }

                if (predeathIndex >= predeathImages.length) {
                    isPredeath = false;
                    isDeath = true;
                    deathIndex = 0;
                }
            } else if (isDeath) {
                deathFrameCount++;
                if (deathFrameCount >= deathFrameDelay) {
                    deathFrameCount = 0;
                    deathIndex++;
                }

                if (deathIndex == 7) {
                    playerVisible = false; // Make the player disappear at frame 7
                }

                if (deathIndex >= deathImages.length) {
                    isDeath = false;
                }
            }
            return; // Skip the update if the player is dead
        }

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

        // Update bird position to oscillate horizontally and vertically at the top of the screen
        angle += 0.05;
        birdX = playerX + (int) (100 * Math.cos(angle));
        birdY = 50 + (int) (10 * Math.sin(angle)); // Adjust Y position for hovering effect

        // Update bird animation frame
        birdFrameCount++;
        if (birdFrameCount >= birdFrameDelay) {
            birdFrameCount = 0;
            birdIndex = (birdIndex + 1) % birdRightImages.length;
            if (Math.cos(angle) > 0) {
                currentBirdImage = birdRightImages[birdIndex];
            } else {
                currentBirdImage = birdLeftImages[birdIndex];
            }
        }

        // Drop poop
        poopDropCount++;
        if (poopDropCount >= poopDropDelay) {
            poopDropCount = 0;
            poops.add(new Poop(birdX, birdY, airPoopImages, groundPoopImages, playerY + PLAYER_HEIGHT));
        }

        // Update poops
        Iterator<Poop> iterator = poops.iterator();
        while (iterator.hasNext()) {
            Poop poop = iterator.next();
            poop.update();
            if (poop.hasHitPlayer(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                // Trigger death animation
                isDead = true;
                isPredeath = true;
                predeathX = playerX - (ANIMATION_WIDTH - PLAYER_WIDTH) / 2;
                predeathY = playerY - (ANIMATION_HEIGHT - PLAYER_HEIGHT) / 2 - ANIMATION_OFFSET;
                break;
            }
            if (poop.isFinished()) {
                iterator.remove();
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

            if (playerVisible) { // Draw the player only if visible
                g.drawImage(currentPlayerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);
            }

            g.drawImage(currentBirdImage, birdX, birdY, BIRD_WIDTH, BIRD_HEIGHT, this);

            for (Poop poop : poops) {
                poop.draw(g);
            }

            if (isPredeath) {
                g.drawImage(predeathImages[Math.min(predeathIndex, predeathImages.length - 1)], predeathX, predeathY, ANIMATION_WIDTH, ANIMATION_HEIGHT, this);
            }

            if (isDeath) {
                g.drawImage(deathImages[Math.min(deathIndex, deathImages.length - 1)], predeathX, predeathY, ANIMATION_WIDTH, ANIMATION_HEIGHT, this);
            }
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

class Poop {
    public static final int POOP_WIDTH = 30; // Define POOP_WIDTH here
    public static final int POOP_HEIGHT = 30; // Define POOP_HEIGHT here

    private int x, y;
    private int frame;
    private boolean onGround;
    private BufferedImage[] airImages;
    private BufferedImage[] groundImages;
    private int groundFrame;
    private int groundLevel;

    public Poop(int x, int y, BufferedImage[] airImages, BufferedImage[] groundImages, int groundLevel) {
        this.x = x;
        this.y = y;
        this.airImages = airImages;
        this.groundImages = groundImages;
        this.frame = 0;
        this.onGround = false;
        this.groundFrame = 0;
        this.groundLevel = groundLevel;
    }

    public void update() {
        if (!onGround) {
            y += 5; // Falling speed
            if (y >= groundLevel - POOP_HEIGHT) { // When hitting the ground
                y = groundLevel - POOP_HEIGHT; // Align with the ground
                onGround = true;
            }
        } else {
            groundFrame++;
        }
        frame = (frame + 1) % airImages.length;
    }

    public void draw(Graphics g) {
        if (!onGround) {
            g.drawImage(airImages[frame], x, y, null);
        } else {
            if (groundFrame < groundImages.length * 10) { // Slow down the ground animation
                g.drawImage(groundImages[groundFrame / 10], x, y, null);
            }
        }
    }

    public boolean isFinished() {
        return onGround && groundFrame >= groundImages.length * 10;
    }

    public boolean hasHitPlayer(int playerX, int playerY, int playerWidth, int playerHeight) {
        Rectangle poopRect = new Rectangle(x, y, POOP_WIDTH, POOP_HEIGHT);
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        return poopRect.intersects(playerRect);
    }
}
