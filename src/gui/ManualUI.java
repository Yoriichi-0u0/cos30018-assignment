package gui;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class ManualUI {

    public static String getCounterOffer(String buyerName, String dealerName, double dealerPrice) {
        AtomicReference<String> result = new AtomicReference<>("REJECT");

        try {
            SwingUtilities.invokeAndWait(() -> {
                // 'true' makes this a Modal dialog, which freezes the background code until closed
                JDialog dialog = new JDialog((Frame)null, "Manual Negotiation - " + buyerName, true);
                dialog.setSize(350, 200);
                dialog.setLocationRelativeTo(null);

                JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                panel.add(new JLabel("<html><b>Dealer " + dealerName + "</b> is offering a car.</html>"));
                panel.add(new JLabel("Asking Price: RM " + dealerPrice));

                JTextField txtOffer = new JTextField();
                txtOffer.setBorder(BorderFactory.createTitledBorder("Enter Counter-Offer (RM):"));
                panel.add(txtOffer);

                JPanel btnPanel = new JPanel(new FlowLayout());
                JButton btnCounter = new JButton("Counter");
                JButton btnReject = new JButton("Walk Away");

                btnCounter.addActionListener(e -> {
                    if (!txtOffer.getText().trim().isEmpty()) {
                        result.set(txtOffer.getText().trim());
                        dialog.dispose();
                    }
                });

                btnReject.addActionListener(e -> {
                    result.set("REJECT");
                    dialog.dispose();
                });

                btnPanel.add(btnCounter);
                btnPanel.add(btnReject);
                panel.add(btnPanel);

                dialog.add(panel);
                dialog.setVisible(true); 
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.get();
    }
}
