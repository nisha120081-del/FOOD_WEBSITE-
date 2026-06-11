 import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

class LudoGameSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new LudoFrame();
        });
    }
}

class LudoFrame extends JFrame {
    public LudoFrame() {
        setTitle("Ludo - Realistic Swing Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        GamePanel gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);
        add(gamePanel.getSidePanel(), BorderLayout.EAST);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setVisible(true);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (isUndecorated()) {
                    dispose();
                    setUndecorated(false);
                    setExtendedState(JFrame.NORMAL);
                    setSize(1200, 800);
                    setLocationRelativeTo(null);
                    setVisible(true);
                } else {
                    System.exit(0);
                }
                return true;
            }
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F11) {
                toggleFullscreen();
                return true;
            }
            return false;
        });
    }
    private void toggleFullscreen() {
        dispose();
        if (!isUndecorated()) {
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setUndecorated(false);
            setExtendedState(JFrame.NORMAL);
            setSize(1200, 800);
            setLocationRelativeTo(null);
        }
        setVisible(true);
    }
}

class GamePanel extends JPanel {
    private int cellSize;
    private static final int BOARD_SIZE = 15;
    private int panelSize;
    private GameLogic gameLogic;
    private DicePanel dicePanel;
    private JLabel statusLabel;
    private JLabel currentPlayerLabel;

