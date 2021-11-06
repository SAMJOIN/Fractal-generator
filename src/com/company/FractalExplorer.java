package com.company;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import javax.swing.JFileChooser.*;
import javax.swing.filechooser.*;
import javax.imageio.ImageIO.*;
import java.awt.image.*;


public class FractalExplorer
{
    private JButton saveButton; // кнопка сохранения
    private JButton resetButton; // кнопка перезапуска
    private JComboBox myComboBox;

    private int rowsRemaining; // количество строк, которое будет отрисовывать программа

    private int displaySize; // размер дисплея

    private JImageDisplay display; // дисплей

    private FractalGenerator fractal; // пометка о том, что объект является фракталом

    private Rectangle2D.Double range; // размер фигуры

    public FractalExplorer(int size) {
        // задание размера дисплея
        displaySize = size;

        // инициализация генератора фракталов и дисплея
        fractal = new Mandelbrot();
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range);
        display = new JImageDisplay(displaySize, displaySize);

    }

    public void createAndShowGUI()
    {
        // Установка фрейма java.awt.BorderLayout для использования его в дисплее
        display.setLayout(new BorderLayout());
        JFrame myFrame = new JFrame("Fractal Explorer");

        // Установка дисплея по центру
        myFrame.add(display, BorderLayout.CENTER);

        // Создание на дисплее кнопки перезапуска
        resetButton = new JButton("Reset");

        // Экземпрял кнопок и действия с ними
        ButtonHandler resetHandler = new ButtonHandler();
        resetButton.addActionListener(resetHandler);
        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);

        // Создание кнопки, которая позволит закрывать дисплей
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Установка списка для выбора фракталов
        myComboBox = new JComboBox();

        // Добавление существующих фракталов в список
        FractalGenerator mandelbrotFractal = new Mandelbrot();
        myComboBox.addItem(mandelbrotFractal);
        FractalGenerator tricornFractal = new Tricorn();
        myComboBox.addItem(tricornFractal);
        FractalGenerator burningShipFractal = new BurningShip();
        myComboBox.addItem(burningShipFractal);
        /**
         * Для добавления в список новых фракталов пропишите для них наследника от FractalGenerator и вствьте сюда
         * myComboBox.addItem("название фрактала");
         */

        // Экземпляр кнопки выбора и её добавление в дисплей
        ButtonHandler fractalChooser = new ButtonHandler();
        myComboBox.addActionListener(fractalChooser);

        // Настройка списка фркаталов ("сверху")
        JPanel myPanel = new JPanel();
        JLabel myLabel = new JLabel("Fractal:");
        myPanel.add(myLabel);
        myPanel.add(myComboBox);
        myFrame.add(myPanel, BorderLayout.NORTH);


        // Настройка кнопки сохранения ("снизу")
        saveButton = new JButton("Save");
        JPanel myBottomPanel = new JPanel();
        myBottomPanel.add(saveButton);
        myBottomPanel.add(resetButton);
        myFrame.add(myBottomPanel, BorderLayout.SOUTH);

        // Экземпляр кнопки сохранения
        ButtonHandler saveHandler = new ButtonHandler();
        saveButton.addActionListener(saveHandler);

        // Настройка дисплея
        myFrame.pack();
        myFrame.setVisible(true);
        myFrame.setResizable(false);

    }


    private void drawFractal() // Метод для отображения фрактала
    {
        // Отключение элементов управления во время рисования
        enableUI(false);

        // Устанавливаем количество строк прорисовки равное размеру дисплея
        rowsRemaining = displaySize;

        // Перебор каждой строки дисплея и отрисовка фрактала
        for (int x=0; x<displaySize; x++){
            FractalWorker drawRow = new FractalWorker(x);
            drawRow.execute();
        }

    }

    private void enableUI(boolean val) { // Установка режима работы кнопок в соответствии со значением
        myComboBox.setEnabled(val);
        resetButton.setEnabled(val);
        saveButton.setEnabled(val);
    }

    private class ButtonHandler implements ActionListener // Класс для обработки событий
    {
        public void actionPerformed(ActionEvent e)
        {
            // Получение источника действия
            String command = e.getActionCommand();
            // Если источник действия список, то отображаем его
            if (e.getSource() instanceof JComboBox) {
                JComboBox mySource = (JComboBox) e.getSource();
                fractal = (FractalGenerator) mySource.getSelectedItem();
                fractal.getInitialRange(range);
                drawFractal();

            }
            // Если источник кнопка сброса, то сбрасываем фрактал
            else if (command.equals("Reset")) {
                fractal.getInitialRange(range);
                drawFractal();
            }
            // Если источник кнопка сохранения, то сохраняем фрактал
            else if (command.equals("Save")) {

                // Позволяем пользователю выбрать, куда сохранить файл
                JFileChooser myFileChooser = new JFileChooser();

                // Сохранение картинки в формате PNG
                FileFilter extensionFilter =
                        new FileNameExtensionFilter("PNG Images", "png");
                myFileChooser.setFileFilter(extensionFilter);

                // Запрещаем использовать форматы кроме PNG
                myFileChooser.setAcceptAllFileFilterUsed(false);

                // Выбор каталога для сохранения
                int userSelection = myFileChooser.showSaveDialog(display);

                // Если пользователь выбрал файл - продолжить сохранение
                if (userSelection == JFileChooser.APPROVE_OPTION) {

                    // Получение файла и имени файла
                    java.io.File file = myFileChooser.getSelectedFile();
                    String file_name = file.toString();


                }
                // Неудача
                else return;
            }
        }
    }

    /**
     * An inner class to handle MouseListener events from the display.
     */
    private class MouseHandler extends MouseAdapter
    {
        /**
         * When the handler receives a mouse-click event, it maps the pixel-
         * coordinates of the click into the area of the fractal that is being
         * displayed, and then calls the generator's recenterAndZoomRange()
         * method with coordinates that were clicked and a 0.5 scale.
         */
        @Override
        public void mouseClicked(MouseEvent e)
        {
            // Return immediately if rowsRemaining is nonzero.
            if (rowsRemaining != 0) {
                return;
            }
            // Get x coordinate of display area of mouse click.
            int x = e.getX();
            double xCoord = fractal.getCoord(range.x,
                    range.x + range.width, displaySize, x);

            // Get y coordinate of display area of mouse click.
            int y = e.getY();
            double yCoord = fractal.getCoord(range.y,
                    range.y + range.height, displaySize, y);

            // Call the generator's recenterAndZoomRange() method with
            // coordinates that were clicked and a 0.5 scale.
            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);

            // Redraw the fractal after the area being displayed has changed.
            drawFractal();
        }
    }

    /**
     * Computes the color values for a single row of the fractal.
     */
    private class FractalWorker extends SwingWorker<Object, Object>
    {
        int yCoordinate; // Кордината y для строки, для которой будет вычислен цвет
        int[] computedRGBValues;  // Массив для хранения вычисленных цветов
        private FractalWorker(int row) { // Принятие координаты y
            yCoordinate = row;
        }


        protected Object doInBackground() {

            computedRGBValues = new int[displaySize];

            for (int i = 0; i < computedRGBValues.length; i++) {


                double xCoord = fractal.getCoord(range.x,
                        range.x + range.width, displaySize, i);
                double yCoord = fractal.getCoord(range.y,
                        range.y + range.height, displaySize, yCoordinate);

                int iteration = fractal.numIterations(xCoord, yCoord);

                if (iteration == -1){
                    computedRGBValues[i] = 0;
                }

                else {
                    float hue = 0.7f + (float) iteration / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                    computedRGBValues[i] = rgbColor;
                }
            }
            return null;

        }

        protected void done() { // Вызывается, когда фоновая задача завершена.
                                // Рисует пиксели для текущей строки и обновляет отображение для этой строки.

            for (int i = 0; i < computedRGBValues.length; i++) {
                display.drawPixel(i, yCoordinate, computedRGBValues[i]);
            }
            display.repaint(0, 0, yCoordinate, displaySize, 1);
            rowsRemaining--;
            if (rowsRemaining == 0) {
                enableUI(true);
            }
        }
    }

    public static void main(String[] args)
    {
        FractalExplorer displayExplorer = new FractalExplorer(600);
        displayExplorer.createAndShowGUI();
        displayExplorer.drawFractal();
    }
}