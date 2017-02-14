package com.honker.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import static com.honker.main.Main.exit;

public class Window extends JFrame{
    
    @Override
    public void dispose(){
        exit("I'm leaving to apply an update, goodbye!");
        
        super.dispose();
        System.exit(0);
    }
    
    public Window(){
        setTitle("Bot Controls");
        setSize(500, 500);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(null);
        
        setVisible(true);
    }
}