    public GamePanel() {
        setBackground(new Color(44, 62, 80));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gameLogic = new GameLogic();
        dicePanel = new DicePanel(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleBoardClick(e);
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                calculateSizes();
                repaint();
            }
        });
    }

    private void calculateSizes() {
        int availableWidth = getWidth() - 40;
        int availableHeight = getHeight() - 40;
        int minDim = Math.min(availableWidth, availableHeight);
        cellSize = minDim / BOARD_SIZE;
        panelSize = cellSize * BOARD_SIZE;
    }

    @Override
    public Dimension getPreferredSize() {
        calculateSizes();
        return new Dimension(panelSize + 40, panelSize + 40);
    }

    public JPanel getSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(new Color(52, 73, 94));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidePanel.setPreferredSize(new Dimension(280, 600));
        JLabel title = new JLabel("LUDO GAME");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(236, 240, 241));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentPlayerLabel = new JLabel("Current: RED");
        currentPlayerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        currentPlayerLabel.setForeground(new Color(231, 76, 60));
        currentPlayerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel = new JLabel("Roll the dice to start!");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statusLabel.setForeground(new Color(189, 195, 199));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dicePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton newGameBtn = new JButton("New Game");
        newGameBtn.setFont(new Font("Arial", Font.BOLD, 16));
        newGameBtn.setBackground(new Color(39, 174, 96));
        newGameBtn.setForeground(Color.WHITE);
        newGameBtn.setFocusPainted(false);
        newGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameBtn.setMaximumSize(new Dimension(180, 50));
        newGameBtn.addActionListener(e -> {
            gameLogic.reset();
            updateStatus();
            repaint();
        });
        JLabel hintLabel = new JLabel("ESC: Windowed | F11: Toggle");
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        hintLabel.setForeground(new Color(127, 140, 141));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(title);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        sidePanel.add(currentPlayerLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidePanel.add(statusLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 40)));
        sidePanel.add(dicePanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 40)));
        sidePanel.add(newGameBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidePanel.add(hintLabel);
        sidePanel.add(Box.createVerticalGlue());
        return sidePanel;
    }

    private void handleBoardClick(MouseEvent e) {
        int offsetX = (getWidth() - panelSize) / 2;
        int offsetY = (getHeight() - panelSize) / 2;
        int x = (e.getX() - offsetX) / cellSize;
        int y = (e.getY() - offsetY) / cellSize;
        for (Player player : gameLogic.getPlayers()) {
            for (Token token : player.getTokens()) {
                int[] coords = token.getCoordinates();
                if (coords[0] == x && coords[1] == y) {
                    if (gameLogic.canMoveToken(token)) {
                        gameLogic.moveToken(token);
                        updateStatus();
                        repaint();
                    }
                    return;
                }
            }
        }
    }

    public void handleDiceRoll(int value) {
        gameLogic.handleDiceRoll(value);
        updateStatus();
        repaint();
    }

    private void updateStatus() {
        Player current = gameLogic.getCurrentPlayer();
        String colorName = current.getName();
        Color color = current.getColor();
        currentPlayerLabel.setText("Current: " + colorName);
        currentPlayerLabel.setForeground(color);
        if (gameLogic.isGameOver()) {
            statusLabel.setText("Game Over! " + colorName + " wins!");
        } else {
            statusLabel.setText(gameLogic.getStatusMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        calculateSizes();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int offsetX = (getWidth() - panelSize) / 2;
        int offsetY = (getHeight() - panelSize) / 2;
        g2d.setColor(new Color(245, 245, 240));
        g2d.fillRoundRect(offsetX - 5, offsetY - 5, panelSize + 10, panelSize + 10, 20, 20);
        drawHomeArea(g2d, offsetX, offsetY, 0, 0, new Color(231, 76, 60));
        drawHomeArea(g2d, offsetX, offsetY, 9, 0, new Color(46, 204, 113));
        drawHomeArea(g2d, offsetX, offsetY, 9, 9, new Color(241, 196, 15));
        drawHomeArea(g2d, offsetX, offsetY, 0, 9, new Color(52, 152, 219));
        drawCenterHome(g2d, offsetX, offsetY);
        drawPaths(g2d, offsetX, offsetY);
        drawGrid(g2d, offsetX, offsetY);
        drawTokens(g2d, offsetX, offsetY);

        // Draw "Neelamber" in bottom right corner
        g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 20));
        g2d.setColor(new Color(100, 100, 100, 180));
        String text = "Neelamber";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = getWidth() - textWidth - 30;
        int textY = getHeight() - 30;
        g2d.drawString(text, textX, textY);
    }

    private void drawHomeArea(Graphics2D g2d, int offsetX, int offsetY, int startX, int startY, Color color) {
        int size = 6 * cellSize;
        int x = offsetX + startX * cellSize;
        int y = offsetY + startY * cellSize;
        GradientPaint gradient = new GradientPaint(x, y, color.brighter(), x + size, y + size, color.darker());
        g2d.setPaint(gradient);
        g2d.fillRoundRect(x + 2, y + 2, size - 4, size - 4, 20, 20);
        g2d.setColor(color.darker().darker());
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x + 2, y + 2, size - 4, size - 4, 20, 20);
        g2d.setColor(new Color(245, 245, 240));
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int cx = offsetX + (int)((startX + 1.5 + j * 2.5) * cellSize);
                int cy = offsetY + (int)((startY + 1.5 + i * 2.5) * cellSize);
                g2d.fillOval(cx - cellSize/3, cy - cellSize/3, cellSize*2/3, cellSize*2/3);
                g2d.setColor(color.darker());
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(cx - cellSize/3, cy - cellSize/3, cellSize*2/3, cellSize*2/3);
                g2d.setColor(new Color(245, 245, 240));
            }
        }
    }

    private void drawCenterHome(Graphics2D g2d, int offsetX, int offsetY) {
        int cx = offsetX + 6 * cellSize;
        int cy = offsetY + 6 * cellSize;
        int size = 3 * cellSize;
        g2d.setColor(new Color(236, 240, 241));
        g2d.fillRect(cx, cy, size, size);
        int[] xPoints = {cx, cx + size/2, cx};
        int[] yPoints = {cy, cy + size/2, cy + size};
        g2d.setColor(new Color(231, 76, 60));
        g2d.fillPolygon(xPoints, yPoints, 3);
        xPoints = new int[]{cx + size, cx + size/2, cx + size};
        yPoints = new int[]{cy, cy + size/2, cy + size};
        g2d.setColor(new Color(46, 204, 113));
        g2d.fillPolygon(xPoints, yPoints, 3);
        xPoints = new int[]{cx + size, cx + size/2, cx};
        yPoints = new int[]{cy + size, cy + size/2, cy + size};
        g2d.setColor(new Color(241, 196, 15));
        g2d.fillPolygon(xPoints, yPoints, 3);
        xPoints = new int[]{cx, cx + size/2, cx + size};
        yPoints = new int[]{cy, cy + size/2, cy};
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setColor(new Color(127, 140, 141));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(cx, cy, size, size);
    }

    private void drawPaths(Graphics2D g2d, int offsetX, int offsetY) {
        g2d.setColor(new Color(223, 230, 233));
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (isPathCell(i, j)) {
                    g2d.fillRoundRect(offsetX + i * cellSize + 2, offsetY + j * cellSize + 2, 
                        cellSize - 4, cellSize - 4, 5, 5);
                }
            }
        }
        int[][] safeZones = {{1,6}, {6,1}, {8,13}, {13,8}, {6,12}, {12,6}, {2,8}, {8,2}};
        g2d.setColor(new Color(243, 156, 18));
        for (int[] zone : safeZones) {
            int x = offsetX + zone[0] * cellSize + cellSize/2;
            int y = offsetY + zone[1] * cellSize + cellSize/2;
            g2d.fillOval(x - cellSize/8, y - cellSize/8, cellSize/4, cellSize/4);
        }
    }

    private boolean isPathCell(int x, int y) {
        return (x >= 6 && x <= 8) || (y >= 6 && y <= 8);
    }

    private void drawGrid(Graphics2D g2d, int offsetX, int offsetY) {
        g2d.setColor(new Color(189, 195, 199));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= BOARD_SIZE; i++) {
            g2d.drawLine(offsetX + i * cellSize, offsetY, offsetX + i * cellSize, offsetY + panelSize);
            g2d.drawLine(offsetX, offsetY + i * cellSize, offsetX + panelSize, offsetY + i * cellSize);
        }
    }

    private void drawTokens(Graphics2D g2d, int offsetX, int offsetY) {
        for (Player player : gameLogic.getPlayers()) {
            for (Token token : player.getTokens()) {
                drawToken(g2d, token, offsetX, offsetY);
            }
        }
    }

    private void drawToken(Graphics2D g2d, Token token, int offsetX, int offsetY) {
        int[] coords = token.getCoordinates();
        int x = offsetX + coords[0] * cellSize + cellSize / 2;
        int y = offsetY + coords[1] * cellSize + cellSize / 2;
        Color color = token.getColor();
        int tokenSize = cellSize * 2 / 3;
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(x - tokenSize/2 - 1, y - tokenSize/2 + 2, tokenSize, tokenSize);
        RadialGradientPaint gradient = new RadialGradientPaint(
            x - tokenSize/6, y - tokenSize/6, tokenSize/2, 
            new float[]{0f, 1f}, new Color[]{color.brighter(), color});
        g2d.setPaint(gradient);
        g2d.fillOval(x - tokenSize/2, y - tokenSize/2, tokenSize, tokenSize);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fillOval(x - tokenSize/3, y - tokenSize/3, tokenSize/2, tokenSize/2);
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - tokenSize/2, y - tokenSize/2, tokenSize, tokenSize);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(10, cellSize/4)));
        String num = String.valueOf(token.getId());
        FontMetrics fm = g2d.getFontMetrics();
        int numX = x - fm.stringWidth(num) / 2;
        int numY = y + fm.getAscent() / 2 - 2;
        g2d.drawString(num, numX, numY);
    }
}

