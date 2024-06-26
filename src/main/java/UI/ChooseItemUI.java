
package UI;

import dvm.service.controller.card.CardServiceController;
import dvm.service.controller.card.Refund;
import dvm.service.controller.item.ItemCheck;
import dvm.domain.item.ItemRepository;
import dvm.service.controller.network.RequestToServiceController;

import javax.swing.*;
import java.awt.*;

public class ChooseItemUI extends JPanel {
    private CardLayout cardLayout;
    public JPanel mainPanel;
    private JPanel beverageSelectionScreen;
    private JPanel messageScreen;
    private JPanel prepayScreen;
    private JPanel refundScreen;
    private CardServiceController cardServiceController;
    private ItemCheck itemCheck;
    private RequestToServiceController requestToServiceController;

    private int selectedPrice;
    private int selectedQuantity;
    private int selectedItemId;
    private int[] selectedDVMLocation;
    private String cardNumber;
    private String authenticationCode;

    public ChooseItemUI() {
        cardServiceController = new CardServiceController();
        itemCheck = new ItemCheck();
        requestToServiceController = new RequestToServiceController();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        beverageSelectionScreen = createBeverageSelectionScreen();
        messageScreen = createMessageScreen();
        prepayScreen = createPrepayScreen();
        refundScreen = createRefundScreen();

        mainPanel.add(beverageSelectionScreen, "BeverageSelectionScreen");
        mainPanel.add(messageScreen, "MessageScreen");
        mainPanel.add(prepayScreen, "PrepayScreen");
        mainPanel.add(refundScreen, "RefundScreen");

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "BeverageSelectionScreen");
    }

    private JPanel createBeverageSelectionScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("음료 선택", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0x3B5998));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, 600, 50);
        panel.add(titleLabel);

        String[] beverages = {"콜라", "사이다", "녹차", "홍차", "밀크티", "탄산수", "보리차", "캔커피",
                "물", "에너지드링크", "유자차", "식혜", "아이스티", "딸기주스", "오렌지주스",
                "포도주스", "이온음료", "아메리카노", "핫초코", "카페라떼"};

        int x = 30, y = 75, width = 100, height = 30;
        for (int i = 0; i < beverages.length; i++) {
            JButton button = new JButton(beverages[i]);
            styleButton(button);
            button.setBounds(x + (i % 5) * (width + 10), y + (i / 5) * (height + 20), width, height);
            panel.add(button);
        }

        JLabel typeLabel = new JLabel("종류:");
        typeLabel.setBounds(50, 300, 50, 30);
        panel.add(typeLabel);

        JComboBox<String> beverageComboBox = new JComboBox<>(beverages);
        beverageComboBox.setBounds(100, 300, 150, 30);
        panel.add(beverageComboBox);

        JLabel quantityLabel = new JLabel("개수:");
        quantityLabel.setBounds(300, 300, 50, 30);
        panel.add(quantityLabel);

        JComboBox<Integer> quantityComboBox = new JComboBox<>();
        for (int i = 1; i <= 99; i++) {
            quantityComboBox.addItem(i);
        }
        quantityComboBox.setBounds(350, 300, 80, 30);
        panel.add(quantityComboBox);

        JButton okButton = new JButton("OK");
        styleButton(okButton);
        okButton.setBounds(450, 300, 100, 30);
        okButton.addActionListener(e -> {
            String selectedBeverage = (String) beverageComboBox.getSelectedItem();
            selectedQuantity = (int) quantityComboBox.getSelectedItem();
            selectedItemId = beverageComboBox.getSelectedIndex() + 1;

            selectedPrice = getItemPriceById(selectedItemId) * selectedQuantity;

            System.out.println("선택한 음료: " + selectedBeverage);
            System.out.println("선택한 개수: " + selectedQuantity);
            System.out.println("선택한 아이템 ID: " + selectedItemId);
            System.out.println("선택한 가격: " + selectedPrice);

            // 현재 DVM에 재고 확인
            boolean isStockEnough = itemCheck.process(selectedItemId, selectedQuantity);
            if (isStockEnough) {
                System.out.println("현재 DVM에 재고 충분");
                cardLayout.show(mainPanel, "MessageScreen");
            } else {
                System.out.println("현재 DVM에 재고 부족, 다른 DVM에 재고 확인 요청");
                // 다른 DVM에 재고 확인
                requestToServiceController.sendStockRequest(selectedItemId, selectedQuantity);
                boolean checkDVM = requestToServiceController.checkAvailableDVM(selectedItemId, selectedQuantity, cardNumber, selectedPrice);
                boolean checkPrepay = false;
                if (checkDVM) {
                    cardLayout.show(mainPanel, "PrepayScreen");
                    requestToServiceController.sendPrepayRequest(selectedItemId, selectedQuantity,cardNumber,selectedPrice);
                }
                else {
                    System.out.println("다른 DVM에 재고 없거나 선결제 실패");
                    cardLayout.show(mainPanel, "RefundScreen");
                }
//                if (checkDVM) {
//                    checkPrepay = requestToServiceController.sendPrepayRequest(selectedItemId, selectedQuantity, cardNumber, selectedPrice);
//                }if (checkPrepay == true ){
//                    String xLocation = requestToServiceController.getReturnValue()[0];
//                    String yLocation = requestToServiceController.getReturnValue()[1];
//                    String certCode = requestToServiceController.getReturnValue()[2];
//                    System.out.println("선결제 가능한 위치" + xLocation+ ","+yLocation );
//                    System.out.println("인증코드: "+certCode);
//                    cardLayout.show(mainPanel, "PrepayScreen");
//                    showDVMLocation();
//                }
            }
        });
        panel.add(okButton);

        return panel;
    }

    private JPanel createMessageScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Message", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0x3B5998));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, 600, 50);
        panel.add(titleLabel);

        JLabel messageLabel1 = new JLabel("<html>선택하신 음료수의 재고가 충분합니다.</html>", JLabel.CENTER);
        JLabel messageLabel2 = new JLabel("<html><br>결제를 진행하시겠습니까?</html>", JLabel.CENTER);
        messageLabel1.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel2.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel1.setBounds(50, 100, 500, 100);
        messageLabel2.setBounds(50, 130, 500, 100);
        panel.add(messageLabel1);
        panel.add(messageLabel2);

        JButton yesButton = new JButton("YES");
        styleButton(yesButton);
        yesButton.setBounds(150, 250, 100, 50);
        yesButton.addActionListener(e -> {
            Runnable onSuccess = () -> {
                updateItemStockAfterPurchase();
                showPaymentSuccessMessage();
            };
            Runnable onInsufficientBalance = () -> {
                PaymentUI paymentUI = new PaymentUI(false, this::goToMainMenu, this::goToBeverageSelection);
                paymentUI.setVisible(true);
            };
            Runnable onRetry = () -> {
                cardLayout.show(mainPanel, "BeverageSelectionScreen");
            };
            CardInputUI cardInputUI = new CardInputUI(selectedPrice, onSuccess, onRetry);
            cardInputUI.setVisible(true);
        });
        panel.add(yesButton);

        JButton noButton = new JButton("NO");
        styleButton(noButton);
        noButton.setBounds(350, 250, 100, 50);
        noButton.addActionListener(e -> {
            goToMainMenu();
        });
        panel.add(noButton);

        return panel;
    }

    private JPanel createPrepayScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("다른 DVM에서 구매 진행하기", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0x3B5998));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, 600, 50);
        panel.add(titleLabel);

        JLabel messageLabel = new JLabel("<html>다른 dvm에서 구매를 진행하시겠습니까?</html>", JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setBounds(50, 100, 500, 100);
        panel.add(messageLabel);

        JButton yesButton = new JButton("YES");
        styleButton(yesButton);
        yesButton.setBounds(150, 250, 100, 50);
        yesButton.addActionListener(e -> {
            Runnable onSuccess = () -> {
                showDVMLocation();
            };

            // 카드 입력 창
            Runnable onRetry = () -> {
                cardLayout.show(mainPanel, "BeverageSelectionScreen");
            };
            CardInputUI cardInputUI = new CardInputUI(selectedPrice, onSuccess, onRetry);
            cardInputUI.setVisible(true);
        });
        panel.add(yesButton);

        JButton noButton = new JButton("NO");
        styleButton(noButton);
        noButton.setBounds(350, 250, 100, 50);
        noButton.addActionListener(e -> {
            goToMainMenu();
        });
        panel.add(noButton);

        return panel;
    }

    private JPanel createRefundScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Refund", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0x3B5998));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, 600, 50);
        panel.add(titleLabel);

        JLabel messageLabel = new JLabel("<html>모든 DVM에서 선결제 실패하였습니다.<br>결제를 하신 경우라면, 환불 처리가 완료되었습니다.</html>", JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setBounds(50, 100, 500, 100);
        panel.add(messageLabel);

        JButton okButton = new JButton("OK");
        styleButton(okButton);
        okButton.setBounds(250, 250, 100, 50);
        okButton.addActionListener(e -> {
            Refund refund = new Refund();
            goToMainMenu();
        });
        panel.add(okButton);

        return panel;
    }

    private void updateItemStockAfterPurchase() {
        ItemRepository itemRepository = ItemRepository.getInstance();
        itemRepository.updateItemStock(selectedItemId, -selectedQuantity);
    }

    private void showDVMLocation() {
        JFrame locationFrame = new JFrame("DVM Location");
        locationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        locationFrame.setSize(400, 270);
        locationFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("DVM Location", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0x3B5998));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, 400, 50);
        panel.add(titleLabel);

        JLabel messageLabel = new JLabel(String.format("<html><center>선결제가 완료되었습니다.<br>다음 위치에서 음료를 수령하세요: <br>위치: (%s, %s)<br>인증 코드: %s<center></html>",
                requestToServiceController.getReturnValue()[0], requestToServiceController.getReturnValue()[1], requestToServiceController.getReturnValue()[2]), JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setBounds(50, 70, 300, 100);
        panel.add(messageLabel);

        JButton okButton = new JButton("OK");
        styleButton(okButton);
        okButton.setBounds(150, 190, 100, 30);
        okButton.addActionListener(e -> {
            goToMainMenu();
            locationFrame.dispose();
        });
        panel.add(okButton);

        locationFrame.add(panel);
        locationFrame.setVisible(true);
    }

    private void showPaymentSuccessMessage() {
        JFrame successFrame = new JFrame("Payment Success");
        successFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        successFrame.setSize(400, 270);
        successFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Payment Success", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0x3B5998));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, 400, 50);
        panel.add(titleLabel);

        JLabel messageLabel = new JLabel("<html>결제가 성공적으로 완료되었습니다.<br>감사합니다!</html>", JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setBounds(50, 60, 300, 100);
        panel.add(messageLabel);

        JButton okButton = new JButton("OK");
        styleButton(okButton);
        okButton.setBounds(150, 150, 100, 30);
        okButton.addActionListener(e -> {
            goToMainMenu();
            successFrame.dispose();
        });
        panel.add(okButton);

        successFrame.add(panel);
        successFrame.setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(0x3B5998));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0x3B5998), 1));
    }

    private boolean checkStock(String beverageName, int quantity) {
        int itemId = getItemIdByName(beverageName);
        return itemCheck.process(itemId, quantity);
    }

    private int getItemIdByName(String beverageName) {
        String[] beverages = {"콜라", "사이다", "녹차", "홍차", "밀크티", "탄산수", "보리차", "캔커피",
                "물", "에너지드링크", "유자차", "식혜", "아이스티", "딸기주스", "오렌지주스",
                "포도주스", "이온음료", "아메리카노", "핫초코", "카페라떼"};
        for (int i = 0; i < beverages.length; i++) {
            if (beverages[i].equals(beverageName)) {
                return i + 1;
            }
        }
        return -1;
    }

    private int getItemPriceById(int itemId) {
        ItemRepository itemRepository = ItemRepository.getInstance();
        return itemRepository.findItem(itemId).getPrice();
    }

    private void goToMainMenu() {
        JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        mainFrame.getContentPane().removeAll();
        MainUI mainUI = new MainUI();
        mainFrame.add(mainUI.mainPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void goToBeverageSelection() {
        cardLayout.show(mainPanel, "BeverageSelectionScreen");
    }
}
