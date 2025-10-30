import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class QuizApp {
    static class Questions {
        private String text;
        private String[] options;
        private int correctIndex;

        public Questions(String text, String[] options, int correctIndex) {
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }

        public String gettext() {
            return text;
        }

        public String[] getoptions() {
            return options;
        }

        public int getCorrectIndex() {
            return correctIndex;
        }
    }

    private JFrame frame;
    private JLabel questionLabel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionsGroup;
    private JButton nextButton;
    private JLabel progressLabel;
    private JLabel scoreLabel;

    private final List<Questions> que = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;

    public QuizApp() {
        loadQuestionsFromText("src/Quiz_Questions.txt");
        initUI();
        printQuestion(0);
    }

    private void loadQuestionsFromText(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<Questions> allQuestions = new ArrayList<>();
            String questionLine;

            while ((questionLine = br.readLine()) != null) {
                String optionsLine = br.readLine();
                if (optionsLine == null) break;

                String[] parts = optionsLine.split("\\|");
                if (parts.length < 5) continue;

                String[] options = {parts[0], parts[1], parts[2], parts[3]};
                int correctIndex = Integer.parseInt(parts[4].trim()) - 1;

                allQuestions.add(new Questions(questionLine, options, correctIndex));
            }

            // Randomly shuffle and pick 5 questions
            Collections.shuffle(allQuestions);
            int questionCount = Math.min(5, allQuestions.size());
            que.addAll(allQuestions.subList(0, questionCount));

            System.out.println("Loaded " + que.size() + " random questions from text file.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame,
                    "Error reading text file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void initUI() {
        frame = new JFrame("DSA Quiz App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 380);
        frame.setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        progressLabel = new JLabel("Question 1/" + que.size());
        scoreLabel = new JLabel("Score: 0");
        topPanel.add(progressLabel, BorderLayout.WEST);
        topPanel.add(scoreLabel, BorderLayout.EAST);
        main.add(topPanel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        questionLabel = new JLabel("Question text");
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        center.add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionButtons = new JRadioButton[4];
        optionsGroup = new ButtonGroup();
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setActionCommand(String.valueOf(i));
            optionsGroup.add(optionButtons[i]);
            optionsPanel.add(optionButtons[i]);
            optionsPanel.add(Box.createVerticalStrut(6));
        }
        center.add(optionsPanel, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nextButton = new JButton("Next");
        nextButton.addActionListener(e -> onNextPressed());
        bottom.add(nextButton);
        main.add(bottom, BorderLayout.SOUTH);

        frame.getContentPane().add(main);
        frame.setVisible(true);
    }

    private void printQuestion(int index) {
        if (index < 0 || index >= que.size()) return;
        currentIndex = index;
        Questions q = que.get(index);
        questionLabel.setText("<html><body style='width:500px'>" + (index + 1) + ". " + q.gettext() + "</body></html>");
        String[] opts = q.getoptions();

        for (int i = 0; i < optionButtons.length; i++) {
            if (i < opts.length) {
                optionButtons[i].setText(opts[i]);
                optionButtons[i].setVisible(true);
            } else {
                optionButtons[i].setVisible(false);
            }
        }
        optionsGroup.clearSelection();
        progressLabel.setText("Question " + (currentIndex + 1) + "/" + que.size());
        scoreLabel.setText("Score: " + score);

        nextButton.setText(currentIndex == que.size() - 1 ? "Submit" : "Next");
    }

    private void onNextPressed() {
        ButtonModel selected = optionsGroup.getSelection();
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "Please select an option before proceeding.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedIndex = Integer.parseInt(selected.getActionCommand());
        Questions q = que.get(currentIndex);
        if (selectedIndex == q.getCorrectIndex()) {
            score++;
        }

        if (currentIndex < que.size() - 1) {
            printQuestion(currentIndex + 1);
        } else {
            showResult();
        }
    }

    private void showResult() {
        String message = String.format("You scored %d out of %d.", score, que.size());
        int choice = JOptionPane.showOptionDialog(
                frame,
                message + "\nWould you like to restart the quiz?",
                "Quiz Finished",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Restart", "Exit"},
                "Restart"
        );

        if (choice == JOptionPane.YES_OPTION) {
            restartQuiz();
        } else {
            frame.dispose();
        }
    }

    private void restartQuiz() {
        score = 0;
        printQuestion(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizApp::new);
    }
}