class DicePanel extends JPanel {
    private int currentValue = 1;
    private boolean rolling = false;
    private GamePanel gamePanel;

    public DicePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setPreferredSize(new Dimension(120, 120));
        setBackground(new Color(52, 73, 94));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                rollDice();
            }
        });
    }

    private void rollDice() {
        if (rolling) return;
        rolling = true;
        javax.swing.Timer timer = new javax.swing.Timer(80, null);
        final int[] count = {0};
        final int maxRolls = 12;
        timer.addActionListener(e -> {
            count[0]++;
            currentValue = (int)(Math.random() * 6) + 1;
            repaint();
            if (count[0] >= maxRolls) {
                timer.stop();
                currentValue = (int)(Math.random() * 6) + 1;
                rolling = false;
                repaint();
                gamePanel.handleDiceRoll(currentValue);
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = 100;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        // Dice background with 3D effect
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRoundRect(x + 3, y + 3, size, size, 15, 15);

        GradientPaint gradient = new GradientPaint(x, y, new Color(245, 245, 245), x + size, y + size, new Color(200, 200, 200));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(x, y, size, size, 15, 15);

        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, size, size, 15, 15);

        // Draw dots based on dice value
        g2d.setColor(new Color(40, 40, 40));
        int dotSize = 14;
        int cx = x + size / 2;
        int cy = y + size / 2;
        int offset = 22;

        switch (currentValue) {
            case 1:
                drawDot(g2d, cx, cy, dotSize);
                break;
            case 2:
                drawDot(g2d, cx - offset, cy - offset, dotSize);
                drawDot(g2d, cx + offset, cy + offset, dotSize);
                break;
            case 3:
                drawDot(g2d, cx - offset, cy - offset, dotSize);
                drawDot(g2d, cx, cy, dotSize);
                drawDot(g2d, cx + offset, cy + offset, dotSize);
                break;
            case 4:
                drawDot(g2d, cx - offset, cy - offset, dotSize);
                drawDot(g2d, cx + offset, cy - offset, dotSize);
                drawDot(g2d, cx - offset, cy + offset, dotSize);
                drawDot(g2d, cx + offset, cy + offset, dotSize);
                break;
            case 5:
                drawDot(g2d, cx - offset, cy - offset, dotSize);
                drawDot(g2d, cx + offset, cy - offset, dotSize);
                drawDot(g2d, cx, cy, dotSize);
                drawDot(g2d, cx - offset, cy + offset, dotSize);
                drawDot(g2d, cx + offset, cy + offset, dotSize);
                break;
            case 6:
                drawDot(g2d, cx - offset, cy - offset, dotSize);
                drawDot(g2d, cx + offset, cy - offset, dotSize);
                drawDot(g2d, cx - offset, cy, dotSize);
                drawDot(g2d, cx + offset, cy, dotSize);
                drawDot(g2d, cx - offset, cy + offset, dotSize);
                drawDot(g2d, cx + offset, cy + offset, dotSize);
                break;
        }
    }

    private void drawDot(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillOval(x - size/2, y - size/2, size, size);
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillOval(x - size/2 + 2, y - size/2 + 2, size - 4, size - 4);
        g2d.setColor(new Color(20, 20, 20));
        g2d.fillOval(x - size/2 + 3, y - size/2 + 3, size - 6, size - 6);
    }
}

class GameLogic {
    private List<Player> players;
    private int currentPlayerIndex;
    private int lastDiceValue;
    private boolean diceRolled;
    private boolean gameOver;
    private String statusMessage;

    private static final int[][] RED_PATH = {
        {1,6},{2,6},{3,6},{4,6},{5,6},{6,5},{6,4},{6,3},{6,2},{6,1},
        {6,0},{7,0},{8,0},{8,1},{8,2},{8,3},{8,4},{8,5},{9,6},{10,6},
        {11,6},{12,6},{13,6},{14,6},{14,7},{14,8},{13,8},{12,8},{11,8},
        {10,8},{9,8},{8,9},{8,10},{8,11},{8,12},{8,13},{8,14},{7,14},
        {6,14},{6,13},{6,12},{6,11},{6,10},{6,9},{5,8},{4,8},{3,8},
        {2,8},{1,8},{0,8},{0,7}
    };

    private static final int[][] GREEN_PATH = {
        {8,1},{8,2},{8,3},{8,4},{8,5},{9,6},{10,6},{11,6},{12,6},{13,6},
        {14,6},{14,7},{14,8},{13,8},{12,8},{11,8},{10,8},{9,8},{8,9},{8,10},
        {8,11},{8,12},{8,13},{8,14},{7,14},{6,14},{6,13},{6,12},{6,11},{6,10},
        {6,9},{5,8},{4,8},{3,8},{2,8},{1,8},{0,8},{0,7},{0,6},{1,6},
        {2,6},{3,6},{4,6},{5,6},{6,5},{6,4},{6,3},{6,2},{6,1},{6,0},
        {7,0}
    };

    private static final int[][] YELLOW_PATH = {
        {13,8},{12,8},{11,8},{10,8},{9,8},{8,9},{8,10},{8,11},{8,12},{8,13},
        {8,14},{7,14},{6,14},{6,13},{6,12},{6,11},{6,10},{6,9},{5,8},{4,8},
        {3,8},{2,8},{1,8},{0,8},{0,7},{0,6},{1,6},{2,6},{3,6},{4,6},
        {5,6},{6,5},{6,4},{6,3},{6,2},{6,1},{6,0},{7,0},{8,0},{8,1},
        {8,2},{8,3},{8,4},{8,5},{9,6},{10,6},{11,6},{12,6},{13,6},{14,6},
        {14,7}
    };

    private static final int[][] BLUE_PATH = {
        {6,13},{6,12},{6,11},{6,10},{6,9},{5,8},{4,8},{3,8},{2,8},{1,8},
        {0,8},{0,7},{0,6},{1,6},{2,6},{3,6},{4,6},{5,6},{6,5},{6,4},
        {6,3},{6,2},{6,1},{6,0},{7,0},{8,0},{8,1},{8,2},{8,3},{8,4},
        {8,5},{9,6},{10,6},{11,6},{12,6},{13,6},{14,6},{14,7},{14,8},{13,8},
        {12,8},{11,8},{10,8},{9,8},{8,9},{8,10},{8,11},{8,12},{8,13},{8,14},
        {7,14}
    };

    public GameLogic() { initializeGame(); }

    private void initializeGame() {
        players = new ArrayList<>();
        players.add(new Player("Red", new Color(231, 76, 60), new int[][]{{1,1},{1,4},{4,1},{4,4}}, RED_PATH));
        players.add(new Player("Green", new Color(46, 204, 113), new int[][]{{10,1},{10,4},{13,1},{13,4}}, GREEN_PATH));
        players.add(new Player("Yellow", new Color(241, 196, 15), new int[][]{{10,10},{10,13},{13,10},{13,13}}, YELLOW_PATH));
        players.add(new Player("Blue", new Color(52, 152, 219), new int[][]{{1,10},{1,13},{4,10},{4,13}}, BLUE_PATH));
        currentPlayerIndex = 0;
        diceRolled = false;
        gameOver = false;
        statusMessage = "Roll the dice!";
    }

    public void handleDiceRoll(int value) {
        lastDiceValue = value;
        diceRolled = true;
        Player current = getCurrentPlayer();
        boolean canMove = false;
        for (Token token : current.getTokens()) {
            if (canMoveToken(token)) { canMove = true; break; }
        }
        if (!canMove) {
            statusMessage = "No valid moves! Next player.";
            nextTurn();
        } else {
            statusMessage = "Select token to move " + value + " steps.";
        }
    }

    public boolean canMoveToken(Token token) {
        if (!diceRolled || gameOver) return false;
        if (!token.getOwner().equals(getCurrentPlayer())) return false;
        if (token.isAtHome() && lastDiceValue != 6) return false;
        if (!token.isAtHome()) {
            int newPos = token.getPathPosition() + lastDiceValue;
            if (newPos >= token.getOwner().getPath().length) return false;
        }
        return true;
    }

    public void moveToken(Token token) {
        if (!canMoveToken(token)) return;
        if (token.isAtHome()) {
            token.leaveHome();
            token.setPathPosition(0);
        } else {
            int newPos = token.getPathPosition() + lastDiceValue;
            token.setPathPosition(newPos);
            checkCapture(token);
            if (token.hasReachedHome()) checkWinCondition();
        }
        updateTokenCoordinates(token);
        if (lastDiceValue != 6) nextTurn();
        else {
            diceRolled = false;
            statusMessage = "Rolled 6! Roll again.";
        }
    }

    private void updateTokenCoordinates(Token token) {
        if (token.isAtHome()) return;
        int pos = token.getPathPosition();
        int[][] path = token.getOwner().getPath();
        if (pos < path.length) {
            int[] coord = path[pos];
            token.setCoordinates(coord[0], coord[1]);
        }
    }

    private void checkCapture(Token mover) {
        int[] moverPos = mover.getCoordinates();
        for (Player player : players) {
            if (player.equals(mover.getOwner())) continue;
            for (Token token : player.getTokens()) {
                if (!token.isAtHome() && !token.hasReachedHome()) {
                    int[] pos = token.getCoordinates();
                    if (pos[0] == moverPos[0] && pos[1] == moverPos[1]) {
                        token.returnHome();
                        statusMessage = "Captured! Token sent home.";
                    }
                }
            }
        }
    }

    private void checkWinCondition() {
        Player current = getCurrentPlayer();
        boolean allHome = true;
        for (Token token : current.getTokens()) {
            if (!token.hasReachedHome()) { allHome = false; break; }
        }
        if (allHome) {
            gameOver = true;
            statusMessage = current.getName() + " wins!";
        }
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        diceRolled = false;
        statusMessage = getCurrentPlayer().getName() + "'s turn. Roll dice!";
    }

    public void reset() { initializeGame(); }
    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public List<Player> getPlayers() { return players; }
    public boolean isGameOver() { return gameOver; }
    public String getStatusMessage() { return statusMessage; }
}

class Player {
    private String name;
    private Color color;
    private List<Token> tokens;
    private int[][] homeCoordinates;
    private int[][] path;

    public Player(String name, Color color, int[][] homeCoords, int[][] path) {
        this.name = name;
        this.color = color;
        this.homeCoordinates = homeCoords;
        this.path = path;
        this.tokens = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tokens.add(new Token(i + 1, this, homeCoords[i]));
        }
    }
    public String getName() { return name; }
    public Color getColor() { return color; }
    public List<Token> getTokens() { return tokens; }
    public int[][] getHomeCoordinates() { return homeCoordinates; }
    public int[][] getPath() { return path; }
}

