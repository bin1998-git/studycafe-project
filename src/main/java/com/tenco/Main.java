package com.tenco;

import com.tenco.view.StudyCafeView;
import javax.swing.SwingUtilities;

public class    Main {
    public static void main(String[] args) {
        // macOS 상단 메뉴바 통합 (Windows/Linux에서는 무시됨)
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        SwingUtilities.invokeLater(() -> {
            StudyCafeView view = new StudyCafeView();
            view.setVisible(true);
        });
    }
}
