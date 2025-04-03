package domotic;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Interfaz para la clase que recibirá actualizaciones del calendario
 */
interface CalendarListener {
    void updateTime(int hour);
}

/**
 * Clase que representa un calendario con un control deslizante para simular el paso del tiempo
 */
public class Calendar extends JFrame {
    private JSlider slider;
    private Timer timer;
    private CalendarListener listener;

    public Calendar(CalendarListener listener) {
        this.listener = listener;
        setTitle("Hora Slider");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 100);

        slider = new JSlider(0, 24, 0); // Rango de horas
        slider.setMinimum(0);
        slider.setMaximum(23);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setValue(0);
        Dictionary<Integer, JLabel> lbs = new Hashtable<Integer, JLabel>();
        for (int i = 0; i < 24; i++) {
            lbs.put(i, new JLabel("" + i + ":00"));
        }
        slider.setLabelTable(lbs);

        timer = new Timer(30000, e -> {
            int hora = slider.getValue();
            if (hora < 23) {
                slider.setValue(hora + 1);
            } else {
                slider.setValue(0);
            }
        });

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                listener.updateTime(slider.getValue());
            }
        });
        timer.start();
        add(slider);
        slider.setPreferredSize(new Dimension(800, 50));
        setVisible(true);
        setAlwaysOnTop(true);
    }

    public int getHora() {
        return slider.getValue();
    }
}
