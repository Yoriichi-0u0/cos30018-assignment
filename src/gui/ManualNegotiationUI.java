package gui;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class ManualNegotiationUI {

    // Static method for the Agent to call from its background thread
    public static String getCounterOffer(String buyerName, String dealerName, String carDetails, double dealerPrice, double maxBudget, int round) {
        AtomicReference<String> result = new AtomicReference<>("REJECT");

        try {
            // Build and show the UI on the Swing Event Thread
            SwingUtilities.invokeAndWait(() -> {
                // Modal JDialog freezes the calling thread (the Agent)
                JDialog dialog = new JDialog((Frame)null, "Manual Negotiation - " + buyerName, true);
                dialog.setSize(450, 300);
                dialog.setLocationRelativeTo(null);
                dialog.setAlwaysOnTop(true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                JPanel panel = new JPanel(new GridLayout(6, 1, 10, 5));
                panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                panel.add(new JLabel("<html><b>Dealer: " + dealerName + "</b> (Round " + round + ")</html>"));
                panel.add(new JLabel("Vehicle: " + carDetails));
                panel.add(new JLabel("Dealer Price: RM " + dealerPrice));
                panel.add(new JLabel("Your Max Budget: RM " + maxBudget));

                JTextField txtOffer = new JTextField();
                txtOffer.setBorder(BorderFactory.createTitledBorder("Your Counter-Offer (RM):"));
                panel.add(txtOffer);

                JPanel btnPanel = new JPanel(new FlowLayout());
                JButton btnAccept = new JButton("Accept");
                btnAccept.setBackground(new Color(144, 238, 144));
                JButton btnCounter = new JButton("Counter");
                btnCounter.setBackground(new Color(173, 216, 230));
                JButton btnReject = new JButton("Reject");
                btnReject.setBackground(new Color(255, 182, 193));

                btnAccept.addActionListener(e -> {
                    result.set(String.valueOf(dealerPrice));
                    dialog.dispose();
                });

                btnCounter.addActionListener(e -> {
                    String offer = txtOffer.getText().trim();
                    if (!offer.isEmpty()) {
                        result.set(offer);
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Please enter a price!");
                    }
                });

                btnReject.addActionListener(e -> {
                    result.set("REJECT");
                    dialog.dispose();
                });

                btnPanel.add(btnAccept);
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