class Token {
    private int id;
    private Player owner;
    private int[] coordinates;
    private int[] homeCoordinates;
    private int pathPosition;
    private boolean atHome;
    private boolean reachedHome;

    public Token(int id, Player owner, int[] homeCoords) {
        this.id = id;
        this.owner = owner;
        this.homeCoordinates = homeCoords;
        this.coordinates = new int[]{homeCoords[0], homeCoords[1]};
        this.pathPosition = -1;
        this.atHome = true;
        this.reachedHome = false;
    }
    public int getId() { return id; }
    public Player getOwner() { return owner; }
    public Color getColor() { return owner.getColor(); }
    public int[] getCoordinates() { return coordinates; }
    public int getPathPosition() { return pathPosition; }
    public boolean isAtHome() { return atHome; }
    public boolean hasReachedHome() { return reachedHome; }
    public void setCoordinates(int x, int y) { this.coordinates = new int[]{x, y}; }
    public void setPathPosition(int pos) {
        this.pathPosition = pos;
        if (pos >= 50) reachedHome = true;
    }
    public void leaveHome() { this.atHome = false; }
    public void returnHome() {
        this.atHome = true;
        this.pathPosition = -1;
        this.coordinates = new int[]{homeCoordinates[0], homeCoordinates[1]};
        this.reachedHome = false;
    }
}